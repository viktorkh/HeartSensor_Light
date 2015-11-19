package com.victorkhazanov.heartsensor;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends ActionBarActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleClient;

    public EditText txtDuration;
    public EditText txtMaxRate;

    SimpleWakefulReceiver _receiver = new SimpleWakefulReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtDuration = (EditText)findViewById(R.id.editText_duration);
        txtMaxRate = (EditText)findViewById(R.id.editText_maxrate);


        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {
        //  String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI

    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }




    public void btnSave_Click(View target) {


        String duration = txtDuration.getText().toString();

        String maxrate = txtMaxRate.getText().toString();


        _receiver.setAlarm(this);

    //    new SendToDataLayerThread("/message_path",duration,maxrate ).start();


        finish();

    }

    public void btnCancel_Click(View target) {


        _receiver.cancelAlarm(this);
        finish();
    }




    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String duration,String maxrate) {
            path = p;
            message = "duration:"+duration+";"+"maxrate:"+maxrate;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Toast.makeText(getApplicationContext(), "There is a problem with connection !!!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }


    }
}