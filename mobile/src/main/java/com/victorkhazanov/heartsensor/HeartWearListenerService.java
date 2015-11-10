package com.victorkhazanov.heartsensor;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class HeartWearListenerService extends WearableListenerService {
    public HeartWearListenerService() {
    }


    public static String SERVICE_CALLED_WEAR = "WearListClicked";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String event = messageEvent.getPath();
        Log.d("Listclicked", event);
        String [] message = event.split("--");

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
        v.vibrate(400);

        if (message[0].equals(SERVICE_CALLED_WEAR)) {

            Intent intent = new Intent(this, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("VOICE_DATA", messageEvent.getData());


            // intent.putExtra("message from wear",);
            startActivity(intent);


        }
    }

}
