package com.infinityinfoway.sfsampleapp.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.infinityinfoway.sfsampleapp.api.APIsConnectivity;
import com.infinityinfoway.sfsampleapp.api.DataConnectivity;
import com.infinityinfoway.sfsampleapp.database.DBConnector;
import com.infinityinfoway.sfsampleapp.database.SharedPref;
import com.infinityinfoway.sfsampleapp.model.GPSMasterBean;
import com.infinityinfoway.sfsampleapp.util.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobScheduledService extends JobService {

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    private DBConnector dbConnector;
    private LocationUpdateForegroundService mService = null;
    private Intent batteryStatus;
    // Tracks the bound state of the service.
    private boolean mBound = false;
    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private ConnectionDetector cd;
    private IntentFilter ifilter;
    private Date today;
    private SimpleDateFormat sdf;
    private LocationManager manager;
    private boolean statusOfGPS;
    private String[] LocationArray;
    private String LocationAddress = "";

    private APIsConnectivity getAPI = new APIsConnectivity();
    private DataConnectivity getData = new DataConnectivity();

    private SharedPref getSharedPref;
    private List<GPSMasterBean> gpsMasterBeanList;
    private int loopCounter=0;
    private String minLastUpdatedRecordId,maxLastUpdatedRecordId;
    private long Status;

    @Override
    public boolean onStartJob(JobParameters params) {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dbConnector=new DBConnector(this);
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        cd=new ConnectionDetector(this);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (!isServiceRunning(LocationUpdateForegroundService.class)) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return true;
            }
            startWorkOnNewThread();
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("finish job","");
      //  jobFinished(params, true);
        return true;
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = i
                    .next();

            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                serviceRunning = true;

                if (runningServiceInfo.foreground) {
                    //service run in foreground
                    Log.e("run", "service run in foreground");
                }
            }
        }
        return serviceRunning;
    }

    private void startWorkOnNewThread() {
        new Thread(new Runnable() {
            public void run() {
                Log.e("job", "start");
                if (ActivityCompat.checkSelfPermission(JobScheduledService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(JobScheduledService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Log.e("job", "start 111");
                getLastLocation();

            }
        }).start();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                Log.e("lat::", "" + mLocation.getLatitude());
                            } else {
                                Log.e("location", "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e("location", "Lost location permission." + unlikely);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
               if(location!=null){
                   if(dbConnector!=null && location!=null){

                       batteryStatus= registerReceiver(null, ifilter);

                       today=new Date();
                       GPSMasterBean data=new GPSMasterBean();

                       data.setGPS_Location_Name("unspecified");
                       data.setGPS_Latitude(String.valueOf(location.getLatitude()));
                       data.setGPS_Longitude(String.valueOf(location.getLongitude()));

                       try{
                           GetAddress(location.getLatitude(),location.getLongitude());
                       }catch (Exception ex){
                           data.setGPS_Address("");
                       }

                       try{
                           data.setGPS_Battery_Percentage(String.valueOf(calculateBatteryPercentage()));
                       }catch (Exception ex){}

                       if(cd!=null){
                           data.setGPS_Internet_Status(cd.isConnectingToInternet()?"1":"0");
                       }else{
                           data.setGPS_Internet_Status("0");
                       }

                       data.setGPS_Status(String.valueOf(isGPSON()));

                       try{
                           data.setGPS_DateTime(sdf.format(today));
                       }catch (Exception ex){}

                       Log.e("inside job service add", "save data");

//                Toast.makeText(context, "old=="+getSharedPref.getlat(), Toast.LENGTH_LONG).show();

                       if(data!=null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("1") &&
                               (!TextUtils.isEmpty(data.getGPS_Latitude())) && (!TextUtils.isEmpty(data.getGPS_Longitude())) &&
                               Double.parseDouble(data.getGPS_Latitude()) >0 && Double.parseDouble(data.getGPS_Longitude()) >0){
                           dbConnector.addGPSData(data);

                       }else if(data!=null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("0")){
                           dbConnector.addGPSData(data);
                       }

                       Log.e("Job scheduled", "Save data");
                   }
               }
            }
        });

        String manufacturer = Build.MANUFACTURER;
        if((!TextUtils.isEmpty(manufacturer)) && manufacturer.equalsIgnoreCase("xiaomi") && dbConnector!=null && dbConnector.countGPSData()>12){
            sendingLocationData();
        }else if(dbConnector!=null && dbConnector.countGPSData()>12){
            sendingLocationData();
        }
    }

    public void GetAddress(double lat,double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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
        try{
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(statusOfGPS){
                return 1;
            }else{
                return 0;
            }
        }catch (Exception ex){
            return 0;
        }

    }

    private void sendingLocationData(){
        loopCounter=0;

        gpsMasterBeanList=new ArrayList<>();

        try{
            gpsMasterBeanList=dbConnector.getGPSMasterData();
        }catch (Exception ex){}




        JSONArray jsonArray=new JSONArray();
        if(gpsMasterBeanList!=null && gpsMasterBeanList.size()>0){
            for(GPSMasterBean data:gpsMasterBeanList){

                loopCounter++;
                JSONObject jsonObject=new JSONObject();
                try{
                    jsonObject.put("loc_name",data.getGPS_Location_Name());
                }catch (Exception ex){}

                try{
                    jsonObject.put("loc_address",data.getGPS_Address());
                }catch (Exception ex){}


                try{
                    jsonObject.put("loc_latitude",data.getGPS_Latitude());
                }catch (Exception ex){}


                try{
                    jsonObject.put("loc_longitude",data.getGPS_Longitude());
                }catch (Exception ex){}


                try{
                    jsonObject.put("loc_date_time",data.getGPS_DateTime());
                }catch (Exception ex){}

                try{
                    jsonObject.put("loc_battery",data.getGPS_Battery_Percentage());
                }catch (Exception ex){}

                try{
                    jsonObject.put("loc_gps_status",data.getGPS_Status());
                }catch (Exception ex){}

                try{
                    jsonObject.put("loc_network_status",data.getGPS_Internet_Status());
                }catch (Exception ex){}

                jsonArray.put(jsonObject);
                if(loopCounter==1){
                    minLastUpdatedRecordId=data.getGPS_Master_Id();
                }
                if(loopCounter==gpsMasterBeanList.size()){
                    maxLastUpdatedRecordId=data.getGPS_Master_Id();
                }



            }

//            Log.e("jsonArray",""+jsonArray);
//
//            Log.e("lastUpdatedRecordId",""+lastUpdatedRecordId);


            getSharedPref=new SharedPref(getApplicationContext());
            LazyDataConnection GetSources_B2CTask = new LazyDataConnection("insert_location_shrink");
            GetSources_B2CTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "insert_location_shrink", getAPI.insert_location_shrink(
                    getSharedPref.getAppVersionCode(),
                    getSharedPref.getAppAndroidId(),
                    getSharedPref.getRegisteredId(),
                    0,
                    getSharedPref.getUserPhone(),
                    jsonArray.toString()
            ));
        }

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
            } else if (method.equals("insert_location_shrink")) {

                InsertGpsDetail(xmlResponse,"insert_location_shrinkResult");

                if(Status>0){
                    dbConnector.deleteGPSRangeData(Integer.parseInt(minLastUpdatedRecordId), Integer.parseInt(maxLastUpdatedRecordId));
                }

//                Log.e("job scheduled", "insert_location_shrink");

               /* String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/SheetalSalesApp/StatusFile/");

                myDir.mkdirs();

                String filename = "gpsStatus.txt";
                File file = new File(myDir, filename);

                String resStatus="Staus==>"+String.valueOf(Status)+"\nmin==>"+String.valueOf(minLastUpdatedRecordId)+"\nmax==>"+String.valueOf(maxLastUpdatedRecordId);

                try {
                    OutputStream output = new FileOutputStream(file);
                    output.write(String.valueOf(resStatus).getBytes());
                    output.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
                        Status = Long.parseLong(el.getElementsByTagName("id").item(0).getTextContent());
                    } catch (Exception e) { }
                }
            }
    }
}
