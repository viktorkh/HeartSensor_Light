package com.victorkhazanov.heartsensor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

public class SimpleWakefulReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;

    private PendingIntent alarmIntent;

    private static final long REPEAT_TIME = 1000 * 30;

    public SimpleWakefulReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is the Intent to deliver to our service.
        Intent service = new Intent(context, AlarmService.class);

        startWakefulService(context, service);
    }

    public void setAlarm(Context context) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SimpleWakefulReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 30);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), REPEAT_TIME, alarmIntent);


        ComponentName receiver = new ComponentName(context, SimpleWakefulReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }


        ComponentName receiver = new ComponentName(context, SimpleWakefulReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
