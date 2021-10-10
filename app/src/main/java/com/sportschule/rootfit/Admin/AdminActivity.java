package com.sportschule.rootfit.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sportschule.rootfit.Admin.adminCreateNewTrainer.adminCreateNewTrainer;
import com.sportschule.rootfit.Admin.adminStats.AdminStatisticCharts;
import com.sportschule.rootfit.Admin.adminStats.CreditAndLPermit;
import com.sportschule.rootfit.Admin.adminViewUsers.AdminViewUsers;
import com.sportschule.rootfit.LoginActivity;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.ToastCustomMessage;

public class AdminActivity extends AppCompatActivity {
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    Intent adminIntent;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toast.toastMessage("Admin logout!");
        mAuth.signOut();
        Intent loginIntent = new Intent(AdminActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(loginIntent);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ActionBar actionBar = getSupportActionBar();
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        actionBar.hide();

    }

    public void personControl(View view) {
        adminIntent = new Intent(AdminActivity.this, AdminViewUsers.class);
        setUpIntent();
    }

    public void statisticControl(View view) {
        adminIntent = new Intent(AdminActivity.this, AdminStatisticCharts.class);
        setUpIntent();
    }

    public void trainerRollInControl(View view) {
        adminIntent = new Intent(AdminActivity.this, adminCreateNewTrainer.class);
        setUpIntent();
    }

    public void licenseControl(View view) {
        adminIntent = new Intent(AdminActivity.this, CreditAndLPermit.class);
        setUpIntent();
    }
    private void setUpIntent()
    {
        AdminActivity.this.startActivity(adminIntent);
        AdminActivity.this.finish();
    }

    public void websiteNavigate(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.vecteezy.com")));

    }

    public void instagramNevigate(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/vecteezy")));

    }

    public void linkedNavigate(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/company/eezy.com")));

    }
}