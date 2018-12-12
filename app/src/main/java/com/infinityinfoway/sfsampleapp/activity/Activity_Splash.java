package com.infinityinfoway.sfsampleapp.activity;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.infinityinfoway.sfsampleapp.R;
import com.infinityinfoway.sfsampleapp.database.SharedPref;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Activity_Splash extends AppCompatActivity {

    private String android_id;

    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_STATE = 100;
    private final String[] RunTimePerMissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WAKE_LOCK,Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS};
    private SweetAlertDialog dialogSuccess;
    private SharedPref getSharedPref;
    private EditText et_name,et_mobile;
    private Button btn_register;
    private PackageInfo pInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSharedPref=new SharedPref(Activity_Splash.this);

        et_name=findViewById(R.id.et_name);
        et_mobile=findViewById(R.id.et_mobile);
        btn_register=findViewById(R.id.btn_register);

        try {
            android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        } catch (Exception ignored) {
        }

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String appOsVer = Build.MODEL + " :: " + Build.VERSION.RELEASE;
            getSharedPref.saveAppCommonData(pInfo.versionCode, pInfo.versionName, android_id, appOsVer);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(Activity_Splash.this, RunTimePerMissions)) {
                ActivityCompat.requestPermissions(Activity_Splash.this, RunTimePerMissions, MY_PERMISSIONS_REQUEST_READ_WRITE_STATE);
            } else {
                checkVersionInfoApiCall();
            }
        } else {
            checkVersionInfoApiCall();
        }
    }

    private void checkVersionInfoApiCall(){




        if(getSharedPref.IsLogin()){
            Intent intent = new Intent(Activity_Splash.this,
                    MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else{
            btn_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(et_name.getText().toString().trim())){
                        Toast.makeText(Activity_Splash.this, "Enter valid name !!!", Toast.LENGTH_LONG).show();
                    }else if(TextUtils.isEmpty(et_mobile.getText().toString().trim()) || et_mobile.getText().toString().length()<=9){
                        Toast.makeText(Activity_Splash.this, "Enter valid mobile no !!!", Toast.LENGTH_LONG).show();
                    }else{

                        if (!isAccessGranted()) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }else{
                            getSharedPref.setIsLogin(true);
                            getSharedPref.setUserName(et_name.getText().toString().trim());
                            getSharedPref.setUserPhone(et_mobile.getText().toString().trim());

                            String manufacturer = android.os.Build.MANUFACTURER;
//                        if ("vivo".equalsIgnoreCase(manufacturer)) {
//                            autoLaunchVivo(Activity_Splash.this);
//                        }else{
                            Intent intent = new Intent(Activity_Splash.this,
                                    MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            //}
                        }


                    }
                }
            });
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_WRITE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 7 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED && grantResults[5] == PackageManager.PERMISSION_GRANTED && grantResults[6] == PackageManager.PERMISSION_GRANTED) {
                checkVersionInfoApiCall();
            } else {
                alertAlert(getResources().getString(R.string.permissions_has_not_grant));
            }
        }
    }


    private void alertAlert(String msg) {

        try {

            dialogSuccess = new SweetAlertDialog(Activity_Splash.this, SweetAlertDialog.ERROR_TYPE);
            dialogSuccess.setTitleText(getResources().getString(R.string.permission_request));

            dialogSuccess.setContentText(msg);
            dialogSuccess.setCancelable(false);
            dialogSuccess.show();

            Button confirm_button=dialogSuccess.findViewById(R.id.confirm_button);
            confirm_button.setText(R.string.yes);

            confirm_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        dialogSuccess.dismissWithAnimation();
                        dialogSuccess.cancel();
                        finish();
                    }catch (Exception ex){}


                }
            });
        } catch (Exception ignored) { }

    }

    private static final int ACCESSIBILITY_ENABLED = 1;



    private static void autoLaunchVivo(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                context.startActivity(intent);
            } catch (Exception ex) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                    context.startActivity(intent);
                } catch (Exception exx) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
