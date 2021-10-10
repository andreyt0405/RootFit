package com.sportschule.rootfit.Trainee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sportschule.rootfit.ConfigureUID;
import com.sportschule.rootfit.RecyclerAdapterTraineeCard;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.ToastCustomMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TraineeTrainingItemActivity extends AppCompatActivity {
    static boolean active = false;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference realDataRef = database.getReference("");

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    private final FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    private final DatabaseReference Statistics = dbPhone.getReference().child("Statistics");
    HashMap <String,String> StatisticsHashMap = new HashMap<>();
    RecyclerView recyclerView;
    TextView SCHEDULE_DATE, SCHEDULE_TIME, SCHEDULE_JOIN_TRAINEE, TRAINER_NAME, TRAINER_EXP,
    MAX_PARTICIPANT;
    ManageParticipantTraining manageParticipantTrainingCount;
    Button joinTraineeButton,unJoinTraineeButton;
    Bundle bundle;
    Intent BACK_TRAINEE_ACTIVITY;
    FirestoreRecyclerAdapter adapter;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    private View parentLayout;
    private static final String SHARED_PREFS = "sharedPrefs";
    private SharedPreferences sharedPreferences;
    private String GET_TIME, GET_DATE, GET_RATE, GET_TRAINER_NAME, GET_TRAINER_UID, GET_EXPERTISE;
    private int GET_MAX;
    private String firstName,lastName,phoneNum,Gender;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BACK_TRAINEE_ACTIVITY = new Intent(this, TraineeActivity.class);
        this.startActivity(BACK_TRAINEE_ACTIVITY);
        this.finish();
    }

    private void returnActivity() {
        BACK_TRAINEE_ACTIVITY = new Intent(this, TraineeActivity.class);
        BACK_TRAINEE_ACTIVITY.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(BACK_TRAINEE_ACTIVITY);
        this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        parentLayout = findViewById(android.R.id.content);
        bundle = getIntent().getExtras();
        setContentView(R.layout.activity_trainee_training_item);
        GET_DATE = bundle.getBundle("itemActivity").getString("getDate").replace(" ", "_");
        GET_EXPERTISE = bundle.getBundle("itemActivity").getString("getTrainerExpertise");
        GET_MAX = bundle.getBundle("itemActivity").getInt("getMaxParticipant");
        GET_RATE = bundle.getBundle("itemActivity").getString("getRating");
        GET_TIME = bundle.getBundle("itemActivity").getString("getTime");
        GET_TRAINER_NAME = bundle.getBundle("itemActivity").getString("getTrainerName");
        GET_TRAINER_UID = bundle.getBundle("itemActivity").getString("getTrainerUid");

        SCHEDULE_DATE = findViewById(R.id.training_schedule_date_edit);
        SCHEDULE_TIME = findViewById(R.id.training_schedule_time_edit);
        TRAINER_NAME = findViewById(R.id.training_schedule_trainer_name_edit);
        SCHEDULE_JOIN_TRAINEE = findViewById(R.id.training_schedule_participate_edit);
        TRAINER_EXP = findViewById(R.id.training_schedule_expertise_edit);
        MAX_PARTICIPANT = findViewById(R.id.training_schedule_max_participate_edit);
        recyclerView = findViewById(R.id.recycler_trainee_detail_item);
        SCHEDULE_DATE.setText(GET_DATE.replace("_", " "));
        SCHEDULE_TIME.setText(GET_TIME);
        TRAINER_EXP.setText(GET_EXPERTISE);
        TRAINER_NAME.setText(GET_TRAINER_NAME);
        MAX_PARTICIPANT.setText(String.valueOf(GET_MAX));
        GET_TIME = GET_TIME.substring(0, 2) + "_" + GET_TIME.substring(6, 8);
        joinTraineeButton = findViewById(R.id.join_training_button_item);
        unJoinTraineeButton = findViewById(R.id.unjoin_training_button_item);
        manageParticipantTrainingCount = new ManageParticipantTraining(GET_TRAINER_UID,GET_DATE,GET_TIME,GET_EXPERTISE,GET_TRAINER_NAME,GET_RATE,String.valueOf(GET_MAX));
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        listerForValueRemoved();
        listenerForCurrentTrainee();
        initTrainingQueryFireBase();
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
    }

    private void initTrainingQueryFireBase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        //Query
        Query query = firebaseFirestore.collection(GET_EXPERTISE).document(GET_DATE + GET_TIME).collection("TraineeIdentifier");
        FirestoreRecyclerOptions<RecyclerAdapterTraineeCard> response = new FirestoreRecyclerOptions.Builder<RecyclerAdapterTraineeCard>()
                .setQuery(query, RecyclerAdapterTraineeCard.class)
                .build();
        //Adapter
        adapter = new FirestoreRecyclerAdapter<RecyclerAdapterTraineeCard, TraineeViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull TraineeViewHolder holder, int position, @NonNull RecyclerAdapterTraineeCard model) {
                holder.name.setText(model.getName());
                holder.last.setText(model.getLast());
                holder.age.setText(model.getAge());
                storageReference.child("users").child(model.getUid()).child("profile").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("profileImage", "profile image loaded");
                        if (uri != null) {
                            Picasso.get().load(uri).into(holder.traineeDetailImageProfile);
                        }
                    }
                });
            }
            @NonNull
            @Override
            public TraineeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainee_details_recyclear_trainer_view, parent, false);
                return new TraineeViewHolder(view);
            }
        };
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private class TraineeViewHolder extends RecyclerView.ViewHolder {
        private final TextView age, name, last;
        private final ImageView traineeDetailImageProfile;

        public TraineeViewHolder(@NonNull View itemView) {
            super(itemView);
            age = itemView.findViewById(R.id.trainee_detail_age);
            name = itemView.findViewById(R.id.trainee_detail_name);
            last = itemView.findViewById(R.id.trainee_detail_last);
            traineeDetailImageProfile = itemView.findViewById(R.id.details_traine_image_profile);
        }
    }
    private void listenerForCurrentTrainee() {
        realDataRef.child(GET_EXPERTISE).child("PersonalPlanTraining")
                .child(GET_TRAINER_UID).child(GET_DATE + GET_TIME)
                .child("Current_Participant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            SCHEDULE_JOIN_TRAINEE.setText(snapshot.getValue(String.class));
                            Log.i("snapshot.getValue(String.class)", snapshot.getValue(String.class));
                            listenerForTrainingCheckOut();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void initJoinStatusButton(int current) {
            Log.i("GET_DATE + GET_TIME",GET_DATE + GET_TIME);
            DocumentReference docIdRef = firebaseFirestore.collection(GET_EXPERTISE).document(GET_DATE + GET_TIME)
                    .collection("TraineeIdentifier").document(ConfigureUID.getUID());
            docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            joinTraineeButton.setVisibility(View.INVISIBLE);
                            unJoinTraineeButton.setVisibility(View.VISIBLE);
                            unJoinTraineeButton.setEnabled(true);
                            Log.i("document.exists()","document.exists()");
                        } else {
                            if(current >= GET_MAX)
                            {
                                joinTraineeButton.setVisibility(View.VISIBLE);
                                joinTraineeButton.setEnabled(false);
                                unJoinTraineeButton.setVisibility(View.INVISIBLE);
                            }
                            else {
                                joinTraineeButton.setVisibility(View.VISIBLE);
                                unJoinTraineeButton.setVisibility(View.INVISIBLE);
                                joinTraineeButton.setEnabled(true);
                            }
                        }
                    }

                }
            });
    }

    private void sendSMSmsg(String phoneNo, String msgTXT) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, msgTXT, null, null);
    }

    public void userSubscribeTraining(View view) {
        joinTraineeButton.setVisibility(View.GONE);
        unJoinTraineeButton.setVisibility(View.VISIBLE);
        Map<String, String> db_account = new HashMap<>();
        Set<String> keys = sharedPreferences.getStringSet("USER_CONTACT", new LinkedHashSet<>());
        String[] user_contact = keys.toArray(new String[keys.size()]);
        List<String> stringList = new ArrayList<String>(Arrays.asList(user_contact));
        Collections.sort(stringList);
        for (String key : stringList) {
            if (key.contains("firstName") || key.contains("lastName") || key.contains("userAge")) {
                Log.i("keys", key);
                db_account.put(convertSharedPreferencesKeyToString(key),
                        key.substring(key.indexOf(":") + 1));
            }
            db_account.put("Uid",ConfigureUID.getUID());
        }
        DocumentReference docIdRef = firebaseFirestore.collection(ConfigureUID.getUID()).document(GET_DATE + GET_TIME);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!(document.exists())) {
                        firebaseFirestore.collection(GET_EXPERTISE).document(GET_DATE + GET_TIME).collection("TraineeIdentifier")
                                .document(ConfigureUID.getUID()).set(db_account)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        DocumentReference docRef = firebaseFirestore.collection("users").document(ConfigureUID.getUID());
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();
                                                if(document.exists()){
                                                    userTheResultAndSendSMS(document.get("First").toString(), document.get("Last").toString(),
                                                            document.get("Phone").toString(),document.get("Gender").toString());
                                                }
                                                else{
                                                    Log.d("firebase user ID not found!", "onComplete: ");
                                                }
                                            }
                                        });
                                        UpdateStatisticsDB();
                                        Log.i("USER_DOCUMENT", "USER_DOCUMENT_ADDED");
                                        manageParticipantTrainingCount.increaseTrainingCount();
                                        manageParticipantTrainingCount.participantAddTrainingFireStore();
                                    }
                                });
                        Log.d("Document_not_exists!", "trainee can join to new training");
                        Snackbar.make(parentLayout, "Successfully subscribed to Training", Snackbar.LENGTH_LONG).show();
                    } else {
                        unJoinTraineeButton.setVisibility(View.INVISIBLE);
                        toast.toastMessage(R.string.prompt_training_planed, Gravity.BOTTOM);
                        Log.d("Document_exist", "trainee has already have training in this time");
                    }
                }
            }
        });
    }

    private void userTheResultAndSendSMS(String first, String last, String phone, String gender) {
        firstName = first;
        lastName = last;
        phoneNum = phone.substring(1).trim();
        Gender = gender;
        sendSMSmsg("+972" + phoneNum,"Reminder! a lesson " + GET_EXPERTISE + "\n\rset to: " + GET_DATE + GET_TIME + ".\n\rdo not be late");
    }

    public void userUnsubscribeTraining(View view) {
        unJoinTraineeButton.setVisibility(View.GONE);
        joinTraineeButton.setVisibility(View.VISIBLE);
        manageParticipantTrainingCount.participantDelTrainingFireStore();
        Snackbar.make(parentLayout, "Training Unsubscribed Successfully",
                Snackbar.LENGTH_LONG).show();
    }

    private String convertSharedPreferencesKeyToString(String key) {
        if(key.contains("firstName")){return "Name";}
        else if(key.contains("lastName")){return "Last";}
        else if(key.contains("userAge")){return "Age";}
        return null;
    }
    private void listerForValueRemoved() {
        realDataRef.child("AdminDashboard").child(GET_EXPERTISE+"_"+GET_DATE+GET_TIME+GET_TRAINER_UID).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    sendSMSmsg("+972" + phoneNum,GET_EXPERTISE+" training which is were planned to be at "+GET_DATE +" "+GET_TIME+"\r\n is canceled.\n" +
                            "Reschedule your training and keep ACTIVE!");
                    Snackbar.make(parentLayout, "Training has cancelled by Trainer/Admin\nFor" +
                            "more information please contact us", Snackbar.LENGTH_LONG).show();
                    if(active==true){
                        returnActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void listenerForTrainingCheckOut() {
        realDataRef.child("AdminDashboard").child(GET_EXPERTISE+"_"+GET_DATE+GET_TIME+GET_TRAINER_UID).child("isCheckout")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.getValue().equals("1")) {
                                Log.i("getValue", "getValue1");
                                unJoinTraineeButton.setVisibility(View.INVISIBLE);
                                Snackbar.make(parentLayout, "Training has checkout\nThank you for your participation ", Snackbar.LENGTH_LONG).show();
                            } else {
                                Log.i("getValue", "getValue0");
                                initJoinStatusButton(Integer.parseInt(SCHEDULE_JOIN_TRAINEE.getText().toString()));
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        }
    private void UpdateStatisticsDB() {
        int Training_AerobicsCntr,Training_CrossfitCntr,Training_FitnessCntr,Training_YogaCntr;

        Training_AerobicsCntr = Integer.valueOf(StatisticsHashMap.get("Training_Aerobics"));
        Training_CrossfitCntr = Integer.valueOf(StatisticsHashMap.get("Training_Crossfit"));
        Training_FitnessCntr = Integer.valueOf(StatisticsHashMap.get("Training_Fitness"));
        Training_YogaCntr = Integer.valueOf(StatisticsHashMap.get("Training_Yoga"));

        if(GET_EXPERTISE.equalsIgnoreCase("Aerobics")){Training_AerobicsCntr++;}
        if(GET_EXPERTISE.equalsIgnoreCase("Crossfit")){Training_CrossfitCntr++;}
        if(GET_EXPERTISE.equalsIgnoreCase("Fitness")){Training_FitnessCntr++;}
        if(GET_EXPERTISE.equalsIgnoreCase("Yoga")){Training_YogaCntr++;}

        if(GET_EXPERTISE.equalsIgnoreCase("Aerobics")){Statistics.child("Training_Aerobics").setValue(Training_AerobicsCntr);}
        if(GET_EXPERTISE.equalsIgnoreCase("Crossfit")){Statistics.child("Training_Crossfit").setValue(Training_CrossfitCntr);}
        if(GET_EXPERTISE.equalsIgnoreCase("Fitness")){Statistics.child("Training_Fitness").setValue(Training_FitnessCntr);}
        if(GET_EXPERTISE.equalsIgnoreCase("Yoga")){Statistics.child("Training_Yoga").setValue(Training_YogaCntr);}

    }
    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        adapter.startListening();
    }
}