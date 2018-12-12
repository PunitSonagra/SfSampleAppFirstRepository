package com.infinityinfoway.sfsampleapp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.infinityinfoway.sfsampleapp.api.APIsConnectivity;
import com.infinityinfoway.sfsampleapp.api.DataConnectivity;
import com.infinityinfoway.sfsampleapp.database.DBConnector;
import com.infinityinfoway.sfsampleapp.database.SharedPref;
import com.infinityinfoway.sfsampleapp.model.GPSMasterBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataSendingService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 30;
    private static final long FASTEST_INTERVAL = 1000 * 15;
    private Location curLocation;

    private int PLAY_SERVICES_RESOLUTION_REQUEST = 502;

    private Context context;
    //	 AppLocationService gps;
    private String[] LocationArray;
    private String LocationAddress = "";

    private int Status;
    private String StatusMessage;
    APIsConnectivity getAPI = new APIsConnectivity();
    DataConnectivity getData = new DataConnectivity();
    private Date today;
    ConnectionDetector cd;
    SharedPref getSharedPref;
    private LocationManager manager;
    private boolean statusOfGPS;
    private DBConnector dbConnector;
    private IntentFilter ifilter;
    private Intent batteryStatus;
    private SimpleDateFormat sdf;

    public void createAlarm(){
        Calendar updateTime = Calendar.getInstance();

        updateTime.set(Calendar.SECOND, 5);

        Intent alarmIntent = new Intent(context, DataSendingReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(recurringDownload);

        //alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 1000 * 60 * getSharedPref.getLocationRequestTime(), recurringDownload); //will run it after every 5 seconds.
        getSharedPref.setAlarm(true);




        if (Build.VERSION.SDK_INT >= 23) {
// Wakes up the device in Doze Mode
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis()+1000 * 60 * getSharedPref.getLocationRequestTime(),
                    recurringDownload);
        } else if (Build.VERSION.SDK_INT >= 19) {
// Wakes up the device in Idle Mode
            alarms.setExact(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis()+1000 * 60 * getSharedPref.getLocationRequestTime(), recurringDownload);
        } else {
// Old APIs
            alarms.set(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis()+1000 * 60 * getSharedPref.getLocationRequestTime(), recurringDownload);
        }


        ComponentName receiver = new ComponentName(context, DeviceBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public DataSendingService(Context context) {
        this.context = context;
        cd = new ConnectionDetector(context);
        getSharedPref = new SharedPref(context);
        buildGoogleApiClient();
        createLocationRequest();

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus= context.registerReceiver(null, ifilter);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

//        boolean present = batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
//        String battery="";
//        if(present == true)
//        {
//            battery= "Yes";
//        }
//        else {
//            battery = "No";
//        }
//
//        String technology = batteryStatus.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
//        // Get the battery voltage
//        int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
//
//        Toast.makeText(context, "battery"+ battery, Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, ""+ batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1), Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, ""+ technology+"\n"+voltage, Toast.LENGTH_SHORT).show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void sendData() {



            if (curLocation == null) {
                curLocation = new Location(LocationManager.GPS_PROVIDER);
            }



       // if (getSharedPref.getlat()!=null && getSharedPref.getlat().length()>0 && getSharedPref.getlng()!=null && getSharedPref.getlng().length()>0 ) {

            if (getSharedPref.getlat()!=null && getSharedPref.getlat().length()>0 && getSharedPref.getlng()!=null && getSharedPref.getlng().length()>0 ) {
                GetAddress(Double.parseDouble(getSharedPref.getlat()), Double.parseDouble(getSharedPref.getlng()));
            }else{
                LocationAddress="";
            }

           if (!(cd.isConnectingToInternet())) {

            if(getSharedPref.IsLogin()){


                boolean insert_flag=false;
                dbConnector=new DBConnector(context);
                today=new Date();
                GPSMasterBean data=new GPSMasterBean();

                data.setGPS_Location_Name("unspecified");
                data.setGPS_Latitude(getSharedPref.getlat());
                data.setGPS_Longitude(getSharedPref.getlng());


                try{
                    //GetAddress(Double.parseDouble(getSharedPref.getlat()),Double.parseDouble(getSharedPref.getlng()));
                    data.setGPS_Address("");
                }catch (Exception ex){}

                try{
                    data.setGPS_Battery_Percentage(String.valueOf(calculateBatteryPercentage()));
                }catch (Exception ex){}
                data.setGPS_Internet_Status("0");
                data.setGPS_Status(String.valueOf(isGPSON()));

                try{
                    data.setGPS_DateTime(sdf.format(today));
                }catch (Exception ex){}

//                Toast.makeText(context, "old=="+getSharedPref.getlat(), Toast.LENGTH_LONG).show();

                if(data!=null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("1") &&
                        (!TextUtils.isEmpty(data.getGPS_Latitude())) && (!TextUtils.isEmpty(data.getGPS_Longitude())) &&
                        Double.parseDouble(data.getGPS_Latitude()) >0 && Double.parseDouble(data.getGPS_Longitude()) >0){
                    insert_flag= dbConnector.addGPSData(data);
                    getSharedPref.setLatLng("0.0", "0.0");
                }else if(data!=null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("0")){
                    insert_flag=dbConnector.addGPSData(data);
                    getSharedPref.setLatLng("0.0", "0.0");
                }

                /*if(insert_flag){
                    Toast.makeText(context, "new=="+getSharedPref.getlat(), Toast.LENGTH_LONG).show();
                }*/



                dbConnector.close();
                return;
            }
        }else{

            if(getSharedPref.IsLogin()){

                int GPSStatus=isGPSON();


                if(GPSStatus==1 && (!TextUtils.isEmpty(getSharedPref.getlat())) && (!TextUtils.isEmpty(getSharedPref.getlng())) &&
                        Double.parseDouble(getSharedPref.getlat()) >0 && Double.parseDouble(getSharedPref.getlng()) >0){



                    LazyDataConnection GetSources_B2CTask = new LazyDataConnection("insert_location_v3");
                    GetSources_B2CTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "insert_location_v3", getAPI.insert_location_v3(
                            "unspecified",
                            LocationAddress,
                            getSharedPref.getUserPhone(),
                            "",
                            "",
                            0,
                            "0.0",
                            "0.0",
                            getSharedPref.getlat(),
                            getSharedPref.getlng(),
                            0,
                            "",
                            getSharedPref.getAppVersionCode(),
                            getSharedPref.getAppAndroidId(),
                            GPSStatus,
                            1,
                            calculateBatteryPercentage()

                    ));
                }else if(GPSStatus==0){

                    LazyDataConnection GetSources_B2CTask = new LazyDataConnection("insert_location_v3");
                    GetSources_B2CTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "insert_location_v3", getAPI.insert_location_v3(
                            "unspecified",
                            LocationAddress,
                            getSharedPref.getUserPhone(),
                            "",
                            "",
                            0,
                            "0.0",
                            "0.0",
                            getSharedPref.getlat(),
                            getSharedPref.getlng(),
                            0,
                            "",
                            getSharedPref.getAppVersionCode(),
                            getSharedPref.getAppAndroidId(),
                            GPSStatus,
                            1,
                            calculateBatteryPercentage()

                    ));
                }


            }
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        curLocation = location;
        getSharedPref = new SharedPref(context);
//        Toast.makeText(context, "cur lat==>"+curLocation.getLatitude(), Toast.LENGTH_SHORT).show();

       // if(curLocation!=null && curLocation.getLatitude()>0 && curLocation.getLongitude()>0){
        getSharedPref.setLatLng(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
       // }
    }

    //=====================================================================================
    // Async Task Class
    //=====================================================================================
    public class LazyDataConnection extends AsyncTask<String, Void, String> {
        String method;

        public LazyDataConnection(String method) {
            this.method = method;
        }

        @Override
        protected String doInBackground(String... arg0) {
            method = arg0[0];
            return getData.callLocationWebService(arg0[0], arg0[1],false);
        }

        protected void onPostExecute(String xmlResponse) {
            if (xmlResponse.equals("")) {
                return;
            } else if (method.equals("insert_location_v3")) {

                getSharedPref.setLatLng("0.0", "0.0");
                /*InsertGpsDetail(xmlResponse,"insert_location_v3Result");
                if(Status>0){
                    Toast.makeText(context, StatusMessage, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, StatusMessage, Toast.LENGTH_LONG).show();
                }*/

            }

        }
    }

    //=====================================================================================
    // GetAllSources
//=====================================================================================
    public void InsertGpsDetail(String str_RespXML, String str_TagName) {
        Document doc = getData.XMLfromString(str_RespXML);
        NodeList nodes = doc.getElementsByTagName(str_TagName);

        if (nodes.getLength() > 0)
            for (int i = 0; i <= nodes.getLength(); i++) {
                Node e1 = nodes.item(i);
                Element el = (Element) e1;

                if (el != null) {
                    try {
                        Status = Integer.parseInt(el.getElementsByTagName("id").item(0).getTextContent());
                    } catch (Exception e) {
                    }
                    try {
                        StatusMessage = el.getElementsByTagName("alertMessage").item(0).getTextContent();
                    } catch (Exception e) {
                    }

                }
            }
    }

    public void GetAddress(double lat,double lng) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 5);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                LocationArray = new String[1];
                for (int i = 0; i <=address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());
                if (address.getLocality().length() > 0 && address.getLocality() != null) {
                    LocationArray[0] = address.getLocality();
                } else {
                    LocationArray[0] = "";
                }


                LocationAddress = sb.toString();
                if(LocationAddress!=null && LocationAddress.length()>0 && LocationAddress.contains("null") || LocationAddress.contains("Null") || LocationAddress.contains("NULL")){
                    if(LocationAddress.contains("null")){
                        LocationAddress=LocationAddress.replace("null","");
                    }
                    if(LocationAddress.contains("Null")){
                        LocationAddress=LocationAddress.replace("Null","");
                    }
                    if(LocationAddress.contains("NULL")){
                        LocationAddress=LocationAddress.replace("NULL","");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateBatteryPercentage(){

        boolean present = batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
        int battery_percentage=0;
        if(present){
            battery_percentage=batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
        return battery_percentage;
    }

    private int isGPSON(){
          manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
          statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
          if(statusOfGPS){
              return 1;
          }else{
              return 0;
          }
    }

}
