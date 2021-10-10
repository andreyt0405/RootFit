package com.sportschule.rootfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sportschule.rootfit.Admin.AdminActivity;
import com.sportschule.rootfit.Trainee.TraineeActivity;
import com.sportschule.rootfit.Trainer.TrainerActivity;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;


public class VerifyOTPActivity extends AppCompatActivity {


    private EditText inputCode1,inputCode2,inputCode3,inputCode4,inputCode5,inputCode6;
    private String verificationId,inputMobile;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mobAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private HashMap<String, String> firebasePhoneAndIDList = new HashMap<String, String>();
    HashMap <String,String> StatisticsHashMap = new HashMap<>();
    private final DatabaseReference Statistics = dbPhone.getReference().child("Statistics");
    private static final String SHARED_PREFS = "sharedPrefs";
    private SharedPreferences sharedPreferences;
    private static final String TAG = "verifyOTPActivity";
    private String expert="";
    private String userID,userState,firstName,lastName,userAge,emailAddress,userName,
            phoneNum,gender,activityState;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);
        ActionBar actionBar = getSupportActionBar();
        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        actionBar.hide();
        /* reteieve data fields from sendOTPActivity verificationId, inputMobile, firebasePhoneandIDlist*/
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            verificationId = extras.getString("verificationId");
            inputMobile = extras.getString("mobile");
            activityState = extras.getString("activityState");
        }
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        Log.i("VerifyActivity1",inputMobile);
        firebasePhoneAndIDList = (HashMap<String, String>) getIntent().getSerializableExtra("firebasePhoneIDList");
        userID = readPhoneUserID("0"+inputMobile);
        Log.d(TAG,"user ID found: " + userID);
        TextView textMobile = findViewById(R.id.textMobile);
        textMobile.setText(String.format("+972-%s",inputMobile));/*cut the zero to show to user!*/
        Log.d(TAG,"Phone entered: " + inputMobile);
        Log.d(TAG, firebasePhoneAndIDList.toString());

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        Button buttonVerify = findViewById(R.id.buttonVerify);

        setupOTPInputs();

        Statistics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    StatisticsHashMap.put(ds.getKey(),ds.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(inputCode1.getText().toString().trim().isEmpty()
                        || inputCode2.getText().toString().trim().isEmpty()
                        || inputCode3.getText().toString().trim().isEmpty()
                        || inputCode4.getText().toString().trim().isEmpty()
                        || inputCode5.getText().toString().trim().isEmpty()
                        || inputCode6.getText().toString().trim().isEmpty()){
                    toast.toastMessage("Please enter valid code");
                    return;
                }


                String code =
                        inputCode1.getText().toString() +
                                inputCode2.getText().toString() +
                                inputCode3.getText().toString() +
                                inputCode4.getText().toString() +
                                inputCode5.getText().toString() +
                                inputCode6.getText().toString();

                Log.d(TAG,"Code Entered: " + code);
                if(verificationId != null){
                    buttonVerify.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    mobAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    buttonVerify.setVisibility(View.VISIBLE);
                                    if(task.isSuccessful()){
                                        UpdateStatisticsDB();
                                        FirebaseUser user = task.getResult().getUser();
                                        String PhoneAuthID1 = mobAuth.getUid();
                                        String PhoneAuthID2 = user.getUid();
                                        Log.d(TAG, "mobAuth ID OTP AUTH: " + PhoneAuthID1);
                                        Log.d(TAG, "user ID OTP AUTH: " + PhoneAuthID2);
                                        Log.d(TAG, "signInWithOTP:success");
                                        extractDataFromUserUID();
                                    }
                                    else{
                                        Log.w(TAG, "signInWithPhone:failure", task.getException());
                                        toast.toastMessage("The verification code entered was invalid");
                                    }
                                }
                            });
                }
            }
        });

        findViewById(R.id.textResendOTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SendActiivy1",inputMobile);
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mobAuth)
                        .setPhoneNumber("+972" + inputMobile)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(VerifyOTPActivity.this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(String newVerificationId,
                                                   PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);
                                verificationId = newVerificationId;
                                toast.toastMessage("NEW Password Sent");
                            }

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential Credential) {

                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Log.e(TAG, "onVerificationFailed: ",e);
                                toast.toastMessage(e.getMessage());
                            }
                        })
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
    }

    private void UpdateStatisticsDB() {
        /*Log.d(TAG, "UpdateStatisticsDB: " + StatisticsHashMap);*/
        int SignInCntr;

        SignInCntr = Integer.valueOf(StatisticsHashMap.get("SignIN"));

        SignInCntr++;

        Statistics.child("SignIN").setValue(SignInCntr);

    }

    private String readPhoneUserID(String phoneNum_in){
        Log.d(TAG, "phoneNum_in: " + phoneNum_in);
        Log.d(TAG, "Firebase list copy: " + firebasePhoneAndIDList.toString());
        return firebasePhoneAndIDList.get(phoneNum_in);
    }

    private void userTheResult(String userStates, String username, String firstname, String lastname, String age,String email,String phone,String sexual) {
        userState = userStates;
        userName = username;
        firstName = firstname;
        lastName = lastname;
        userAge = age;
        emailAddress = email;
        phoneNum =phone;
        gender = sexual;
    }

    private void switchLoggedActivity() {
        Intent LoginIntent = null;
        {
            switch(userState) {
                case "Admin":
                    LoginIntent = new Intent(VerifyOTPActivity.this, AdminActivity.class);
                    break;
                case "Trainee":
                    switch (activityState)
                    {
                        case "LoginActivity":
                            LoginIntent = new Intent(VerifyOTPActivity.this, TraineeActivity.class);
                            break;
                        case "RemoveAccount":
                            LoginIntent = new Intent(VerifyOTPActivity.this, MainActivity.class);
                            new RemoveUserAccount(ConfigureUID.getUID());
                            break;
                    }
                    break;
                case "Trainer":
                    switch (activityState)
                    {
                        case "LoginActivity":
                            LoginIntent = new Intent(VerifyOTPActivity.this, TrainerActivity.class);
                            break;
                        case "RemoveAccount":
                            LoginIntent = new Intent(VerifyOTPActivity.this, MainActivity.class);
                            new RemoveUserAccount(ConfigureUID.getUID());
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + userState);
            }
            SharedPreferences.Editor editor =  sharedPreferences.edit();
            LinkedHashSet<String> setStringPref = new LinkedHashSet<String>();
            setStringPref.add("firstName:"+firstName);
            setStringPref.add("lastName:"+lastName);
            setStringPref.add("userName:"+userName);
            setStringPref.add("emailAddress:"+emailAddress);
            setStringPref.add("userAge:"+userAge);
            setStringPref.add("userState:"+userState);
            setStringPref.add("userPhone:"+phoneNum);
            setStringPref.add("userGender:"+gender);
            if(!expert.equals(""))
            {
                setStringPref.add("expert:"+expert);
                editor.putString(userID+"expert",expert);
            }
            editor.putStringSet("USER_CONTACT", setStringPref);
            editor.putString(userID+"firstName",firstName);
            editor.putString(userID+"lastName",lastName);
            editor.apply();
            editor.commit();
            ConfigureUID.setUID(userID);
            Log.i("userIDVerifyAc",userID);
            if(activityState.equals("LoginActivity")) {
                VerifyOTPActivity.this.startActivity(LoginIntent);
                firebasePhoneAndIDList.clear();
                toast.toastMessage("Welcome Back,"+firstName+" "+lastName, userState.equals("Admin")?Gravity.BOTTOM:Gravity.CENTER);}
            if(activityState.equals("RemoveAccount"))
            {
                sharedPreferences.edit().clear().apply();
                toast.toastMessage(R.string.account_removed);
                startActivity(LoginIntent);
                overridePendingTransition(0, 0);
            }
            VerifyOTPActivity.this.finish();
        }
    }

    private void extractDataFromUserUID() {
        try {
            DocumentReference docRef = db.collection("users").document(userID);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userTheResult(document.get("State").toString(), document.get("Username").toString(), document.get("First").toString(), document.get("Last").toString(),
                                    document.get("Age").toString(), document.get("Email").toString(),document.get("Phone").toString(),document.get("Gender").toString());
                            if(document.get("State").equals("Trainer"))
                            {
                                expert = (document.get("Expert").toString());
                            }
                            Log.i("usernameToAccount", "extract data from account success");
                            switchLoggedActivity();

                        } else {
                            Log.i("nodocument", "document not exists");
                        }
                    } else {
                        toast.toastMessage("Connection to firebase failed");
                        Log.d("error", "unexpected error");

                    }
                }
            });
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            toast.toastMessage("Connection to firebase failed");
        }
    }

    private void setupOTPInputs(){
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }
}

