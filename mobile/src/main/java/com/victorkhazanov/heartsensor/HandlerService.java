package com.victorkhazanov.heartsensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.WearableStatusCodes;

public class HandlerService    extends WearableListenerService

{
    GoogleApiClient mGoogleApiClient;
    private static final String TAG = "HandlerService";
    public static final String CONFIG_START = "config/start";
    public static final String CONFIG_STOP = "config/stop";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    private  static  final  String PATH="/heartrate";



    int mStartMode;       // indicates how to behave if the service is killed

    boolean mAllowRebind; // indicates whether onRebind should be used

    @Override
    public void onCreate() {
        // The service is being created
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApiIfAvailable(Wearable.API)
                    .build();


            byte[] message = new byte[1];

            mGoogleApiClient.connect();

            //    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    for (int i = 0; i < result.getNodes().size(); i++) {
                        Node node = result.getNodes().get(i);
                        String nName = node.getDisplayName();
                        String nId = node.getId();
                        Log.d(TAG, "Node name and ID: " + nName + " | " + nId);

                        Wearable.MessageApi.addListener(mGoogleApiClient, new MessageApi.MessageListener() {
                            @Override
                            public void onMessageReceived(MessageEvent messageEvent) {


                                if (messageEvent.getPath().equals("/control")) {
                                    final String message = new String(messageEvent.getData());
                                    Log.v("myTag", "Message path received on watch is: " + messageEvent.getPath());
                                    Log.v("myTag", "Message received on watch is: " + message);

                                    NotificationCompat.Builder builder =
                                            new NotificationCompat.Builder(getApplicationContext())
                                                    .setSmallIcon(R.drawable.cast_ic_notification_0)
                                                    .setContentTitle("Your Heart Rate")
                                                    .setContentText(message);

                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    builder.setSound(alarmSound);
                                    int NOTIFICATION_ID = 12345;

                                    Intent targetIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    builder.setContentIntent(contentIntent);
                                    NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    nManager.notify(NOTIFICATION_ID, builder.build());

                                    //   int heartRate =GetSensorData();


                                }
                                Log.d(TAG, "Message received: " + messageEvent);
                            }
                        });

                        PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                PATH, null);
                        messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                Status status = sendMessageResult.getStatus();
                                Log.d(TAG, "Status: " + status.toString());
                                if (status.getStatusCode() != WearableStatusCodes.SUCCESS) {

                                }

                                //stopSelf();
                            }
                        });
                    }
                }
            });
        }

        catch (Exception ex){


            Log.e("HandlerService", "ERROR: failed to send Message: " + ex.getMessage());
        }




        return START_STICKY;
    }





    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();




            SimpleWakefulReceiver.completeWakefulIntent(intent);
        }
    }





}
