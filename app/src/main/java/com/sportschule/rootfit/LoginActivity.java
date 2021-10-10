package com.sportschule.rootfit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.sportschule.rootfit.Admin.AdminActivity;
import com.sportschule.rootfit.Trainee.TraineeActivity;
import com.sportschule.rootfit.Trainer.ManageForPastPlanTraining;
import com.sportschule.rootfit.Trainer.TrainerActivity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    ArrayMap<String,Integer> StatisticsHashMap = new ArrayMap<>();
    ProgressBar loginProgressBar;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private final DatabaseReference Statistics = dbPhone.getReference().child("Statistics");
    private final Map<String, String> trainerUID = new HashMap<>();
    private final LinkedHashSet<String> traineeUID = new LinkedHashSet<String>();
    private static final String SHARED_PREFS = "sharedPrefs";
    private SharedPreferences sharedPreferences;
    private View parentLayout;
    private EditText email,password;
    private String expert="";
    private String userState,firstName,lastName,userAge,emailAddress,userName,phoneNum,gender;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        parentLayout = findViewById(android.R.id.content);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        Statistics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Log.d(TAG, ds.toString());
                    StatisticsHashMap.put(ds.getKey(),Integer.parseInt(ds.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if((!sharedPreferences.getString("lastEmailLogin", "").equals(""))&&
                !sharedPreferences.getString("lastPassLogin", "").equals(""))
        {
            email.setText(sharedPreferences.getString("lastEmailLogin", ""));
            password.setText(decodeBase64(sharedPreferences.getString("lastPassLogin", "")));
        }
        if(mAuth.getUid() != null) {mAuth.signOut(); }
        permissionStorageDialog();
        CollectUserDateUID();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
    }
    /*andrew*/
    public void smsOTP(View view){

            Intent mainIntent = new Intent(LoginActivity.this, SendOTPActivity.class);
            mainIntent.putExtra("userState",userState);
            mainIntent.putExtra("activityState","LoginActivity");
            LoginActivity.this.startActivity(mainIntent);
            LoginActivity.this.finish();
    }

    public void loginButton(View view) {
		loginProgressBar = findViewById(R.id.login_ProgressBar);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        if (!email.getText().toString().equals("") && !password.getText().toString().equals("")) {
			loginProgressBar.setVisibility(View.VISIBLE);
            checkCredentials(password.getText().toString(),email);
        }
        else
        {
			loginProgressBar.setVisibility(View.INVISIBLE);
            Snackbar.make(parentLayout, "Username/Password fields are required",
                    Snackbar.LENGTH_LONG).show();
            email.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
        }
    }

    public void registrationButton(View view)
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        LoginActivity.this.startActivity(registerIntent);
        LoginActivity.this.finish();
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
    private void switchLoggedActivity()
    {
        Intent loginIntent;
        {
            switch(userState) {
                case "Admin":
                    loginIntent = new Intent(LoginActivity.this, AdminActivity.class);
                    break;
                case "Trainee":
                    loginIntent = new Intent(LoginActivity.this, TraineeActivity.class);
                    break;
                case "Trainer":
                    loginIntent = new Intent(LoginActivity.this, TrainerActivity.class);
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
                editor.putString(mAuth.getUid()+"expert",expert);
            }
            editor.putStringSet("USER_CONTACT", setStringPref);
            editor.putString(mAuth.getUid()+"firstName",firstName);
            editor.putString(mAuth.getUid()+"lastName",lastName);
            editor.putString("lastEmailLogin",emailAddress);
            editor.putString("lastPassLogin",encodeBase64(password.getText().toString()));
            editor.apply();
            editor.commit();
            ConfigureUID.setUID(mAuth.getUid());
            UpdateStatisticsDB();
            toast.toastMessage("Welcome Back, "+firstName+" "+lastName,userState.equals("Admin")?Gravity.BOTTOM:Gravity.CENTER);
            LoginActivity.this.startActivity(loginIntent);
            LoginActivity.this.finish();
        }
    }

    public void forgetPasswordButton(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        final View customLayout = getLayoutInflater().inflate(R.layout.credential_dialog, null);
        builder.setView(customLayout);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText inputLine = customLayout.findViewById(R.id.input_line);
                if(!inputLine.getText().toString().equals(""))
                {
                    mAuth.sendPasswordResetEmail(inputLine.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(parentLayout, "Email has sent, Please check you email box",
                                        Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(parentLayout, "Unexpected occurs\nContact us",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void extractDataFromUserUID(ProgressDialog dialog) {
        try {
            DocumentReference docRef = db.collection("users").document(mAuth.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userTheResult(document.get("State").toString(), document.get("Username").toString(), document.get("First").toString(), document.get("Last").toString(),
                                    document.get("Age").toString(), document.get("Email").toString(),document.get("Phone").toString(),document.get("Gender").toString());
                            if(userState.equals("Trainer"))
                            {
                                expert = (document.get("Expert").toString());
                            }
                            Log.i("usernameToAccount", "extract data from account success");
                            dialog.dismiss();
                            if(userState.equals("Admin")){
                                switchLoggedActivity();
                            }
                            else
                            {

                                checkUserGreenPass();
                            }
                        } else {
                            Log.i("nodocument", "document not exists");
                            toast.toastMessage("Account doesn't exists, Press on registration or contact US if it mistake");
                            dialog.dismiss();
                            loginProgressBar.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Snackbar.make(parentLayout, "Connection to firebase failed",
                                Snackbar.LENGTH_LONG).show();
                        Log.d("error", "unexpected error");
                        dialog.dismiss();
                    }
                }
            });
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            Snackbar.make(parentLayout, "Connection to firebase failed",
                    Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    public void checkCredentials(String password, EditText email) {
		ProgressBar loginProgressBar = findViewById(R.id.login_ProgressBar);
        Log.i("checkCredentials","checkCredentials");
        ProgressDialog dialog = ProgressDialog.show(this, "Authentication..", "Please wait....", true);
        mAuth.signInWithEmailAndPassword(email.getText().toString(),password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG", "signInWithEmail:success");
                    extractDataFromUserUID(dialog);
                } else {
					loginProgressBar.setVisibility(View.INVISIBLE);
                    Log.w("TAG", "signInWithEmail:failure", task.getException());
                    dialog.dismiss();
                    Snackbar.make(parentLayout, "Authentication failed",
                            Snackbar.LENGTH_LONG).show();
                    email.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                }
                else {
					Snackbar.make(parentLayout, "Apply the SMS sending,storage permission,Allow to read,write from storage",
                            Snackbar.LENGTH_LONG).show();
                    Log.i("perrmission","perrmission denied please allow read external the data");
                    permissionStorageDialog();
                }
                return;
            }
        }
    }
    private void permissionStorageDialog()
    {
        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                             Manifest.permission.SEND_SMS
                            },
                12);
    }
	
	private void CollectUserDateUID() {
        try{
            db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for(DocumentSnapshot qds : queryDocumentSnapshots.getDocuments())
                    {
                        if (qds.get("State").equals("Trainer")) {
                            trainerUID.put(qds.getId(),qds.get("Expert").toString());
                        }
                    }
                    swapPastDateToPastPath();
                }
            });
        }
        catch(NullPointerException e){
            e.printStackTrace();
            Log.e(TAG, "CollectUserDateUID: " + e.getMessage() );
            Snackbar.make(parentLayout, "Connection to firebase failed",
                    Snackbar.LENGTH_LONG).show();

        }

    }

    private void swapPastDateToPastPath() {
        ManageForPastPlanTraining manageForPastPlanTraining = new
                ManageForPastPlanTraining();
        Iterator<Map.Entry<String, String>> iteratorTrainer = trainerUID.entrySet().iterator();
        Iterator<String> iteratorTrainee = traineeUID.iterator();
        while (iteratorTrainer.hasNext())
        {
            Map.Entry entryTrainer = iteratorTrainer.next();
            manageForPastPlanTraining.movePreviousTrainerTraining(entryTrainer.getValue().toString(),entryTrainer.getKey().toString());
        }
    }
    public void checkUserGreenPass()
    {
        Intent greenPassIntent = new Intent(LoginActivity.this, QRScannerActivity.class);
        storageReference.child("users").child(mAuth.getUid()).child("GreenPassQR").getDownloadUrl().
                addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        switchLoggedActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                String errorMessage = exception.getMessage();
                Log.i("NoGreenPass", String.valueOf(errorCode));
                loginProgressBar.setVisibility(View.INVISIBLE);
                greenPassIntent.putExtra("mAuth",mAuth.getUid());
                greenPassIntent.putExtra("activity","loginActivity");
                LoginActivity.this.startActivity(greenPassIntent);
            }
        });
    }
    private String encodeBase64(String encodePass)
    {
        byte[] data = new byte[0];
        data = encodePass.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }
    private String decodeBase64(String decodePass)
    {
        byte[] data = Base64.decode(decodePass, Base64.DEFAULT);
        return new String(data, StandardCharsets.UTF_8);
    }

    private void UpdateStatisticsDB() {
        /*Log.d(TAG, "UpdateStatisticsDB: " + StatisticsHashMap);*/
        int SignInCntr;

        SignInCntr =StatisticsHashMap.get("SignIN");

        SignInCntr++;

        Statistics.child("SignIN").setValue(SignInCntr);

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        startActivity(intent);

    }
}