package com.infinityinfoway.sfsampleapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DataSendingReceiver extends BroadcastReceiver {

	Context context;
	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		this.context=context;
		DataSendingService data=new DataSendingService(context);
//        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        data.sendData();

       /* ConnectionDetector connectionDetector = new ConnectionDetector(context);
        if(!statusOfGPS)
        {
            if(connectionDetector.isConnectingToInternet())
                Notification(context,"Please Start Location");
            else
            {
                Notification(context,"Please Start Internet and Location");
            }

        }
        else
        {
            if(!connectionDetector.isConnectingToInternet())
            {
                Notification(context,"Please Start Internet");
            }
            else
            {
                data.sendData();
            }
        }*/
	}


	
}
