package com.infinityinfoway.sfsampleapp.api;


import com.infinityinfoway.sfsampleapp.config.Config;

public class APIsConnectivity {
    private final String location_api_url = Config.location_api_url;

    public String xmlEnvelope(String params) {
        return ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                //+<soap:Envelope xmlns:ns="http://192.168.0.46" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">

                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body>" + params + "</soap:Body>" + "</soap:Envelope>";
    }


    //==============================================================================
    // insert_location
//==============================================================================
    public String insert_location_v3(String name, String LocationAddress, String mobile_no, String image, String image_name,
                                     long image_size, String image_latitude, String image_longitude, String location_latitude, String location_longitude,
                                     int image_flag, String shop_mobile_no, int AppVersion, String AndroidId, int is_GPS_on, int is_internet_on, int battery_percentage) {
        return xmlEnvelope(""
                + "<insert_location_v3 xmlns=\"" + location_api_url + "\">"
                + "<app_version>" + AppVersion + "</app_version>"
                + "<android_id>" + AndroidId + "</android_id>"
                + "<device_id>" + 0 + "</device_id>"
                + "<user_id>" + 0 + "</user_id>"
                + "<name>" + name + "</name>"
                + "<address>" + LocationAddress + "</address>"
                + "<mobile_no>" + mobile_no + "</mobile_no>"
                + "<image>" + image + "</image>"
                + "<image_name>" + image_name + "</image_name>"
                + "<image_size>" + image_size + "</image_size>"
                + "<image_latitude>" + image_latitude + "</image_latitude>"
                + "<image_longitude>" + image_longitude + "</image_longitude>"
                + "<location_latitude>" + location_latitude + "</location_latitude>"
                + "<location_longitude>" + location_longitude + "</location_longitude>"
                + "<image_flag>" + image_flag + "</image_flag>"
                + "<shop_mobile_no>" + shop_mobile_no + "</shop_mobile_no>"
                + "<is_GPS_on>" + is_GPS_on + "</is_GPS_on>"
                + "<is_internet_on>" + is_internet_on + "</is_internet_on>"
                + "<battery_percentage>" + battery_percentage + "</battery_percentage>"
                + "</insert_location_v3>");
    }

    //==============================================================================
    // insert_location
//==============================================================================
    public String insert_location_shrink(int app_version, String android_id, int device_id, int user_id, String mobile_no, String jsonString) {
        return xmlEnvelope(""
                + "<insert_location_shrink xmlns=\"" + location_api_url + "\">"
                + "<app_version>" + app_version + "</app_version>"
                + "<android_id>" + android_id + "</android_id>"
                + "<device_id>" + device_id + "</device_id>"
                + "<user_id>" + user_id + "</user_id>"
                + "<mobile_no>" + mobile_no + "</mobile_no>"
                + "<jsonString>" + jsonString + "</jsonString>"
                + "</insert_location_shrink>");
    }


}