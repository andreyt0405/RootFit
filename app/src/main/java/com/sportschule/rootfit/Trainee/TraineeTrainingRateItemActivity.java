package com.sportschule.rootfit.Trainee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sportschule.rootfit.ConfigureUID;
import com.sportschule.rootfit.RecyclerAdapterTraineeCard;
import com.sportschule.rootfit.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Map;

public class TraineeTrainingRateItemActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference realDataRef = database.getReference("");
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    FirestoreRecyclerAdapter adapter;
    Intent BACK_TRAINEE_ACTIVITY;
    Bundle bundle;
    RecyclerView recyclerView;

    private boolean isIndicatorRate=false;
    private static final String SHARED_PREFS = "sharedPrefs";
    private SharedPreferences sharedPreferences;
    private String GET_TIME_PAST, GET_DATE_PAST, SET_RATE, GET_TRAINER_NAME, GET_TRAINER_UID, GET_EXPERTISE;
    private int participantNumberRate = 0;
    private float currentRate;
    RatingBar ratingBar;
    TextView SCHEDULE_DATE, SCHEDULE_TIME, TRAINER_NAME, TRAINER_EXP;

    public void onBackPressed() {
        super.onBackPressed();
        super.onBackPressed();
        BACK_TRAINEE_ACTIVITY = new Intent(this, TraineeActivity.class);
        this.startActivity(BACK_TRAINEE_ACTIVITY);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_training_past_item);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        bundle = getIntent().getExtras();
        ratingBar = findViewById(R.id.rating_past_activity);
        GET_DATE_PAST = bundle.getBundle("itemActivity").getString("getDate");
        GET_EXPERTISE = bundle.getBundle("itemActivity").getString("getTrainerExpertise");
        SET_RATE = bundle.getBundle("itemActivity").getString("getRating");
        GET_TIME_PAST = bundle.getBundle("itemActivity").getString("getTime");
        GET_TRAINER_NAME = bundle.getBundle("itemActivity").getString("getTrainerName");
        GET_TRAINER_UID = bundle.getBundle("itemActivity").getString("getTrainerUid");

        SCHEDULE_DATE = findViewById(R.id.training_schedule_date_edit);
        SCHEDULE_TIME = findViewById(R.id.training_schedule_time_edit);
        TRAINER_EXP = findViewById(R.id.training_schedule_expertise_edit);
        TRAINER_NAME = findViewById(R.id.training_schedule_trainer_name_edit);
        recyclerView = findViewById(R.id.recycler_trainee_detail_past_item);
        SCHEDULE_DATE.setText(GET_DATE_PAST.replace("_", " "));
        SCHEDULE_TIME.setText(GET_TIME_PAST);
        TRAINER_EXP.setText(GET_EXPERTISE);
        TRAINER_NAME.setText(GET_TRAINER_NAME);
        GET_TIME_PAST = GET_TIME_PAST.substring(0, 2) + "_" + GET_TIME_PAST.substring(6, 8);
        GET_DATE_PAST = GET_DATE_PAST.replace(" ","_");
        listenerCurrentRateTrainee();
        getRateIndicator();
        initTrainingQueryFireBase();
    }

    private void listenerCurrentRateTrainee() {
        realDataRef.child(GET_EXPERTISE).child("PastTraining")
                .child(GET_TRAINER_UID).child(GET_DATE_PAST + GET_TIME_PAST)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.getKey().equals("NumberTraineeRate")) {
                                    participantNumberRate = Integer.parseInt(dataSnapshot.getValue().toString());
                                }
                                if(dataSnapshot.getKey().equals("Rating"))
                                {
                                    currentRate = Float.parseFloat(dataSnapshot.getValue().toString());
                                }
                            }
                            if(participantNumberRate==0){participantNumberRate=1;}
                            else
                            {
                                participantNumberRate+=1;
                            }
                            Log.i("participantNumberRate",String.valueOf(participantNumberRate));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void setTrainingRating() {
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        Float v = (rating + currentRate) / participantNumberRate;
                        ratingBar.setRating(rating);
                        ratingBar.setIsIndicator(true);
                        Log.i("ratingBar", String.valueOf(rating));
                        setRatingInPastTraining(true,v,rating);
                }
            });
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void setRatingInPastTraining(boolean isIndicator, Float trainerRate,float traineeRate)
    {
        Log.i("participantNumberRatesetRai",String.valueOf(participantNumberRate));
        realDataRef.child(GET_EXPERTISE).child("PastTraining")
                .child(GET_TRAINER_UID).child(GET_DATE_PAST + GET_TIME_PAST)
                .updateChildren(Map.of("Rating",(new DecimalFormat("##.##").format(trainerRate)),"NumberTraineeRate",participantNumberRate))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                realDataRef.child(GET_EXPERTISE)
                        .child(GET_DATE_PAST).child(GET_TIME_PAST)
                        .updateChildren(Map.of("Rating",(new DecimalFormat("##.##").format(trainerRate)),"NumberTraineeRate",participantNumberRate))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("GET_DATE_PAST+GET_TIME_PAST",GET_DATE_PAST+GET_TIME_PAST);
                                firebaseFirestore.collection(ConfigureUID.getUID()).document(GET_EXPERTISE)
                                        .collection(GET_DATE_PAST).document(GET_DATE_PAST+GET_TIME_PAST)
                                        .update("Rating",String.valueOf(traineeRate)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection(ConfigureUID.getUID()).document("PastAllReservedTraining")
                                                .collection("PastAllReservedTraining")
                                                .document(GET_EXPERTISE+"_"+GET_DATE_PAST+GET_TIME_PAST)
                                                .update("Rating",String.valueOf(traineeRate),"IsIndicator",isIndicator).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i("RatingChanged","Rating modified for PastTraining");
                                            }
                                        });
                                    }
                                });
                            }
                        });
            }
        });
    }
    private void initTrainingQueryFireBase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        //Query
        Query query = firebaseFirestore.collection(GET_EXPERTISE).document(GET_DATE_PAST + GET_TIME_PAST)
                .collection("TraineeIdentifier");
        FirestoreRecyclerOptions<RecyclerAdapterTraineeCard> response = new FirestoreRecyclerOptions.Builder<RecyclerAdapterTraineeCard>()
                .setQuery(query, RecyclerAdapterTraineeCard.class)
                .build();
        //Adapter
        adapter = new FirestoreRecyclerAdapter<RecyclerAdapterTraineeCard, TraineeTrainingRateItemActivity.TraineeViewHolder>(response) {
            @Override
            protected void onBindViewHolder(@NonNull TraineeTrainingRateItemActivity.TraineeViewHolder holder, int position, @NonNull RecyclerAdapterTraineeCard model) {
                holder.name.setText(model.getName());
                holder.last.setText(model.getLast());
                holder.age.setText(model.getAge());
                storageReference.child("users/" + model.getUid() + "/profile").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
            public TraineeTrainingRateItemActivity.TraineeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainee_details_recyclear_trainer_view, parent, false);
                return new TraineeTrainingRateItemActivity.TraineeViewHolder(view);
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
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    public void getRateIndicator()
    {
        firebaseFirestore.collection(ConfigureUID.getUID()).document("PastAllReservedTraining")
                .collection("PastAllReservedTraining")
                .document(GET_EXPERTISE+"_"+GET_DATE_PAST+GET_TIME_PAST).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.contains("IsIndicator")) {
                    isIndicatorRate = (boolean) documentSnapshot.get("IsIndicator");
                }
                Log.i("IsIndicator",String.valueOf(isIndicatorRate));
                if(isIndicatorRate) {
                    ratingBar.setRating(currentRate);
                    ratingBar.setIsIndicator(true);
                }
                else {
                    setTrainingRating();
                }
            }
        });
    }
}
