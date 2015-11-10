package com.victorkhazanov.heartsensor;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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
import com.google.android.gms.wearable.WearableStatusCodes;

public class AlarmService extends IntentService implements SensorService.OnChangeListener {

    private static final String TAG = "AlarmService";

    private static final String PATH = "/message_path";

    private static final long REPEAT_TIME = 1000 * 30;

    GoogleApiClient googleApiClient;

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();


            //   SendHeartRate();


            //      startService(new Intent(this, SensorService.class));

            bindService(new Intent(AlarmService.this, SensorService.class), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder binder) {
                    // Log.d(LOG_TAG, "connected to service.");
                    // set our change listener to get change events
                    ((SensorService.SensorServiceBinder)binder).setChangeListener(AlarmService.this);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            }, Service.BIND_AUTO_CREATE);

            //   bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            SimpleWakefulReceiver.completeWakefulIntent(intent);
        }
    }

    private void SendHeartRate() {


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();

        fireMessage();

    }

    private void fireMessage() {
        // Send the RPC
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                for (int i = 0; i < result.getNodes().size(); i++) {
                    Node node = result.getNodes().get(i);
                    String nName = node.getDisplayName();
                    String nId = node.getId();
                    Log.d(TAG, "Node name and ID: " + nName + " | " + nId);

                    Wearable.MessageApi.addListener(googleApiClient, new MessageApi.MessageListener() {
                        @Override
                        public void onMessageReceived(MessageEvent messageEvent) {
                            Log.d(TAG, "Message received: " + messageEvent);
                        }
                    });

                    PendingResult<MessageApi.SendMessageResult> messageResult =
                            Wearable.MessageApi.sendMessage(googleApiClient, node.getId(),
                                    PATH, null);
                    messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Status status = sendMessageResult.getStatus();
                            Log.d(TAG, "Status: " + status.toString());
                            if (status.getStatusCode() != WearableStatusCodes.SUCCESS) {


                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onValueChanged(int newValue) {

    }
}
