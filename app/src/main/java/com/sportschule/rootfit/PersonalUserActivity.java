package com.sportschule.rootfit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sportschule.rootfit.Trainee.TraineeActivity;
import com.sportschule.rootfit.Trainer.TrainerActivity;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersonalUserActivity extends AppCompatActivity {
    private static boolean FOUND_PHONE = false;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    DatabaseReference realtimeRef = dbPhone.getReference("");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference Statistics = dbPhone.getReference().child("Statistics");
    ArrayMap<String,Integer> StatisticsHashMap = new ArrayMap<>();
    ImageView profile;
    private int ageBeforeUpdate = 0;
    private static final String TAG = "PhoneValidation";
    private static final String SHARED_PREFS = "sharedPrefs";
    private SharedPreferences sharedPreferences;
    private String userState;
    private static final int GET_FROM_GALLERY = 3;
    private final List<String> list = new ArrayList<String>();
    private ArrayAdapter adapter;

    @Override
    public void onBackPressed() {
        Log.i("ActionNotAllow", "Action not allowed press on menu");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        setContentView(R.layout.activity_personal_user);
        TextView personalName = findViewById(R.id.personal_welcome_name);
        Set<String> keys = sharedPreferences.getStringSet("USER_CONTACT", new LinkedHashSet<>());
        String[] user_contact = keys.toArray(new String[keys.size()]);
        List<String> stringList = new ArrayList<String>(Arrays.asList(user_contact));
        Collections.sort(stringList);
        for (String key : stringList) {
            if (key.contains("firstName")) {
                personalName.setText(key.substring(key.indexOf(":") + 1).trim() + " " +
                        sharedPreferences.getString(mAuth.getCurrentUser().getEmail() + "lastName", "") + "\nPersonal Area");
            }
            if (key.contains("userState")) {
                userState = key.substring(key.indexOf(":") + 1).trim();
            }
            if (!key.contains("expert")) {
                list.add(convertSharedPreferencesKeyToString(key) + " " + key.substring(key.indexOf(":") + 1).trim());
            }
        }
        if (!sharedPreferences.getString(ConfigureUID.getUID() + "expert", "").equals("")) {
            list.add("Expertise:" + " " + sharedPreferences.getString(ConfigureUID.getUID() + "expert", ""));
        }
        ListView personalInfo = findViewById(R.id.personal_info_list);
        adapter = new ArrayAdapter(PersonalUserActivity.this, R.layout.support_simple_spinner_dropdown_item, list);
        personalInfo.setAdapter(adapter);
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        personalInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0 && position != 4 && position != 5 && position != 7) {
                    showDialogScreen(list.get(position), position, id);
                } else {
                    toast.toastMessage("There are no editing options\nContact Administrator");
                }
            }
        });
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
        loadProfileImageSharedPref();
    }

    public void uploadProfileImage(View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ImageView profileImage = findViewById(R.id.upload_profile_trainee);
                profileImage.setImageBitmap(bitmap);
                uploadProfileImageFirebase(selectedImage);
                saveImageSharedPreferences(selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_personal_zone, menu);
        Log.i("onCreateOptionsMenu", "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent userPersonalIntent;
        Log.i("onOptionsItemSelected", item.getTitle().toString());
        if (item.getTitle().toString().equals("Home")) {
            userPersonalIntent = new Intent(PersonalUserActivity.this,
                    userState.equals("Trainee") ? TraineeActivity.class : TrainerActivity.class);//Trainee or Trainer
        } else {
            UpdateStatisticsDB();
            userPersonalIntent = new Intent(PersonalUserActivity.this, LoginActivity.class);
            userPersonalIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            toast.toastMessage("Hope to see you again, Going logout..");
        }
        this.startActivity(userPersonalIntent);
        this.finish();
        return false;
    }

    private void uploadProfileImageFirebase(Uri filePath) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        StorageReference ref = storageReference.child("users").child(ConfigureUID.getUID()).child("profile");
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        toast.toastMessage("Uploaded");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        toast.toastMessage("Uploading Failed please verify network connection");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    }
                });
    }

    private void saveImageSharedPreferences(Uri selectedImage) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ConfigureUID.getUID() + "profileImage", selectedImage.toString());
        editor.apply();
    }

    private void showDialogScreen(String item, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Details");
        final View customLayout = getLayoutInflater().inflate(R.layout.credential_dialog, null);
        EditText inputHint = customLayout.findViewById(R.id.input_line);
        inputHint.setHint(item);
        builder.setView(customLayout);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText inputLine = customLayout.findViewById(R.id.input_line);
                if (!inputLine.getText().toString().equals("")) {
                    String inputLineValue = item.substring(0, item.indexOf(":") + 2) + inputLine.getText().toString();
                    if (position == 3) {
                        if (inputLine.getText().toString().matches("[0-9]+") && inputLine.getText().toString().length() >= 2
                                && inputLine.getText().toString().length() < 3) {
                            ageBeforeUpdate = Integer.parseInt(list.get(position).substring(list.get(position).indexOf(":") + 1).trim());
                            list.set(position, inputLineValue);
                        } else {
                            dialog.dismiss();
                        }
                    }
                    if (position == 1 || position == 2) {
                        if (inputLine.getText().toString().matches("^[a-zA-Z]+$")) {
                            list.set(position, inputLineValue);
                        } else {
                            dialog.dismiss();
                        }
                    }
                    if (position == 6) {
                        if (inputLine.getText().toString().matches("[0-9]+") && inputLine.getText().toString().length() == 10) {
                            phoneNumberValidation(inputLineValue,position,dialog);
                        } else {
                            dialog.dismiss();
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
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

    public void userRestPassword(View view) {
        mAuth.sendPasswordResetEmail(auth.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    toast.toastMessage("An email with instruction has been send");
                } else {
                    toast.toastMessage("Unexpected error occurs\nContact US");
                }
            }
        });
    }

    public void saveDataInFireBaseAndShared(View view) {
        Intent userPersonalIntent;
        Map<String, Object> changedData = new HashMap<>();
        changedData.put("First", list.get(1).substring(list.get(1).indexOf(":") + 1).trim());
        changedData.put("Last", list.get(2).substring(list.get(2).indexOf(":") + 1).trim());
        changedData.put("Age", list.get(3).substring(list.get(3).indexOf(":") + 1).trim());
        changedData.put("Email", list.get(0).substring(list.get(0).indexOf(":") + 1).trim());
        changedData.put("Phone", list.get(6).substring(list.get(6).indexOf(":") + 1).trim());
        Log.i("printlist", list.toString());
        db.collection("users").document(ConfigureUID.getUID()).update("First", changedData.get("First"),
                "Last", changedData.get("Last"),
                "Age", changedData.get("Age"), "Email", changedData.get("Email"), "Phone", changedData.get("Phone"));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        LinkedHashSet<String> setStringPref = new LinkedHashSet<String>();
        setStringPref.add("emailAddress:" + list.get(0).substring(list.get(0).indexOf(":") + 1).trim());
        setStringPref.add("firstName:" + list.get(1).substring(list.get(1).indexOf(":") + 1).trim());
        setStringPref.add("lastName:" + list.get(2).substring(list.get(2).indexOf(":") + 1).trim());
        setStringPref.add("userAge:" + list.get(3).substring(list.get(3).indexOf(":") + 1).trim());
        setStringPref.add("userGender:" + list.get(4).substring(list.get(4).indexOf(":") + 1).trim());
        setStringPref.add("userName:" + list.get(5).substring(list.get(5).indexOf(":") + 1).trim());
        setStringPref.add("userPhone:" + list.get(6).substring(list.get(6).indexOf(":") + 1).trim());
        setStringPref.add("userState:" + list.get(7).substring(list.get(7).indexOf(":") + 1).trim());
        if (userState.equals("Trainer")) {
            setStringPref.add("expert:" + sharedPreferences.getString(ConfigureUID.getUID() + "expert", ""));
            editor.putString(ConfigureUID.getUID() + "expert", sharedPreferences.getString(ConfigureUID.getUID() + "expert", ""));
        }
        editor.putStringSet("USER_CONTACT", setStringPref);
        editor.putString(ConfigureUID.getUID() + "firstName", list.get(1).substring(list.get(1).indexOf(":") + 1).trim());
        editor.putString(ConfigureUID.getUID() + "lastName", list.get(2).substring(list.get(1).indexOf(":") + 1).trim());
        editor.apply();
        editor.commit();
        Log.i("agedesntChanfed",(list.get(3).substring(list.get(3).indexOf(":") + 1).trim()));
        if(ageBeforeUpdate!=0){updateAgeStatistics(Integer.parseInt(list.get(3).substring(list.get(3).indexOf(":") + 1).trim()), ageBeforeUpdate); }
        updatePhoneNumberRealTimeTable(list.get(6).substring(list.get(6).indexOf(":") + 1));
        userPersonalIntent = new Intent(PersonalUserActivity.this,
                userState.equals("Trainee") ? TraineeActivity.class : TrainerActivity.class);
        PersonalUserActivity.this.startActivity(userPersonalIntent);
        PersonalUserActivity.this.finish();
    }


    public void removeUserFireBaseAccount(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Account");
        final View customLayout = getLayoutInflater().inflate(R.layout.auth_dialog, null);
        builder.setView(customLayout);
        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent sendOTPRemoveUser;
                sendOTPRemoveUser = new Intent(PersonalUserActivity.this, SendOTPActivity.class);
                sendOTPRemoveUser.putExtra("userState",userState);
                sendOTPRemoveUser.putExtra("activityState","RemoveAccount");
                PersonalUserActivity.this.startActivity(sendOTPRemoveUser);
                PersonalUserActivity.this.finish();
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

    public void changerUserVerificationAddress(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rest Email Address");
        final View customLayout = getLayoutInflater().inflate(R.layout.credential_dialog, null);
        EditText inputline = customLayout.findViewById(R.id.input_line);
        inputline.setHint("New Email Address");
        builder.setView(customLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newEmailAddress = inputline.getText().toString();
                if (!newEmailAddress.equals("") && newEmailAddress.contains("@")) {
                    auth.updateEmail(newEmailAddress).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("auth.updateEmail","auth.updateEmail addOnSuccessListener");
                            auth.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i("auth.sendEmailVerification","auth.sendEmailVerification addOnSuccessListener");
                                    db.collection("users").document(ConfigureUID.getUID()).update("Email",newEmailAddress);
                                    list.set(0, newEmailAddress);
                                    toast.toastMessage("Email changed successfully!");
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("auth.updateEmail","auth.updateEmail onFailure");
                            toast.toastMessage("Please renter an Email or try another one");
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

    private String convertSharedPreferencesKeyToString(String key) {
        if (key.contains("emailAddress")) {
            return "Email Address:";
        }
        if (key.contains("expert")) {
            return "Expertise:";
        }
        if (key.contains("firstName")) {
            return "First Name:";
        }
        if (key.contains("userName")) {
            return "Username:";
        }
        if (key.contains("lastName")) {
            return "Last Name:";
        }
        if (key.contains("userState")) {
            return "Who Am I:";
        }
        if (key.contains("userAge")) {
            return "Age:";
        }
        if (key.contains("userPhone")) {
            return "Phone:";
        }
        if (key.contains("userGender")) {
            return "Gender:";
        }
        return null;
    }

    private void phoneNumberValidation(String number, int pos, DialogInterface dialog) {
        FOUND_PHONE = false;
        realtimeRef.child("PhoneUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(number.substring(number.indexOf(":") + 2).equals(ds.getKey()))
                    {
                        FOUND_PHONE = true;
                        toast.toastMessage("Phone Number already exists");
                        dialog.dismiss();
                        break;
                    }
                }
                if(FOUND_PHONE != true){
                    list.set(pos,number);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }
    private void updatePhoneNumberRealTimeTable(String phoneNumber) {
        Map<String, Object> updatedPhoneNumber = new HashMap<>();
        updatedPhoneNumber.put(phoneNumber.substring(phoneNumber.indexOf(":")+ 2) , ConfigureUID.getUID());
        realtimeRef.child("PhoneUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    if(ds.getValue().equals(ConfigureUID.getUID()))
                    {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("DataPhoneNumber","DataPhoneNumber Deleted");
                                realtimeRef.child("PhoneUsers").updateChildren(updatedPhoneNumber);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updateAgeStatistics(int newAgeUpdate,int oldAge) {
            realtimeRef.child("Statistics").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        int numStart, numEnd;
                        String var = ds.getKey().replaceAll("[^0-9]+", "");
                        if (var.length() != 0) {
                            numStart = Integer.parseInt(var.substring(0, 2).trim());
                            numEnd = Integer.parseInt(var.substring(2, 4).trim());
                            if(newAgeUpdate >= numStart && newAgeUpdate <= numEnd && oldAge >= numStart && oldAge <= numEnd){break;}
                            if (oldAge >= numStart && oldAge <= numEnd) {
                                ds.getRef().setValue(Integer.parseInt(ds.getValue().toString()) - 1);
                            }
                            if (newAgeUpdate >= numStart && newAgeUpdate <= numEnd) {
                                ds.getRef().setValue(Integer.parseInt(ds.getValue().toString()) + 1);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }
    private void UpdateStatisticsDB() {
        /*Log.d(TAG, "UpdateStatisticsDB: " + StatisticsHashMap);*/
        int SignOUTCntr;
        SignOUTCntr = Integer.valueOf(StatisticsHashMap.get("SignOUT"));
        SignOUTCntr++;
        Statistics.child("SignOUT").setValue(SignOUTCntr);
    }
    private void loadProfileImageSharedPref() {
        profile = findViewById(R.id.upload_profile_trainee);
        if (!(sharedPreferences.getString(ConfigureUID.getUID() + "profileImage", "").isEmpty())){
            String bitEncodedString = sharedPreferences.getString(ConfigureUID.getUID() + "profileImage", "");
            Log.i("ImagefromSh","ImagefromSh");
            Uri imageProfile = Uri.parse(bitEncodedString);
            Picasso.get().load(imageProfile)
                    .into(profile, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            sharedPreferences.edit().remove(ConfigureUID.getUID() + "profileImage").apply();
                            profile.setImageResource(R.mipmap.avatar);
                        }
                    });
        }
    }

}