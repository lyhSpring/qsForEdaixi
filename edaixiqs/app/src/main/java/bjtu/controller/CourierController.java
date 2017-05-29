package bjtu.controller;

import bjtu.deJson.CourierDeJson;
import bjtu.model.Courier;
import bjtu.network.HttpClient;
import bjtu.util.Config;

public class CourierController {
    private HttpClient httpClient = new HttpClient();
    private CourierDeJson courierDeJson = new CourierDeJson();

    //login
    public Courier login(String username,String password){
        //获取快递员信息
        Courier courier;
        String url = Config.developmentHost+"couriers/login?courier[mobile]="+username+"&courier[password]="+password;
        String method = "GET";
        String result = httpClient.doPost(url,method);
        courier = courierDeJson.deJson(result);
        int station_id = courier.getStation_id();
        Config.setSettlement_id(courierDeJson.parseSettlementId(result));

        //获取station信息
        String stationMethod = "GET";
        String stationUrl = Config.developmentHost+"stations/"+station_id;
        httpClient.setResult("");
        String stationResult = httpClient.doPost(stationUrl,stationMethod);
        int factory_id = courierDeJson.parseInt(stationResult,"id");
        int region_id = courierDeJson.parseInt(stationResult,"region_id");
        Config.setFactory_id(factory_id);
        Config.setRegion_id(region_id);
        return courier;
    }

    //获取该快递员结算情况
    public String getSettledInfo(){
        String method = "GET";
        String url = Config.developmentHost+"settlements/"+Config.getSettlement_id();
        httpClient.setResult("");
        return httpClient.doPost(url,method);
    }
}
