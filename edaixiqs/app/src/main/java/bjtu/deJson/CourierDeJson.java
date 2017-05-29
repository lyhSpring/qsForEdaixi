package bjtu.deJson;

import org.json.JSONObject;

import bjtu.model.Courier;

/**
 * Created by 李奕杭_lyh on 2017/5/16.
 */

public class CourierDeJson {

    public Courier deJson(String jsonStr){
        Courier courier = new Courier();
        try{
            JSONObject jsonObject = new JSONObject(jsonStr);
            courier.setId(jsonObject.getInt("id"));
            courier.setName(jsonObject.getString("name"));
            courier.setStatus(jsonObject.getInt("status"));
            courier.setEmail(jsonObject.getString("email"));
            courier.setMobile(jsonObject.getString("mobile"));
            courier.setPassword(jsonObject.getString("password"));
            courier.setStation_id(jsonObject.getInt("station_id"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return courier;
    }

    public int parseInt(String jsonStr,String index){
        int parseResult = 0 ;
        try{
            JSONObject jsonObject = new JSONObject(jsonStr);
            parseResult = jsonObject.getInt(index);
        }catch (Exception e){
            e.printStackTrace();
        }
        return parseResult;
    }

    public int parseSettlementId(String jsonStr){
        int settlement_id = 0;
        try{
            JSONObject jsonObject = new JSONObject(jsonStr);
            String settlement = jsonObject.getString("settlement");
            JSONObject jo = new JSONObject(settlement);
            settlement_id = jo.getInt("id");
        }catch (Exception e){
            e.printStackTrace();
        }
        return settlement_id;
    }
}
