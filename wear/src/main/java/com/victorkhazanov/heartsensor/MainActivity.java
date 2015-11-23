package com.victorkhazanov.heartsensor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onYesBtn(View view) {

     //   _receiver.setAlarm(this);

        finish();

    }

    public void onNoBtn(View view) {

        WearService ws= new WearService();

        ws.CancelSensorListener();


        finish();
//        _receiver.cancelAlarm(this);
    }







}
