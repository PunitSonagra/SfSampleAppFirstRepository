package com.infinityinfoway.sfsampleapp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPref {
	Context context;
	Boolean isGetCode;

	public SharedPref(Context mContext)
	{
		context = mContext;
	}
//=================================================================
// Set Login	
//=================================================================	
	public void setIsLogin(Boolean isLogin)
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putBoolean("IS_LOGIN", isLogin);
		editPref.commit();
	}
	
	public boolean IsLogin() 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
        return getReg.getBoolean("IS_LOGIN", false);
	}
	
//=================================================================
// Set setTempRegisteredId(mst_table)	
//=================================================================	
	public void setTempRegisteredId(int regId) 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putInt("TEMP_REG_ID", regId);
		editPref.commit();
	}
	public int getTempRegisteredId() 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getInt("TEMP_REG_ID", 0);
	}
	
//=================================================================
// setUserName	& phone in RegistrationActivity
//=================================================================	
	public void setUserName(String UserName)
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putString("USERNAME", UserName);
		editPref.commit();
	}
		
	public void setUserPhone(String phone)
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putString("PHONE", phone);
		editPref.commit();
	}
	
	public String getUserName()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("USERNAME", "");
	}
	public String getUserPhone()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("PHONE", "");
	}
	
//=================================================================
// setDeviceId	 in RegistrationDevice Activity
//=================================================================
	public void setRegisteredId(int regId) 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putInt("REG_ID", regId);
		editPref.commit();
	}
	
	public int getRegisteredId() 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getInt("REG_ID", 0);
	}
//================================================================
// mst tablel	 in Registered username
//=================================================================
	public void setRegisteredUserName(String regUserName)
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putString("REG_USERNAME", regUserName);
		editPref.commit();
	}
	public String getRegisteredUserName()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("REG_USERNAME","");
	}
	public void setRegisteredUserId(int regUserId) 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putInt("REG_USERID", regUserId);
		editPref.commit();
	}


	public int getRegisteredUserId() 
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getInt("REG_USERID", 0);
	}


	

	
//=================================================================
// Status Message...
//=================================================================	
	public String getStatusMsg()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("status_msg","");
	}
	
	public void setStatusMsg(String user)
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putString("status_msg", user);
		editPref.commit();	
	}
	

//=================================================================
	// First time application call..
//=================================================================	
		public void setFirstTimeAppStart(boolean flag)
		{
			SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editPref = getReg.edit();
			editPref.putBoolean("FIRSTTIME", flag);
			editPref.commit();	
		}
		public boolean getFirstTimeAppStart()
		{
			SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
			return getReg.getBoolean("FIRSTTIME", false);
		}
	
//=================================================================
// LoginToken...
//=================================================================	
		public void setLoginToken(String UserName)
		{
			SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editPref = getReg.edit();
			editPref.putString("LOGIN_TOKEN", UserName);
			editPref.commit();
		}
		public String getLoginToken()
		{
			SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
			return getReg.getString("LOGIN_TOKEN","");
		}
//=================================================================
		// Login Time Session set...
//=================================================================	
		public void setLoginTimeSession(String loginTime)
		{
					SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
					Editor editPref = getReg.edit();
					editPref.putString("LOGINTIME", loginTime);
					editPref.commit();
		}
		public String getLoginTimeSession()
		{
					SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
					return getReg.getString("LOGINTIME","");
		}

	public void saveAppCommonData(int VersionCode, String VersionName, String AndroidId, String AppOSVer)
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putInt("AppVerCode", VersionCode);
		editPref.putString("AppVerName", VersionName);
		editPref.putString("AndroidId", AndroidId);
		editPref.putString("AppOSVer", AppOSVer);
		editPref.commit();
	}
	public int getAppVersionCode()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getInt("AppVerCode", 0);
	}
	public String getAppVerName()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("AppVerName", "");
	}
	public String getAppAndroidId()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("AndroidId", "");
	}
	public String getAppOSVer()
	{
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("AppOSVer", "");
	}
	public void deletePref()
	{
		SharedPreferences setReg = PreferenceManager.getDefaultSharedPreferences(context);
		setReg.edit().clear().commit();
	}

	//=================================================================
	// usr_ref_id.........
	// =================================================================
	public void setLoginUserName(String distributer_name) {
		SharedPreferences getReg = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putString("User_Name", distributer_name);
		editPref.commit();
	}

	public String getLoginUserName() {
		SharedPreferences getReg = PreferenceManager
				.getDefaultSharedPreferences(context);
		return getReg.getString("User_Name", "");
	}

	//=================================================================
	// LoginLoginUserPassword.........
	// =================================================================
	public void setLoginLoginUserPassword(String UserPassword) {
		SharedPreferences getReg = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editPref = getReg.edit();
		editPref.putString("User_Password", UserPassword);
		editPref.commit();
	}

	public String getLoginLoginUserPassword() {
		SharedPreferences getReg = PreferenceManager
				.getDefaultSharedPreferences(context);
		return getReg.getString("User_Password", "");
	}

	public void setLocationRequestTime(int LocationRequesttime, int Position) {
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editPref = getReg.edit();
		editPref.putInt("LocationRequestTime", LocationRequesttime);
		editPref.putInt("LocationRequestPosition", Position);
		editPref.commit();
	}

	public int getLocationRequestTime() {
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getInt("LocationRequestTime", 5);
	}

	public void setLatLng(String lat, String lng) {
		SharedPreferences getReg = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editPref = getReg.edit();
		editPref.putString("lat", lat);
		editPref.putString("lng", lng);
		editPref.commit();
	}

	public String getlat() {
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("lat", "0.0");
	}

	public String getlng() {
		SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
		return getReg.getString("lng", "0.0");
	}

    public void setAlarm(boolean IsAlarm) {
        SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editPref = getReg.edit();
        editPref.putBoolean("IsAlarm", IsAlarm);
        editPref.commit();
    }

    public boolean isSetAlarm() {
        SharedPreferences getReg = PreferenceManager.getDefaultSharedPreferences(context);
        return getReg.getBoolean("IsAlarm", false);
    }



}
