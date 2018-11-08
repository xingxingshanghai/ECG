package com.example.bluetooth.app;

/**
 * Created by Administrator on 2017/11/3.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

public class ChartService {

    private GraphicalView mGraphicalView;
    private XYMultipleSeriesDataset multipleSeriesDataset;// 数据集容器
    private XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();;// 渲染器容器
    private XYSeries mSeries;// 单条曲线数据集
    private XYSeriesRenderer mRenderer;// 单条曲线渲染器
    private Context context;
    private int i=0;
    public ChartService(Context context) {
        this.context = context;
    }
    public GraphicalView getGraphicalView() {
        mGraphicalView = ChartFactory.getCubeLineChartView(context, multipleSeriesDataset, multipleSeriesRenderer, 0.3f);
        return mGraphicalView;
    }
    /**
     * 获取数据集，及xy坐标的集合
     *
     * @param curveTitle
     */
    public void setXYMultipleSeriesDataset(String curveTitle) {
        multipleSeriesDataset = new XYMultipleSeriesDataset();
        mSeries = new XYSeries("信号实时传输");
        multipleSeriesDataset.addSeries(mSeries);
    }
    /**
     * 获取渲染器
     *
     * @param maxX
     *            x轴最大值
     * @param maxY
     *            y轴最大值
     * @param chartTitle
     *            曲线的标题
     * @param xTitle
     *            x轴标题
     * @param yTitle
     *            y轴标题
     * @param axeColor
     *            坐标轴颜色
     * @param labelColor
     *            标题颜色
     * @param curveColor
     *            曲线颜色
     * @param gridColor
     *            网格颜色
     */
    public void setXYMultipleSeriesRenderer(double maxX, double maxY,
                                            String chartTitle, String xTitle, String yTitle, int axeColor,
                                            int labelColor, int curveColor, int gridColor) {
        //multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);
        }
        multipleSeriesRenderer.setXTitle(xTitle);
        multipleSeriesRenderer.setYTitle(yTitle);
        multipleSeriesRenderer.setRange(new double[] { 0, maxX, -1, maxY });//xy轴的范围
        multipleSeriesRenderer.setLabelsColor(labelColor);
        multipleSeriesRenderer.setXLabels(20);
        multipleSeriesRenderer.setYLabels(10);
        multipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setAxisTitleTextSize(20);
        multipleSeriesRenderer.setChartTitleTextSize(20);
        multipleSeriesRenderer.setLabelsTextSize(10);
        multipleSeriesRenderer.setLegendTextSize(20);
        //multipleSeriesRenderer.setPointSize(1f);//曲线描点尺寸
        multipleSeriesRenderer.setFitLegend(true);
       // multipleSeriesRenderer.setMargins(new int[] { 20（上）, 30(左), 15(下), 20(右) });
        multipleSeriesRenderer.setMargins(new int[] { 30, 30, 25, 20 });
        multipleSeriesRenderer.setShowGrid(true);
        //multipleSeriesRenderer.setShowGridY(true);
        multipleSeriesRenderer.setZoomButtonsVisible(true);
        //设置x轴缩放，y轴不缩放
        multipleSeriesRenderer.setZoomEnabled(true, false);
        //设置坐标轴的颜色
        multipleSeriesRenderer.setAxesColor(axeColor);
        //设置移动
       // multipleSeriesRenderer.setPanEnabled(false);
        //设置x轴可移动，y轴不移动
        multipleSeriesRenderer.setPanEnabled(true,false);
        //设置缩放范围
//        multipleSeriesRenderer.setZoomLimits(new double[] { 0, 4500, -2, 4 });//设置缩放的范围
//        multipleSeriesRenderer.setPanLimits(new double[] { 0, 4500, -2, 4 });//设置拉动的范围
        //设置网格的颜色
        multipleSeriesRenderer.setGridColor(gridColor);
        multipleSeriesRenderer.setApplyBackgroundColor(true);
        //multipleSeriesRenderer.setBackgroundColor(Color.GRAY);//背景色
        multipleSeriesRenderer.setBackgroundColor(Color.TRANSPARENT);

        //multipleSeriesRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
        multipleSeriesRenderer.setMarginsColor(Color.parseColor("#ff4bb2f5"));//边距背景色，默认背景色为黑色，这里修改为白色
        //曲线参数配置
        //legend显示与否
        multipleSeriesRenderer.setShowLegend(false);
        //单条曲线渲染器
        mRenderer = new XYSeriesRenderer();
        mRenderer.setColor(Color.GREEN);
        mRenderer.setHighlighted(true);
        //mRenderer.setFillPoints(true);
        mRenderer.setLineWidth(3f);
        mRenderer.setPointStyle(PointStyle.POINT);//描点风格，可以为圆点，方形点等等
        //追加曲线
        multipleSeriesRenderer.addSeriesRenderer(mRenderer);
    }
    protected void updateCharts(double datas[], int MAX_POINT) {

        for(int j =0;j<datas.length;j++)
        {
            mSeries.add(i, datas[j]);
            i++;
        }
//        for (double addY : datas) {
//            mSeries.add(i, addY);
//            i++;
//        }
        if (i < MAX_POINT) {
            multipleSeriesRenderer.setXAxisMin(0);
            multipleSeriesRenderer.setXAxisMax(MAX_POINT);
        } else {
            multipleSeriesRenderer.setXAxisMin(mSeries.getItemCount() - MAX_POINT);
            multipleSeriesRenderer.setXAxisMax(mSeries.getItemCount());
        }
        mGraphicalView.repaint();
    }
}