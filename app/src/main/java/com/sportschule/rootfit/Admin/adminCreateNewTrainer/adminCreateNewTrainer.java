package com.sportschule.rootfit.Admin.adminCreateNewTrainer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sportschule.rootfit.Admin.AdminActivity;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.ToastCustomMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class adminCreateNewTrainer extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private final DatabaseReference root = dbPhone.getReference("");
    private final DatabaseReference Statistics = dbPhone.getReference().child("Statistics");
    ArrayList<String> firebasePhoneList = new ArrayList<>();
    HashMap<String,String> StatisticsHashMap = new HashMap<>();
    Map<String, EditText> account = new HashMap<>();
    Map<String, String> db_account = new HashMap<>();
    private static final String TAG = "adminCreateNewTrainer";
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    private boolean invalidFiled = true;
    private boolean PhoneNumFound = true;
    private EditText val;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(adminCreateNewTrainer.this, AdminActivity.class);
        adminCreateNewTrainer.this.startActivity(intent);
        adminCreateNewTrainer.this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_new_trainer);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        final Spinner spinner = findViewById(R.id.trainer_Expert);
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item_custome_new_trainer,
                getResources().getStringArray(R.array.spinnerTraineePlanItem));
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getSelectedItem().toString();
                db_account.put("Expert",selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Statistics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Log.d(TAG, "user Exists: datasnapshot: " + ds);
                    StatisticsHashMap.put(ds.getKey(),ds.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        root.child("PhoneUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d(TAG, "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error){
                Log.e(TAG,error.getMessage());
            }
        });
        root.child("PhoneUsers").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d(TAG, "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d(TAG, "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d(TAG, "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d(TAG, "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                toast.toastMessage("Failed to load comments.");
            }
        });
    }

    public void continueButton(View view) {
        radioSexGroup = (RadioGroup)findViewById(R.id.radioGroup);
        EditText nameEditText = findViewById(R.id.trainer_name);
        EditText lastEditText = findViewById(R.id.trainer_last);
        EditText usernameEditText = findViewById(R.id.trainer_username);
        EditText emailEditText = findViewById(R.id.trainer_email);
        EditText passEditText = findViewById(R.id.trainer_password);
        EditText ageEditText = findViewById(R.id.trainer_age);
        EditText phoneEditText = findViewById(R.id.trainer_phone);
        ImageView error_gender = findViewById(R.id.error_icon_gender);
        int selectedId=radioSexGroup.getCheckedRadioButtonId();
        radioSexButton=(RadioButton)findViewById(selectedId);
        /*Log.d(TAG, "gender selected: " + radioSexButton.getText().toString());*/
        account.put("First", nameEditText);
        account.put("Last", lastEditText);
        account.put("Username", usernameEditText);
        account.put("Pass", passEditText);
        account.put("Email", emailEditText);
        account.put("Age", ageEditText);
        account.put("Phone", phoneEditText);
        String name = nameEditText.getText().toString();
        if(!name.equals("")) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        db_account.put("First", name);
        db_account.put("Last", lastEditText.getText().toString());
        db_account.put("Username", usernameEditText.getText().toString().toLowerCase());
        db_account.put("Email", emailEditText.getText().toString());
        db_account.put("Age", ageEditText.getText().toString());
        db_account.put("Phone", phoneEditText.getText().toString());
        try {db_account.put("Gender", radioSexButton.getText().toString());}
        catch (Exception e){
            error_gender.setVisibility(View.VISIBLE);
            toast.toastMessage("Input Error!Please choose Gender!");
            new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    error_gender.setVisibility(View.INVISIBLE);
                }
            }.start();
            return;
        }
        db_account.put("State", "Trainer");
        fieldValidation(account,emailEditText);
        checkIfPhoneNumberExists(phoneEditText.getText().toString(), firebasePhoneList,phoneEditText);

        if (invalidFiled & PhoneNumFound) {
            UpdateStatisticsDB(db_account);
            userEmailAuthNewAccount(emailEditText,passEditText.getText().toString(),db_account);
        }
    }

    private void UpdateStatisticsDB(Map<String, String> db_account) {
        Log.d(TAG, "UpdateStatisticsDB: " + StatisticsHashMap);
        String tempGender;
        int ageStep = 0,tempAge,MaleCounter,FemaleCounter,counterAge_11_20,counterAge_21_30,counterAge_31_40,counterAge_41_50,counterAge_51_60,counterAge_61_70,counterAge_71_80,counterAge_81_90,counterAge_91_99;

        MaleCounter = Integer.valueOf(StatisticsHashMap.get("Male"));
        FemaleCounter = Integer.valueOf(StatisticsHashMap.get("Female"));
        counterAge_11_20 = Integer.valueOf(StatisticsHashMap.get("age:11->20"));
        counterAge_21_30 = Integer.valueOf(StatisticsHashMap.get("age:21->30"));
        counterAge_31_40 = Integer.valueOf(StatisticsHashMap.get("age:31->40"));
        counterAge_41_50 = Integer.valueOf(StatisticsHashMap.get("age:41->50"));
        counterAge_51_60 = Integer.valueOf(StatisticsHashMap.get("age:51->60"));
        counterAge_61_70 = Integer.valueOf(StatisticsHashMap.get("age:61->70"));
        counterAge_71_80 = Integer.valueOf(StatisticsHashMap.get("age:71->80"));
        counterAge_81_90 = Integer.valueOf(StatisticsHashMap.get("age:81->90"));
        counterAge_91_99 = Integer.valueOf(StatisticsHashMap.get("age:91->99"));


        tempGender = db_account.get("Gender");
        tempAge = Integer.valueOf(db_account.get("Age"));


        if(tempAge>=11 && tempAge<=20){ counterAge_11_20++; ageStep = 1; }
        else if (tempAge>=21 && tempAge<=30){ counterAge_21_30++; ageStep = 2; }
        else if (tempAge>=31 && tempAge<=40){ counterAge_31_40++; ageStep = 3; }
        else if (tempAge>=41 && tempAge<=50){ counterAge_41_50++; ageStep = 4; }
        else if (tempAge>=51 && tempAge<=60){ counterAge_51_60++; ageStep = 5; }
        else if (tempAge>=61 && tempAge<=70){ counterAge_61_70++; ageStep = 6; }
        else if (tempAge>=71 && tempAge<=80){ counterAge_71_80++; ageStep = 7; }
        else if (tempAge>=81 && tempAge<=90){ counterAge_81_90++; ageStep = 8; }
        else if (tempAge>=91 && tempAge<=99){ counterAge_91_99++; ageStep = 9; }
        else {ageStep = 10; }

        if(tempGender.equalsIgnoreCase("Male")){MaleCounter++;}
        if(tempGender.equalsIgnoreCase("Female")){FemaleCounter++;}

        if(tempGender.equalsIgnoreCase("Male")){Statistics.child("Male").setValue(MaleCounter);}
        if(tempGender.equalsIgnoreCase("Female")){Statistics.child("Female").setValue(FemaleCounter);}


        switch(ageStep){
            case 1:Statistics.child("age:11->20").setValue(counterAge_11_20);
                break;
            case 2:Statistics.child("age:21->30").setValue(counterAge_21_30);
                break;
            case 3:Statistics.child("age:31->40").setValue(counterAge_31_40);
                break;
            case 4:Statistics.child("age:41->50").setValue(counterAge_41_50);
                break;
            case 5:Statistics.child("age:51->60").setValue(counterAge_51_60);
                break;
            case 6:Statistics.child("age:61->70").setValue(counterAge_61_70);
                break;
            case 7:Statistics.child("age:71->80").setValue(counterAge_71_80);
                break;
            case 8:Statistics.child("age:81->90").setValue(counterAge_81_90);
                break;
            case 9:Statistics.child("age:91->99").setValue(counterAge_91_99);
                break;
            default:
                break;
        }
    }

    private void checkIfPhoneNumberExists(String phoneNum, ArrayList<String> firebasePhoneList,EditText phoneEditText) {
        PhoneNumFound = true;
        for(String i:firebasePhoneList){
            /*Log.d("checkIfPhoneNumberExists", "check..." + i.equals(phoneNum));*/
            if(i.equals(phoneNum)){
                toast.toastMessage("Phone Number already exists in DB!please enter different one!");
                /*Log.d("myFlag", "changePhoneNumFound() called --> PhoneNumFound = false;");*/
                phoneEditText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_action_error,0);
                changePhoneNumFound();
                return;
            }
        }
    }


    private void userAccountAuthNewAccount(Map account) {
        Map<String, Object> updatedTrainerStatistic = new HashMap<>();
        root.child("PhoneUsers").child(account.get("Phone").toString()).setValue(mAuth.getUid());/*andrew 23052021*/
        db.collection("users").document(mAuth.getUid()).set(account);
        root.child("Statistics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean traineeKeyExist = false;
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    if(ds.getKey().equals("Trainer")) {
                        updatedTrainerStatistic.put("Trainer",Integer.parseInt(ds.getValue().toString())+1);
                        Log.i("parseInt",String.valueOf(Integer.parseInt(ds.getValue().toString())+1));
                        root.child("Statistics").updateChildren(updatedTrainerStatistic);
                        traineeKeyExist =true;
                    }
                }
                if(!(traineeKeyExist)) {
                    updatedTrainerStatistic.put("Trainer", 0);
                    root.child("Statistics").updateChildren(updatedTrainerStatistic);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mAuth.signOut();/*andrew*/
        Intent adminIntent = new Intent(adminCreateNewTrainer.this, AdminActivity.class);
        adminCreateNewTrainer.this.startActivity(adminIntent);
        adminCreateNewTrainer.this.finish();
    }

    @SuppressLint("ResourceAsColor")
    public void fieldValidation(Map account,EditText emailEditText) {
        invalidFiled = true;
        if (!emailEditText.getText().toString().equals("")) {
            Iterator<Map.Entry<String, EditText>> iterator = account.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                val = (EditText) entry.getValue();
                val.setCompoundDrawablesWithIntrinsicBounds(setDrawable(entry.getKey().toString()), 0, 0, 0);
                if (val.getText().toString().equals("")) {
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
                if(entry.getKey().toString().equals("Pass") && val.getText().toString().length() < 8)
                {
                    toast.toastMessage(R.string.prompt_weak_password);
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
                if(entry.getKey().toString().equals("Email") && !val.getText().toString().contains("@"))
                {
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
                try {
                    if (entry.getKey().toString().equals("Age")) {
                        if (Integer.parseInt(val.getText().toString()) > 99 || Integer.parseInt(val.getText().toString()) <= 10) {
                            val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                            changeInvalidString();

                        }
                    }
                } catch (NumberFormatException e) {
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
                if(entry.getKey().toString().equals("First") && (val.getText().toString().matches(".*\\d.*")||val.getText().toString().matches(".*[^A-Za-z0-9].*")))
                {
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
                if(entry.getKey().toString().equals("Last") && (val.getText().toString().matches(".*\\d.*")||val.getText().toString().matches(".*[^A-Za-z0-9].*")))
                {
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
                if(entry.getKey().toString().equals("Phone") && (val.getText().toString().length() < 10 || val.getText().toString().length() > 10))
                {
                    toast.toastMessage(R.string.prompt_Error_Phone_NUMBER);
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    changeInvalidString();
                }
            }
        }
        else {
            Iterator<Map.Entry<String, EditText>> iterator = account.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                val = (EditText) entry.getValue();
                if (val.getText().toString().equals("") || (entry.getKey() == "Email" && !val.getText().toString().contains("@")) ||
                        (entry.getKey() == "Age" && Integer.parseInt(val.getText().toString()) <=10
                                || entry.getKey() == "Age" && Integer.parseInt(val.getText().toString()) > 99) || (entry.getKey() == "Pass" &&
                        val.getText().toString().length() < 8)) {
                    val.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                    val.getText().clear();
                } else {
                    val.setCompoundDrawablesWithIntrinsicBounds(setDrawable(entry.getKey().toString()), 0, 0, 0);

                }

            }
            changeInvalidString();
        }
    }
    private int setDrawable(String id) {
        switch(id) {
            case "First":
                return R.drawable.ic_action_name;
            case "Username":
                return R.drawable.ic_action_login;
            case "Email":
                return R.drawable.ic_action_email;
            case "Pass":
                return R.drawable.ic_action_pass;
            case "Age":
                return R.drawable.ic_action_age;
            case "Phone":
                return R.drawable.ic_action_phone;
            case "Expert":
                return R.drawable.ic_action_phone;
            default:
                return R.drawable.ic_action_name;
        }
    }
    private void userEmailAuthNewAccount(EditText email, String password,Map db_account){
        String msgTEXT = db_account.get("First") + " Welcome to Rootfit app! Your trainer account activated! Your Password:" + password + " ,your EMAIL " + email.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("System registration");
        builder.setMessage("Account registration...\nPlease wait. ");
        builder.setCancelable(true);
        final AlertDialog dlg = builder.create();
        dlg.show();
        if(!email.getText().toString().equals("") && email.getText().toString().contains("@")) {
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dlg.dismiss();
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                sendSMSmsg(db_account.get("Phone").toString(),msgTEXT);
                                mAuth.signInWithEmailAndPassword(email.getText().toString(),password);
                                userAccountAuthNewAccount(db_account);
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure Google Authentication", task.getException());
                                email.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                                toast.toastMessage(R.string.prompt_invalid_email);
                                email.getText().clear();
                            }
                        }
                    });
        }
        else
        {
            email.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
            toast.toastMessage(R.string.prompt_invalid_email);
        }
    }

    private void sendSMSmsg(String phoneNo, String msgTXT) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, msgTXT, null, null);
    }

    private void changeInvalidString() {  invalidFiled = false; }
    private void changePhoneNumFound() {  PhoneNumFound = false; }
}