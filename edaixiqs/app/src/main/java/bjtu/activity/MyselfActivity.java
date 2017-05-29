package bjtu.activity;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;

import bjtu.adapter.OrderWaitingListViewAdapter;
import bjtu.controller.CourierController;
import bjtu.deJson.CourierDeJson;

public class MyselfActivity extends AppCompatActivity {

    private PieChart meterChart;
    private PieChart valuateChart;

    private CourierDeJson courierDeJson = new CourierDeJson();
    private CourierController courierController = new CourierController();
    private int settled = 0;
    private int unsettled = 0;
    private int money_settled = 0;
    private int money_unsettled = 0;
    private static String meterDesc = "结算数量";
    private static String valuteDesc = "结算金额";
    private static String settled_str = "未结算数量";
    private static String unsettled_str = "已结算数量";
    private static String money_settled_str = "未结算金额";
    private static String money_unsettled_str = "已结算金额";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself);

        meterChart = (PieChart) findViewById(R.id.meter);
        valuateChart = (PieChart) findViewById(R.id.valuate);
        Runnable getSettlementTask = new Runnable() {
            @Override
            public void run() {
                String settlementInfo = courierController.getSettledInfo();
                settled = courierDeJson.parseInt(settlementInfo,"settled");
                unsettled = courierDeJson.parseInt(settlementInfo,"unsettled");
                money_settled = courierDeJson.parseInt(settlementInfo,"money_settled");
                money_unsettled = courierDeJson.parseInt(settlementInfo,"money_unsettled");
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        };
        Thread getSettlementThread = new Thread(getSettlementTask);
        getSettlementThread.start();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    PieData meterPieData = getPieData(settled,unsettled,settled_str,unsettled_str,meterDesc);
                    PieData valuatePieData = getPieData(money_settled,money_unsettled,money_settled_str,money_unsettled_str,valuteDesc);
                    showChart(meterChart,meterPieData,meterDesc);
                    showChart(valuateChart,valuatePieData,valuteDesc);
                    break;
            }
        }
    };

    private void showChart(PieChart pieChart, PieData pieData,String descStr) {
        pieChart.setHoleColorTransparent(true);

        pieChart.setHoleRadius(60f);  //半径
        pieChart.setTransparentCircleRadius(64f); // 半透明圈
        //pieChart.setHoleRadius(0)  //实心圆

        pieChart.setDescription(descStr);

        // mChart.setDrawYValues(true);
        pieChart.setDrawCenterText(true);  //饼状图中间可以添加文字

        pieChart.setDrawHoleEnabled(true);

        pieChart.setRotationAngle(90); // 初始旋转角度

        // draws the corresponding description value into the slice
        // mChart.setDrawXValues(true);

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true); // 可以手动旋转

        // display percentage values
        pieChart.setUsePercentValues(false);  //显示成百分比
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
//      mChart.setOnChartValueSelectedListener(this);
        // mChart.setTouchEnabled(false);

//      mChart.setOnAnimationListener(this);

        pieChart.setCenterText(descStr);  //饼状图中间的文字

        //设置数据
        pieChart.setData(pieData);

        // undo all highlights
//      pieChart.highlightValues(null);
//      pieChart.invalidate();

        Legend mLegend = pieChart.getLegend();  //设置比例图
        mLegend.setPosition(LegendPosition.RIGHT_OF_CHART);  //最右边显示
//      mLegend.setForm(LegendForm.LINE);  //设置比例图的形状，默认是方形
        mLegend.setXEntrySpace(7f);
        mLegend.setYEntrySpace(5f);

        pieChart.animateXY(1000, 1000);  //设置动画
        // mChart.spin(2000, 0, 360);
    }

    private PieData getPieData(int settled,int unsettled,String kind1,String kind2,String desc) {
        ArrayList<String> xValues = new ArrayList<String>();  //xVals用来表示每个饼块上的内容
        xValues.add(0,kind1);
        xValues.add(1,kind2);
        ArrayList<Entry> yValues = new ArrayList<Entry>();  //yVals用来表示封装每个饼块的实际数据
        // 饼图数据
        /**
         * 将一个饼形图分成四部分， 四部分的数值比例为14:14:34:38
         * 所以 14代表的百分比就是14%
         */
//        float quarterly1 = 14;
//        float quarterly2 = 14;
//        float quarterly3 = 34;
//        float quarterly4 = 38;

        yValues.add(new Entry(unsettled, 0));
        yValues.add(new Entry(settled, 1));
//        yValues.add(new Entry(quarterly3, 2));
//        yValues.add(new Entry(quarterly4, 3));
        //y轴的集合
        PieDataSet pieDataSet = new PieDataSet(yValues, desc/*显示在比例图上*/);
        pieDataSet.setSliceSpace(0f); //设置个饼状图之间的距离

        ArrayList<Integer> colors = new ArrayList<Integer>();

        // 饼图颜色
        colors.add(Color.rgb(205, 205, 205));   //#cdcdcd 灰
//        colors.add(Color.rgb(114, 188, 223));   //#72bcdf 浅蓝
        colors.add(Color.rgb(255, 123, 124));   //#ff7b7c 红
//        colors.add(Color.rgb(57, 135, 200));    //#3987c8 深蓝

        pieDataSet.setColors(colors);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = 5 * (metrics.densityDpi / 160f);
        pieDataSet.setSelectionShift(px); // 选中态多出的长度
        PieData pieData = new PieData(xValues, pieDataSet);
        return pieData;
    }

}
