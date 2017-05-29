package bjtu.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bjtu.deJson.OrderDeJson;
import bjtu.deJson.WaybillDeJson;
import bjtu.model.Order;
import bjtu.model.Waybill;
import bjtu.network.HttpClient;
import bjtu.util.Config;

public class OrderController {
    private HttpClient httpClient = new HttpClient();
    private OrderDeJson orderDeJson = new OrderDeJson();
    private WaybillDeJson waybillDeJson = new WaybillDeJson();

    //获取可抢订单order
    public List<Order> getOrdersByStatus(){
        List<Order> orderList1 = new ArrayList<>();
        List<Order> orderList2 = new ArrayList<>();
        String method = "GET";
        //获取订单部分的信息 status = 1
        String orderUrl1 = Config.developmentHost+"orders/getOrdersByStatus?order[status]=1";
        String result1 = httpClient.doPost(orderUrl1,method);
        orderList1 = orderDeJson.deJson(result1);
        //获取订单部分的信息 status = 5
        String orderUrl2 = Config.developmentHost+"orders/getOrdersByStatus?order[status]=5";
        String result2 = httpClient.doPost(orderUrl2,method);
        orderList2 = orderDeJson.deJson(result2);
        System.out.print(orderList2.size()+"        "+Config.getFactory_id()+"\n-------------------------");
        System.out.print(orderList1.size()+"        "+Config.getFactory_id());
        for(int i = orderList2.size()-1;i>=0;i--){
            if(orderList2.get(i).getFactory_id() != Config.getFactory_id()){
                orderList2.remove(i);
            }
        }
        for(int i = orderList1.size()-1;i>=0;i--){
            if(orderList1.get(i).getFactory_id() != Config.getFactory_id()){
                orderList1.remove(i);
            }
        }
        orderList2.addAll(orderList1);
        //根据order_id获取订单对应运单部分的信息
        for (int i=0;i<orderList2.size();i++){
            String waybillUrl = Config.developmentHost+"waybills/getWaybillByStatus?waybill[order_id]="+orderList2.get(i).getId()
                    +"&waybill[recieve_type]=1";
            httpClient.setResult("");
            String waybillResult = httpClient.doPost(waybillUrl,"GET");
            Waybill waybill = waybillDeJson.deJson(waybillResult);
            orderList2.get(i).setWaybill(waybill);
        }
        return orderList2;
    }

    //抢单，修改和补充订单、运单的信息，创建新的运单信息
    public void modifyOrderAndWaybill(Order order,int courier_id){
        String modifyMethod = "PUT";
        String addMethod = "POST";
        //修改订单--courier_id,factory_id,status=>下一个状态 1->2  5->6
        String modifyOrderUrl = Config.developmentHost+"orders/"+order.getId()+"?order[status]="
                +String.valueOf(order.getStatus()+1)+"&order[courier_id]="+courier_id+"&order[factory_id]="+Config.getFactory_id();
        httpClient.setResult("");
        httpClient.doPost(modifyOrderUrl,modifyMethod);
        //修改运单--status=>下一个状态，actual_time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currTime = new Date();
        String modifyWaybillUrl = Config.developmentHost+"waybills/"+order.getWaybill().getId()
                +"?waybill[status]="+String.valueOf(order.getWaybill().getStatus()+1)
                +"&waybill[actual_time]="+sdf.format(currTime)
                +"&waybill[recieve_id]="+courier_id
                +"&waybill[recieve_type]=1";    //取送人员
        httpClient.doPost(modifyWaybillUrl,modifyMethod);
        //创建新的运单
        Calendar cal = Calendar.getInstance();
        cal.setTime(currTime);
        cal.add(Calendar.HOUR_OF_DAY,2);
        String expTime = sdf.format(cal.getTime());
        String recieve_type = "";
        if(order.getStatus() == 1){  //订单在1-待抢单状态，抢单后，接收类型为3-工厂
            recieve_type = "3";
        }else if(order.getStatus() == 5){  //订单在5-清洗完成状态，抢单后，接收类型为0-用户
            recieve_type = "0";
        }
        String createUrl = Config.developmentHost+"waybills?waybill[sender_type]=1&waybill[status]="
                +String.valueOf(order.getWaybill().getStatus()+1)+"&waybill[exp_time]="+expTime
                +"&waybill[waybill_id]="+order.getWaybill().getId()+"&waybill[order_id]="+order.getId()
                +"&waybill[recieve_type]="+recieve_type;
        httpClient.setResult("");
        httpClient.doPost(createUrl,addMethod);
    }

    //获取自己已经抢到的订单
    public List<Order> getOrdersByCourierId(int courier_id){
        List<Order> orderList = new ArrayList<>();
        //获取订单部分的信息
        String orderUrl = Config.developmentHost+"orders/getOrdersByCourierId?order[courier_id]="+courier_id;
        String method = "GET";
        String result = httpClient.doPost(orderUrl,method);
        orderList = orderDeJson.deJson(result);
        //根据order_id获取订单对应运单部分的信息
        for (int i=0;i<orderList.size();i++){
            Order order = orderList.get(i);
            String recieve_type = "";
            if(order.getStatus() == 2){  //送往站点
                recieve_type = "3";
            }else if(order.getStatus() == 6){  //送回用户
                recieve_type = "0";
            }
            String waybillUrl = Config.developmentHost+"waybills/getWaybillByStatus?waybill[order_id]="+orderList.get(i).getId()
                    +"&waybill[recieve_type]="+recieve_type;
            httpClient.setResult("");
            String waybillResult = httpClient.doPost(waybillUrl,"GET");
            Waybill waybill = waybillDeJson.deJson(waybillResult);
            orderList.get(i).setWaybill(waybill);
        }
        return orderList;
    }

    //确认送达
    public String conform (Order order,int courier_id){
        String modifyMethod = "PUT";
        String modifyMeterMethod = "POST";
        String returnStr = "失败";
        switch (order.getStatus())   {
            case 2: //确认送达
                //修改Order状态,
                //修改waybill状态
                //创建新的运单
                int recievetype = 3;
                //获取对应的工厂id
                int recieveId = 0;
                if(modifyOrder(order,courier_id,3) && modifyWaybills(order,courier_id)
                        && createWaybills(order,courier_id, recievetype, recieveId)){
                    returnStr = "已从用户取件";
                }
                break;
            case 5:
                //修改Order状态,
                //修改waybill状态
                //创建新的运单
                int recievetype1 = 0;
                int recieveId1 = order.getUser_id();
                if(modifyOrder(order,courier_id, 6) && modifyWaybills(order,courier_id)
                        && createWaybills(order,courier_id, recievetype1, recieveId1)){
                    returnStr = "已从工厂取件";
                }
                break;
            case 3:
                //修改计量
                System.out.print("order status = "+ order.getStatus());
                if(order.getStatus() == 3 || order.getStatus() == 6){
                    String modifySettleCountURl = Config.developmentHost+"settlements/meter?settlement[courier_id]="+courier_id;
                    System.out.print("134444444444444444444444444444444444");
                    httpClient.setResult("");
                    httpClient.doPost(modifySettleCountURl,modifyMeterMethod);
                }
                //修改Order状态,
                //修改waybill状态
                if((modifyOrder(order,courier_id,4) && modifyWaybills(order,courier_id))) {
                    returnStr = "已送达站点";
                }
                break;
            case 6:
                //修改计量
                if(order.getStatus() == 3 || order.getStatus() == 6){
                    String modifySettleCountURl = Config.developmentHost+"settlements/meter?settlement[courier_id]="+courier_id;
                    httpClient.setResult("");
                    httpClient.doPost(modifySettleCountURl,modifyMeterMethod);
                }
                //修改Order状态,
                //修改waybill状态
                if((modifyOrder(order,courier_id,7) && modifyWaybills(order,courier_id))) {
                    returnStr = "已送达用户";
                }
                break;
        }
        return returnStr;
    }

    //修改Order状态
    public boolean modifyOrder(Order order,int courier_id, int status){
        String modifyMethod = "PUT";
        String addMethod = "POST";
        String modifyOrderUrl = Config.developmentHost+"orders/"+order.getId()
                +"?order[status]="+status;
        httpClient.setResult("");
        httpClient.doPost(modifyOrderUrl,modifyMethod);
        return true;
    }

    //修改waybill状态
    public boolean modifyWaybills(Order order,int courier_id){
        String modifyMethod = "PUT";
        String addMethod = "POST";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currTime = new Date();
        String modifyWaybillUrl = Config.developmentHost+"waybills/"+order.getWaybill().getId()
                +"&waybill[actual_time]="+sdf.format(currTime);
        httpClient.doPost(modifyWaybillUrl,modifyMethod);
        return true;
    }

    //创建新的运单
    public boolean createWaybills(Order order,int courier_id, int recievetype, int recieveId){
        String modifyMethod = "PUT";
        String addMethod = "POST";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currTime);
        cal.add(Calendar.HOUR_OF_DAY,2);
        String expTime = sdf.format(cal.getTime());
        String createUrl = Config.developmentHost+"waybills?waybill[sender_type]=1&waybill[status]="
                +String.valueOf(order.getWaybill().getStatus()+1)+"&waybill[exp_time]="+expTime
                +"&waybill[waybill_id]="+order.getWaybill().getId()+"&waybill[order_id]="+order.getId()
                +"&waybill[recieve_type]="+recievetype+"&waybill[recieve_id]="+recieveId;
        httpClient.setResult("");
        httpClient.doPost(createUrl,addMethod);
        return true;
    }

    //修改计量

}
