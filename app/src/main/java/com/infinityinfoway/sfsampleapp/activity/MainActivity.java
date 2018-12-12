package com.infinityinfoway.sfsampleapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.infinityinfoway.sfsampleapp.BuildConfig;
import com.infinityinfoway.sfsampleapp.R;
import com.infinityinfoway.sfsampleapp.database.SharedPref;
import com.infinityinfoway.sfsampleapp.service.JobScheduledService;
import com.infinityinfoway.sfsampleapp.service.LocationUpdateForegroundService;
import com.infinityinfoway.sfsampleapp.util.DataSendingReceiver;
import com.infinityinfoway.sfsampleapp.util.DeviceBootReceiver;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private CardView card_punch_in,card_punch_out;
    private FrameLayout frm_add_attendance;



    // Foreground location service

    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdateForegroundService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    private PowerManager pm;
    private SharedPref getSharedPref;

    private LocationManager manager;
    private boolean statusOfGPS;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final long INTERVAL = 1000 * 60;
    private static final long FASTEST_INTERVAL = 1000 * 30;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private ProgressDialog progDialog;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdateForegroundService.LocalBinder binder = (LocationUpdateForegroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                mService.requestLocationUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSharedPref=new SharedPref(MainActivity.this);
        myReceiver = new MyReceiver();
        card_punch_in=findViewById(R.id.card_punch_in);

        card_punch_out=findViewById(R.id.card_punch_out);
        frm_add_attendance=findViewById(R.id.frm_add_attendance);

        card_punch_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }*/
            }
        });

        card_punch_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mService.removeLocationUpdates();
            }
        });

        scheduleJob();

        sendLocationData();


    }







    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(MainActivity.this);



        /*img_add_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }
            }
        });*/

       /* img_minus_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();
            }
        });*/


        //


        // Restore the state of the buttons when the activity (re)launches.


        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdateForegroundService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {

        super.onResume();


        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isGooglePlayServicesAvailable()) {
            statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!statusOfGPS) {
                buildGoogleApiClient();
                createLocationRequest();
            }else{
                dismissAppConfigDialog();
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdateForegroundService.ACTION_BROADCAST));


    }

    protected void createLocationRequest() {

        try{
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setSmallestDisplacement(20);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final com.google.android.gms.common.api.Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });

        }catch (Exception ex){}

    }

    protected synchronized void buildGoogleApiClient() {
        try{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }catch (Exception ex){}
    }


    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            /*if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }*/
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    frm_add_attendance,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdateForegroundService.EXTRA_LOCATION);
            if (location != null) {
               /* Toast.makeText(MainActivity.this, LocationUtil.getLocationText(location),
                        Toast.LENGTH_LONG).show();*/
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.

    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.

                Snackbar.make(
                        frm_add_attendance,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public  void scheduleJob() {

        final long ONE_DAY_INTERVAL = 1500000; // 10 Min

        final long ONE_DAY_INTERVAL_RESTRICTED = 1500000; // 15 Min

        ComponentName serviceComponent = new ComponentName(MainActivity.this, JobScheduledService.class);
        JobInfo.Builder builder = new JobInfo.Builder(10, serviceComponent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            builder.setPeriodic(ONE_DAY_INTERVAL_RESTRICTED);
        }else{
            builder.setPeriodic(ONE_DAY_INTERVAL);
        }

        Log.d("Activity_Home", "job being to start!");

        JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(builder.build());

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Activity_Home", "Job scheduled!");
        } else {
            Log.d("Activity_Home", "Job not scheduled");
        }
    }

    private void sendLocationData(){



        Calendar updateTime = Calendar.getInstance();

        updateTime.set(Calendar.SECOND, 10);

        Intent alarmIntent = new Intent(this, DataSendingReceiver.class);
//            PendingIntent recurringDownload = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(this, 501, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(recurringDownload);

        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 1000 * 60 * getSharedPref.getLocationRequestTime(), recurringDownload); //will run it after every 5 mins.
        getSharedPref.setAlarm(true);


        ComponentName receiver = new ComponentName(MainActivity.this, DeviceBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);




           /* Calendar updateTime = Calendar.getInstance();

            updateTime.set(Calendar.SECOND, 5);


            Intent alarmIntent = new Intent(this, DataSendingReceiver.class);
            PendingIntent recurringDownload = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarms.cancel(recurringDownload);

            //alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 1000 * 60 * getSharedPref.getLocationRequestTime(), recurringDownload); //will run it after every 5 seconds.
            getSharedPref.setAlarm(true);


            if (Build.VERSION.SDK_INT >= 23) {
// Wakes up the device in Doze Mode
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(),
                        recurringDownload);
            } else if (Build.VERSION.SDK_INT >= 19) {
// Wakes up the device in Idle Mode
                alarms.setExact(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), recurringDownload);
            } else {
// Old APIs
                alarms.set(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), recurringDownload);
            }


            ComponentName receiver = new ComponentName(MainActivity.this, DeviceBootReceiver.class);
            PackageManager pm = getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

                    */



    }

    private void dismissAppConfigDialog(){
        try{
            if(progDialog!=null && progDialog.isShowing()){
                progDialog.dismiss();
            }
        }catch (Exception ex){}

    }



}
