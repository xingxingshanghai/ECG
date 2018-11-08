package com.example.bluetooth.app;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.ecg.StatusMsg;

import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class MainPC80B extends Activity {
	private Thread drawThread;
	private TextView tv_Gain, tv_HR, tv_MSG,mTimer;
	private ImageView img_Battery, img_Smooth, img_Pulse;
	private int temp = 0;
	private boolean timeStamp = false;

	private LinearLayout mRightCurveLayout;//存放右图表的布局容器
	private GraphicalView mView, mView2;//左右图表
	private ChartService mService, mService2;

	/**
	 * 心电测量结果
	 */
	private String[] measureResult;
	private MyCount mc;
	/**
	 * 电量等级
	 */
	private int batteryRes[] = { R.drawable.battery_0, R.drawable.battery_1, R.drawable.battery_2, R.drawable.battery_3 };
Bundle tempBundle;
	//new
	private Handler handler;
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			mService2.updateCharts(tempBundle.getDoubleArray("datas"),1000);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置当前activity常亮 必须放在setContentView之前
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main_context_pc80b);
		startService(new Intent(this, ReceiveService.class));
		init();
		Intent i = new Intent(this, ConnectActivity.class);
		i.putExtra("device", 3);
		startActivityForResult(i, 0x100);
	}

	private void init() {

		mRightCurveLayout = (LinearLayout) findViewById(R.id.left_temperature_curve);
		mService2 = new ChartService(this);
		mService2.setXYMultipleSeriesDataset("心电曲线图");
		mService2.setXYMultipleSeriesRenderer(1000, 3, "心电曲线图", "时间", "相对幅度", Color.RED, Color.YELLOW, Color.GREEN, Color.WHITE);
		mView2 = mService2.getGraphicalView();
		handler = new Handler();
		//将图表添加到布局容器中
		mRightCurveLayout.addView(mView2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		measureResult = getResources().getStringArray(R.array.ecg_measureres);
		//心电测量
		tv_MSG = (TextView) findViewById(R.id.main_pc80B_MSG);
		//心电波形增益
		tv_Gain = (TextView) findViewById(R.id.main_pc80B_title_gain);
		//心率
		tv_HR = (TextView) findViewById(R.id.main_pc80B_title_hr);
		//电池
		img_Battery = (ImageView) findViewById(R.id.main_pc80B_title_battery);
		//
		img_Smooth = (ImageView) findViewById(R.id.main_pc80B_title_smooth);
		img_Pulse = (ImageView) findViewById(R.id.main_pc80B_title_pulse);
		//drawRunable = (DrawThreadPC80B) findViewById(R.id.main_pc80B_view_draw);
		//drawBG = (BackGround) findViewById(R.id.main_pc80B_view_bg);
		mTimer = (TextView)findViewById(R.id.mTimer);
		//drawRunable.setmHandler(mHandler);
		//断开连接监听
		IntentFilter filter = new IntentFilter();
		filter.addAction(ReceiveService.ACTION_BLU_DISCONNECT);
		registerReceiver(receiver, filter);
	}
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ReceiveService.ACTION_BLU_DISCONNECT)) {
				Toast.makeText(MainPC80B.this, R.string.connect_connect_off, Toast.LENGTH_SHORT).show();
			}
		}
	};


	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case StaticReceive.MSG_DATA_BATTERY: {
					setBattery(msg.arg1);
				}
				break;
				case BATTERY_ZERO: {// 电池电量为0时的消息
					if (img_Battery.isShown()) {
						img_Battery.setVisibility(View.INVISIBLE);
					} else {
						img_Battery.setVisibility(View.VISIBLE);
					}
					mHandler.sendEmptyMessageDelayed(BATTERY_ZERO, 500);
				}
				break;
				case StaticReceive.MSG_DATA_ECG_STATUS_CH: {
					switch (msg.arg1) {
						case StatusMsg.FILE_TRANSMIT_START: {// 接收文件
							setMSG(getResources().getString(
									R.string.measure_ecg_file_ing));
						}
						break;
						case StatusMsg.FILE_TRANSMIT_SUCCESS: {
							setMSG(getResources().getString(
									R.string.measure_ecg_file_end));
						}
						break;
						case StatusMsg.FILE_TRANSMIT_ERROR: {
							setMSG(getResources().getString(
									R.string.measure_ecg_time_err));
						}
						break;
						case StaticReceive.MSG_DATA_TIMEOUT: {
							setMSG(getResources().getString(
									R.string.measure_ecg_time_out));
						}
						break;
						case 4: {// 准备阶段波形
							//handler.postDelayed(runnable, 1);
							//handler.post(runnable);
//							if (drawRunable.isPause()) {
//								drawRunable.Continue();
//							}
							Bundle data = msg.getData();
							if (data.getBoolean("bLeadoff")) {
								setMSG(getResources().getString(
										R.string.measure_lead_off));
							} else {
								setMSG(" ");
							}
							setGain(data.getInt("nGain"));
							tv_MSG.setText("心电测试正在准备阶段");
						}
						break;
						case 5: {// 实时测量波形
							temp = temp+1;
							timeStamp = true;
							if(timeStamp&&temp==1){
								mTimer.setVisibility(View.VISIBLE);
								mc = new MyCount(30000, 1000);
								mc.start();
							}
//							if (drawRunable.isPause()) {
//								drawRunable.Continue();
//							}
							Bundle data = msg.getData();
							tempBundle = data;
							handler.post(runnable);
							if (data.getBoolean("bLeadoff")) {
								setMSG(getResources().getString(
										R.string.measure_lead_off));
							} else {
								setMSG("请保持测量状态，正在测量中...");
							}
							data.getInt("nTransMode");
							setHR(data.getInt("nHR"));
							setGain(data.getInt("nGain"));
							//tv_MSG.setText("请保持测量状态，正在测量中...");
						}
						break;
						case 6: {// 测量结果
//							drawRunable.Pause();
							handler.removeCallbacks(runnable);
							Bundle data = msg.getData();
							nTransMode = data.getInt("nTransMode");
							String time = data.getString("time");
							if (nTransMode == StatusMsg.TRANSMIT_MODE_QUICK && time != null) {
								setMSG(measureResult[data.getInt("nResult")]);
							} else {
								setMSG("");
							}
							//drawRunable.cleanWaveData();
							//drawRunable.Pause();
							//setGain(0);
							setHR(data.getInt("nHR"));
							setSmooth(false);
						}
						break;
						case 7: {// 传输设置
							int nSmoothingMode = msg.arg2;// 滤波模式
							nTransMode = (Integer) msg.obj;// 传输模式
							if (nTransMode == StatusMsg.TRANSMIT_MODE_FILE) {
								setMSG(getResources().getString(
										R.string.measure_ecg_file_ing));
							} else if (nTransMode == StatusMsg.TRANSMIT_MODE_CONTINUOUS) {
								setMSG("");
								setSmooth(nSmoothingMode == StatusMsg.SMOOTHMODE_ENHANCE);
							}
						}
						break;
					}
				}
				break;
				case StaticReceive.MSG_DATA_PULSE: {
					showPulse(true);
				}
				break;
				case RECEIVEMSG_PULSE_OFF: {
					showPulse(false);
				}
				break;
			}
		}

	};
	/*定义一个倒计时的内部类*/
	class MyCount extends CountDownTimer {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			mTimer.setVisibility(View.GONE);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mTimer.setText(Integer.toString((int) millisUntilFinished / 1000));
		}
	}
	/** 取消搏动标记 */
	public static final int RECEIVEMSG_PULSE_OFF = 0x115;
	/**
	 * 设置搏动标记
	 */
	private void showPulse(boolean isShow) {
		if (isShow) {
			img_Pulse.setVisibility(View.VISIBLE);
			new Thread() {
				@Override
				public void run() {
					super.run();
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mHandler.sendEmptyMessage(RECEIVEMSG_PULSE_OFF);
				}
			}.start();
		} else {
			img_Pulse.setVisibility(View.INVISIBLE);
		}
	}
	/**
	 * 当前数据传输模式
	 */
	private int nTransMode = 0;
	@Override
	protected void onResume() {
		super.onResume();
//		if (drawThread == null) {
//			drawThread = new Thread(drawRunable, "DrawPC80BThread");
//			drawThread.start();
//		}
//		if (drawRunable.isPause()) {
//			drawRunable.Continue();
//		}

//		Intent i = new Intent(this, ConnectActivity.class);
//		i.putExtra("device", 3);
//		startActivityForResult(i, 0x100);
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if (drawThread != null && !drawRunable.isPause()) {
//			drawRunable.Pause();
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sendBroadcast(new Intent(ReceiveService.BLU_ACTION_DISCONNECT));
//		if (!drawRunable.isStop()) {
//			drawRunable.Stop();
//		}
		drawThread = null;
		stopService(new Intent(this, ReceiveService.class));
		unregisterReceiver(receiver);
	}

	/** 消息 电池电量为0 */
	private static final int BATTERY_ZERO = 0x302;
	private void setBattery(int battery) {
		setImgResource(img_Battery, batteryRes[battery]);
		if (battery == 0) {
			if (!mHandler.hasMessages(BATTERY_ZERO)) {
				mHandler.sendEmptyMessage(BATTERY_ZERO);
			}
		} else {
			img_Battery.setVisibility(View.VISIBLE);
			mHandler.removeMessages(BATTERY_ZERO);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (MyBluetoooth.isConnected) {
			menu.add("断开连接");
		} else {
			menu.add("连接设备");
		}
		return super.onCreateOptionsMenu(menu);
	}
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		menu.clear();
//		if (MyBluetoooth.isConnected) {
//			menu.add("断开连接");
//		} else {
//			menu.add("连接设备");
//		}
//		return super.onPrepareOptionsMenu(menu);
//	}

	//蓝牙设备连接
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (MyBluetoooth.isConnected) {
			sendBroadcast(new Intent(ReceiveService.BLU_ACTION_DISCONNECT));
		} else {
			Intent i = new Intent(this, ConnectActivity.class);
			i.putExtra("device", 3);
			startActivityForResult(i, 0x100);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0x100) {
			StaticReceive.setmHandler(mHandler);
		}
	}
	/**
	 * 设置滤波模式
	 *
	 * @param isVisible
	 */
	private void setSmooth(boolean isVisible) {
		setImgVisible(img_Smooth, isVisible);
	}
	/**
	 * 设置图片
	 *
	 * @param img
	 * @param res
	 */
	private void setImgResource(ImageView img, int res) {
		if (!img.isShown()) {
			img.setVisibility(View.VISIBLE);
		}
		img.setImageResource(res);
	}
	private void setImgVisible(ImageView img, boolean isVisible) {
		if (isVisible) {
			img.setVisibility(View.VISIBLE);
		} else {
			img.setVisibility(View.INVISIBLE);
		}
	}
	private void setGain(int gain) {
		System.out.println("setGain=" + gain);
		gain = gain <= 0 ? 2 : gain;
		setTVtext(tv_Gain, "x" + gain);
		//drawRunable.setGain(gain);
		//drawBG.setGain(gain);
	}

	private void setHR(int hr) {
//		if (hr != 0)
			setTVtext(tv_HR, "HR=" + hr);
//		else
//			setTVtext(tv_HR, "HR=--");
	}

	private void setMSG(String msg) {
		setTVtext(tv_MSG, msg);
	}

	/**
	 * 设置TextView显示的内容
	 */
	private void setTVtext(TextView tv, String msg) {
		if (tv != null) {
			if (!tv.isShown()) {
				tv.setVisibility(View.VISIBLE);
			}
			if (msg != null && !msg.equals("")) {
				if (msg.equals("0")) {
					tv.setText(getResources().getString(
							R.string.const_data_nodata));
				} else {
					tv.setText(msg);
				}
			}
		}
	}

}