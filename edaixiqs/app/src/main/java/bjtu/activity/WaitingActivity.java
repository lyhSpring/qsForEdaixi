package bjtu.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bjtu.adapter.OrderWaitingListViewAdapter;
import bjtu.controller.OrderController;
import bjtu.model.Order;

public class WaitingActivity extends AppCompatActivity {
    //申明控件
    private TextView hintTextView;
    private ListView orderListQSView;

    private OrderController orderController = new OrderController();
    private List<Order> dataList = new ArrayList<>();
    private OrderWaitingListViewAdapter adapter ;

    private int courier_id = 0;
    private String region = "海淀区";
    private int region_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Intent intent = this.getIntent();
        courier_id = intent.getIntExtra("userID",0);

        //初始化控件
        hintTextView = (TextView) findViewById(R.id.hintForNoOrders);
        orderListQSView = (ListView) findViewById(R.id.orderListQSView);
        //获取订单数据
        Runnable getOrdersTask = new Runnable() {
            @Override
            public void run() {
                dataList = orderController.getOrdersByStatus();
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        };
        Thread getOrdersThread = new Thread(getOrdersTask);
        getOrdersThread.start();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Toast.makeText(WaitingActivity.this,String.valueOf(msg.obj),Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    //显示界面中的控件
                    if(dataList.size() <= 0){
                        hintTextView.setVisibility(View.VISIBLE);
                        orderListQSView.setVisibility(View.INVISIBLE);
                    }else{
                        adapter = new OrderWaitingListViewAdapter(WaitingActivity.this,dataList);
                        orderListQSView.setAdapter(adapter);
                        orderListQSView.setOnItemClickListener(new catchOrderListener());
                    }
                    break;
            }
        }
    };

    class catchOrderListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            //抢单，修改和补充订单、运单的信息
            Runnable modifyInfoTask = new Runnable() {
                @Override
                public void run() {
                    orderController.modifyOrderAndWaybill(dataList.get(position),courier_id);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = "抢单成功";
                    handler.sendMessage(msg);
                }
            };
            Thread modifyInfoThread = new Thread(modifyInfoTask);
            modifyInfoThread.start();
        }
    }
}
