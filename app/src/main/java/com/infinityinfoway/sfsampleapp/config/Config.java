package com.infinityinfoway.sfsampleapp.config;

public class Config {

    public static final String PACKAGE_NAME = "com.infinityinfoway.sfsampleapp";
    public static final String APP_NAME = "SfSampleApp";
    public static final String db_name = APP_NAME;
    public static final int db_version = 1;

    public static final String location_api_url = "http://sf.iipl.info";
    //	public static final String location_api_url = "http://192.168.20.62";
    public static final String location_str_SOAPActURL = location_api_url + "/";
    public static final String location_resStr_URL = location_str_SOAPActURL + "imobileTestHandler_V3.asmx";
}
