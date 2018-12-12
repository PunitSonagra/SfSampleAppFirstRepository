package com.infinityinfoway.sfsampleapp.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.infinityinfoway.sfsampleapp.R;
import com.infinityinfoway.sfsampleapp.activity.Activity_Splash;
import com.infinityinfoway.sfsampleapp.api.APIsConnectivity;
import com.infinityinfoway.sfsampleapp.api.DataConnectivity;
import com.infinityinfoway.sfsampleapp.database.DBConnector;
import com.infinityinfoway.sfsampleapp.database.SharedPref;
import com.infinityinfoway.sfsampleapp.model.GPSMasterBean;
import com.infinityinfoway.sfsampleapp.util.ConnectionDetector;
import com.infinityinfoway.sfsampleapp.util.LocationUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LocationUpdateForegroundService extends Service {
    public LocationUpdateForegroundService() {
    }

    private Handler mHandler = new Handler();

    private static final String PACKAGE_NAME =
            "com.infinityinfoway.sheetalsalesapp.service.updatedata";

    private static final String TAG = LocationUpdateForegroundService.class.getSimpleName();

    private DBConnector dbConnector;

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 12000000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    //


    private LocationManager manager;
    private boolean statusOfGPS;
    private IntentFilter ifilter;
    private Intent batteryStatus;
    private SimpleDateFormat sdf;
    private Date today;
    private String[] LocationArray;
    private String LocationAddress = "";
    private ConnectionDetector cd;



    // LocationData Sending

    private APIsConnectivity getAPI = new APIsConnectivity();
    private DataConnectivity getData = new DataConnectivity();
    private SharedPref getSharedPref;
    private List<GPSMasterBean> gpsMasterBeanList;
    private int loopCounter=0;
    private String minLastUpdatedRecordId,maxLastUpdatedRecordId;
    private long Status;


    public static final String
            LOCK_NAME_STATIC="com.infinityinfoway.sheetalsalesapp.service.LocationUpdateForegroundService.Static";;
    public static final String
            LOCK_NAME_LOCAL="ccom.infinityinfoway.sheetalsalesapp.service.LocationUpdateForegroundService.Local";

    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private ActivityManager activityManager;

    @Override
    public void onCreate() {

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GpsTrackerWakelock");
        wl.acquire();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }

        };

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        cd=new ConnectionDetector(this);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbConnector=new DBConnector(this);


        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }



    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }

        startTimer();

        // Tells the system to not try to recreate the service after it has been killed.
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "Task Removed");
        super.onTaskRemoved(rootIntent);

        Log.i(TAG, "Task Removed 111");

    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && LocationUtil.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            // TODO(developer). If targeting O, use the following code.
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
//                mNotificationManager.startServiceInForeground(new Intent(this,
//                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
//            } else {
//                startForeground(NOTIFICATION_ID, getNotification());
//            }

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        wl.release();
        stoptimertask();
        Log.i(TAG, "Service Destroyed");

        Log.i(TAG, "WakeLock Destroyed");
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */

    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");

        LocationUtil.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdateForegroundService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            LocationUtil.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            LocationUtil.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            LocationUtil.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdateForegroundService.class);

        CharSequence text = LocationUtil.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Activity_Splash.class), 0);

       /* NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_punchin, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_punchout, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(LocationUtil.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());*/


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle(LocationUtil.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                onNewLocation(mLocation);
                                Log.e(TAG, ""+mLocation.getLatitude());
                            } else {
                                Log.e(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.e(TAG, "New location: " + location.getLatitude());




        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }

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

//                Toast.makeText(context, "old=="+getSharedPref.getlat(), Toast.LENGTH_LONG).show();

            if(data!=null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("1") &&
                    (!TextUtils.isEmpty(data.getGPS_Latitude())) && (!TextUtils.isEmpty(data.getGPS_Longitude())) &&
                    Double.parseDouble(data.getGPS_Latitude()) >0 && Double.parseDouble(data.getGPS_Longitude()) >0){
                 dbConnector.addGPSData(data);

            }else if(data!=null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("0")){
                dbConnector.addGPSData(data);
            }

            Log.e("Location update", "Save data");
        }

        //Log.e("countGPSData", ""+dbConnector.countGPSData());

        String manufacturer = Build.MANUFACTURER;
        if((!TextUtils.isEmpty(manufacturer)) && manufacturer.equalsIgnoreCase("xiaomi") && dbConnector!=null && dbConnector.countGPSData()>12){
            sendingLocationData();
        }else if(dbConnector!=null && dbConnector.countGPSData()>12){
            sendingLocationData();
        }
    }

    /**
     * Sets the location request parameters.
     */
    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationUpdateForegroundService getService() {
            return LocationUpdateForegroundService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
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

                Log.e("Location update", "insert_location_shrink");

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
                    } catch (Exception e) {
                    }


                }
            }
    }

    private Timer timer;
    public int counter=0;
    private TimerTask timerTask;
    public void startTimer() {

        try{
            timer = new Timer();
            timerTask = new TimerTask() {
                public void run() {

                    Log.e("Count", "=========  "+ (counter++));
                    getLastLocation();

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // display toast

                        }

                    });



                }
            };
            timer.schedule(timerTask, 1000, 18000000); //
        }catch (Exception ex){}

    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


}
