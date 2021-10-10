package com.sportschule.rootfit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    /** Duration of wait **/
    /** in ms **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    /** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_main);
        final TextView motivation_string = (TextView) findViewById(R.id.motivation_string);
        motivation_string.setText(generateRandomString());
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
    private String generateRandomString()
    {
        String[] motivation = getResources().getStringArray(R.array.motivation_string);
        Random rand = new Random();
        int  n = rand.nextInt(motivation.length-1);
        Log.i("Motive",motivation[n]);
        return motivation[n];
    }
}