package com.sportschule.rootfit.Admin.adminStats;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sportschule.rootfit.Admin.AdminActivity;
import com.sportschule.rootfit.R;

import java.util.ArrayList;

public class CreditAndLPermit extends AppCompatActivity{
    PDFView pdfView;
    private static final String TAG="adminStats";
    ArrayList<DataSnapshot> itemList=new ArrayList<DataSnapshot>();
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private final DatabaseReference Statistics = dbPhone.getReference().child("Statistics");

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CreditAndLPermit.this, AdminActivity.class);
        CreditAndLPermit.this.startActivity(intent);
        CreditAndLPermit.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        pdfView = findViewById(R.id.pdfView);
        pdfView.fromAsset("Vecteezy-License-Information.pdf").load();
    }
}