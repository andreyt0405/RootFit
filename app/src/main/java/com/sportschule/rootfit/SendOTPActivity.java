package com.sportschule.rootfit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.sportschule.rootfit.Trainee.TraineeActivity;
import com.sportschule.rootfit.Trainer.TrainerActivity;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class SendOTPActivity extends AppCompatActivity {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private final DatabaseReference root = dbPhone.getReference().child("PhoneUsers");
    private final HashMap<String, String> firebasePhoneandIDlist = new HashMap<String, String>();
    private static final String TAG = "sendOTPActivity";
    private EditText inputMobile;
    private String phoneNumber;
    private ProgressBar progressBar;
    private Button buttonGetOTP;
    private String onBackPressedSWactivity = "LoginActivity";
    private String userID,userState,activityState;
    private boolean inputFlag = false;


    @Override
    public void onBackPressed() {
        Intent mainIntent;
        if(activityState.equals("LoginActivity")) {
            switch (onBackPressedSWactivity) {
                case "RegisterActivity":
                    mainIntent = new Intent(SendOTPActivity.this, RegisterActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    SendOTPActivity.this.startActivity(mainIntent);
                    break;
                default:
                    mainIntent = new Intent(SendOTPActivity.this, LoginActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    SendOTPActivity.this.startActivity(mainIntent);
                    break;
            }
        }
        else {
            switch (userState) {
                case "Trainer":
                    mainIntent = new Intent(SendOTPActivity.this, TrainerActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    SendOTPActivity.this.startActivity(mainIntent);
                    break;
                case "Trainee":
                    mainIntent = new Intent(SendOTPActivity.this, TraineeActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    SendOTPActivity.this.startActivity(mainIntent);
                    break;
            }
        }
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_o_t_p);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        inputMobile = findViewById(R.id.inputMobile);
        progressBar = findViewById(R.id.progressBar);
        buttonGetOTP = findViewById(R.id.buttonGetOTP);
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        firebasePhoneandIDlist.clear();
        Bundle extras = getIntent().getExtras();
        if (extras != null) { userState = extras.getString("userState");
            activityState =  extras.getString("activityState");}
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    firebasePhoneandIDlist.put(ds.getKey(),ds.getValue(String.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error){
                Log.e(TAG,error.getMessage());
            }
        });

    }
    private boolean checkIfPhoneNumberExists(String phoneNum, HashMap<String, String> firebasePhoneandIDlist) {
        String hashMapReply;
        hashMapReply = firebasePhoneandIDlist.get("0"+phoneNum);
        /*Log.d(TAG, hashMapReply);*/
        /*if hushmap reply is null then number missing!*/
        return hashMapReply != null;
    }



    public void getOTP(View view) {
        inputFlag = false;
        Log.d(TAG, firebasePhoneandIDlist.toString());
        Log.i("inputMobiletext",inputMobile.getText().toString().trim());
        phoneNumber=inputMobile.getText().toString().startsWith("0")?
                inputMobile.getText().toString().substring(1):inputMobile.getText().toString();
        if(inputMobile.getText().toString().trim().isEmpty()){
            toast.toastMessage("Enter Valid Mobile Number");
            inputFlag = true;
        }
        if(!(checkIfPhoneNumberExists(phoneNumber,firebasePhoneandIDlist)) && !(inputFlag)){
            toast.toastMessage("Phone Number does not registered! Press back to Register/SignUp");
            onBackPressedSWactivity = "RegisterActivity";
            inputFlag = true;
            Log.d(TAG, "input flag: " + inputFlag);
        }
        if(inputFlag == false){
            checkUserGreenPass(phoneNumber);
        }
    }
    private void fireBaseSendOTPCode() {
        if (!(inputFlag)) {
            progressBar.setVisibility(View.VISIBLE);
            buttonGetOTP.setVisibility(View.INVISIBLE);
            Log.i("SendActiivy",phoneNumber);
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder((mAuth))
                    .setPhoneNumber("+972" + phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(SendOTPActivity.this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onCodeSent(String verificationId,
                                               PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(verificationId, forceResendingToken);
                            progressBar.setVisibility(View.GONE);
                            buttonGetOTP.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(SendOTPActivity.this, VerifyOTPActivity.class);
                            intent.putExtra("mobile", phoneNumber);
                            intent.putExtra("verificationId", verificationId);
                            intent.putExtra("firebasePhoneIDList", firebasePhoneandIDlist);
                            intent.putExtra("activityState",activityState);
                            SendOTPActivity.this.startActivity(intent);

                        }

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential Credential) {
                            progressBar.setVisibility(View.GONE);
                            buttonGetOTP.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            progressBar.setVisibility(View.GONE);
                            buttonGetOTP.setVisibility(View.VISIBLE);
                            Log.e(TAG, "onVerificationFailed: ", e);
                            toast.toastMessage(e.getMessage());
                        }
                    })
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
    }
    public void checkUserGreenPass(String phoneNumber)
    {
        userID = (firebasePhoneandIDlist.get("0"+phoneNumber));
        Intent greenPassIntent = new Intent(SendOTPActivity.this, QRScannerActivity.class);
        storageReference.child("users").child(userID).child("GreenPassQR").getDownloadUrl().
                addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("scanned","green pass has been scanned successfully");
                        fireBaseSendOTPCode();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                String errorMessage = exception.getMessage();
                Log.i("NoGreenPass", String.valueOf(errorCode));
                greenPassIntent.putExtra("mAuth",userID);
                SendOTPActivity.this.startActivity(greenPassIntent);
            }
        });
    }
}