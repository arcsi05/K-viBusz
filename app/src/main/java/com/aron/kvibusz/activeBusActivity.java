package com.aron.kvibusz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activeBusActivity extends AppCompatActivity {

    TextView stopNameTextView, directionTextView, timeLeftTextView, lineNumberTextView;

    String lineNum, lineColor, busStop, direction;
    Long timeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_bus);
        stopNameTextView = findViewById(R.id.stopNameTextView);
        directionTextView = findViewById(R.id.directionTextView);
        timeLeftTextView = findViewById(R.id.timeLeftTextView);
        lineNumberTextView = findViewById(R.id.lineNumberTextView);

        getData();
        setData();
        startNotificationService();
    }

    public void startNotificationService() {
        String title = lineNum + " " + direction;
        String label = busStop;
        Long when = timeLeft;

        Intent serviceIntent = new Intent(this, activeBusService.class);
        serviceIntent.putExtra("title", title);
        serviceIntent.putExtra("label", label);
        serviceIntent.putExtra("when", when);

        startService(serviceIntent);
    }

    public void stopNotificationService() {
        Intent serviceIntent = new Intent(this, activeBusService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        stopNotificationService();
    }

    private void getData() {
        if (getIntent().hasExtra("lineNum") &&
                getIntent().hasExtra("lineColor") &&
                getIntent().hasExtra("busStop") &&
                getIntent().hasExtra("direction") &&
                getIntent().hasExtra("timeLeft")) {
            lineNum = getIntent().getStringExtra("lineNum");
            lineColor = getIntent().getStringExtra("lineColor");
            busStop = getIntent().getStringExtra("busStop");
            direction = getIntent().getStringExtra("direction");
            timeLeft = getIntent().getLongExtra("timeLeft", System.currentTimeMillis() + 60000);
        } else {
            Toast.makeText(this, "ERROR: No bus selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setData() {
        stopNameTextView.setText(busStop);
        lineNumberTextView.setText(lineNum);
        directionTextView.setText(direction);
        new CountDownTimer(timeLeft * 60000, 60000) {

            public void onTick(long millisUntilFinished) {
                timeLeftTextView.setText((int) Math.ceil(millisUntilFinished / 60000) + 1 + "'");
            }

            public void onFinish() {
                timeLeftTextView.setText("🚌");
            }
        }.start();
        lineNumberTextView.setBackgroundColor(Color.parseColor(lineColor));
    }
}