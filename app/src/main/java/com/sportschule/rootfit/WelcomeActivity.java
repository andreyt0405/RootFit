package com.sportschule.rootfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
public class WelcomeActivity extends AppCompatActivity {
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String welcomeFlag = "false";
    private String readTXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/impact.ttf");
        if(!readData()){
            setContentView(R.layout.activity_welcome);
            TextView getStartedView = findViewById(R.id.get_start);
            getStartedView.setTypeface(tf);
        }
        else
        {
            Intent mainIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
            WelcomeActivity.this.startActivity(mainIntent);
            WelcomeActivity.this.finish();
        }
    }

    public void saveData(View view){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);/*NO OTHER APP CAN CHANGE OUR PREFERENCES*/
        SharedPreferences.Editor editor =  sharedPreferences.edit();

        editor.putString(welcomeFlag,"true");
        editor.apply();
        Intent mainIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
        WelcomeActivity.this.startActivity(mainIntent);
        WelcomeActivity.this.finish();
    }

    public boolean readData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);/*NO OTHER APP CAN CHANGE OUR PREFERENCES*/
        readTXT = ((SharedPreferences) sharedPreferences).getString(welcomeFlag,"false");
        return Boolean.parseBoolean(readTXT);
    }
}