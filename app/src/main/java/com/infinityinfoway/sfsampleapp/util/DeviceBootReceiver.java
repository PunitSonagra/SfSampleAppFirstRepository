package com.infinityinfoway.sfsampleapp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.infinityinfoway.sfsampleapp.database.SharedPref;

import java.util.Calendar;

public class DeviceBootReceiver extends BroadcastReceiver {
	SharedPref getSharedPref;
	@Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /* Setting the alarm here */

        	getSharedPref=new SharedPref(context);
            Calendar updateTime = Calendar.getInstance();
	        updateTime.set(Calendar.SECOND, 5);
	        Intent alarmIntent = new Intent(context, DataSendingReceiver.class);
			PendingIntent recurringDownload = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        alarms.cancel(recurringDownload);
	        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 1000 * 60*getSharedPref.getLocationRequestTime(), recurringDownload); //will run it after every 60 seconds.
	             
        }
    }
}
