package com.sportschule.rootfit;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView scannerView;
    QRGEncoder qrgEncoder;
    private String mAuth;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
           mAuth = extras.getString("mAuth");
        }
        new SampleDialogBox("Green Pass Require","We couldn't find your green pass in our system\n" +
                "Please scan green pass QR Code").show(getSupportFragmentManager(),"Green Pass");
        initScannerImplements();
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
    }
    @Override
    public void handleResult(Result rawResult) {
        qrgEncoder = new QRGEncoder(rawResult.getText(), null, QRGContents.Type.TEXT,20);
        String regxId = "\\b\\d{9}\\b";
        String regxDate = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
        Matcher matcherId = Pattern.compile(regxId).matcher(rawResult.getText());
        Matcher matcherDate = Pattern.compile(regxDate).matcher(rawResult.getText());
        if(rawResult.getText().contains("IL MOH") && matcherId.find() && matcherDate.find())
        {
            uploadQRGreenPassStorage(qrgEncoder);
        }
        else
        {
            onResume();
            toast.toastMessage("The QR does not meet the Green Pass policy");
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
    private void uploadQRGreenPassStorage(QRGEncoder qrgEncoder)
    {
        Bitmap bitmap = qrgEncoder.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        StorageReference ref = storageReference.child("users").child(mAuth).child("GreenPassQR");
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        toast.toastMessage("Uploading Failed please verify you network connection");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
    }
    private void initScannerImplements()
    {
        scannerView=new ZXingScannerView(this);
        setContentView(scannerView);

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent qrIntent;
        qrIntent = new Intent(QRScannerActivity.this, LoginActivity.class);
        QRScannerActivity.this.startActivity(qrIntent);
        QRScannerActivity.this.finish();
    }

}