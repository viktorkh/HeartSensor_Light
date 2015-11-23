package com.victorkhazanov.heartsensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class WearService extends WearableListenerService implements SensorEventListener

{


    private GoogleApiClient mGoogleApiClient;
    private String mNodeId;
    private static final String TAG = "WearService";
    public static final String CONFIG_START = "config/start";
    public static final String CONFIG_STOP = "config/stop";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    private static final String PATH = "/control";

    private static byte[] message;

    private static int heartData;


    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {


        if (messageEvent.getPath().equals("/control")) {

            final String message = new String(messageEvent.getData());



        } else if (messageEvent.getPath().equals("/heartrate")) {


            GetSensorData();

        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void GetSensorData() {
        int heartRate = 0;
        if (mSensorManager == null) {
            mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        }


        if (mHeartRateSensor == null) {
            mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

            mSensorManager.registerListener((SensorEventListener) this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

        }


//        return heartRate;


    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {

            if (event.values[0] > 0) {
                int data = (int) event.values[0];
                if (heartData != data) {

                    heartData = data;
                    String msg = String.valueOf(heartData);
                    sendHeartRate(msg);
                }
            } else {

            /*    mSensorManager.unregisterListener(this);
                mHeartRateSensor=null;*/

            }
        }
    }

    private void sendHeartRate(String msg) {


        message = msg.getBytes();

        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApiIfAvailable(Wearable.API)
                    .build();


            mGoogleApiClient.connect();

            PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    for (int i = 0; i < result.getNodes().size(); i++) {
                        Node node = result.getNodes().get(i);
                        String nName = node.getDisplayName();
                        String nId = node.getId();
                        Log.d(TAG, "Node name and ID: " + nName + " | " + nId);

                        PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                PATH, message);
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

            mSensorManager.unregisterListener(this);
            mHeartRateSensor = null;

        } catch (Exception ex) {

            Log.e("WearService", "ERROR: failed to send Message: " + ex.getMessage());
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onStart() {

        mSensorManager.registerListener(this, this.mHeartRateSensor, 3);
    }

    public void CancelSensorListener(){

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }


        if (mHeartRateSensor != null) {
            mHeartRateSensor = null;

        }
    }



}






















