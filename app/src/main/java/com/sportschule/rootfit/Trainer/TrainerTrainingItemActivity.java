package com.sportschule.rootfit.Trainer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sportschule.rootfit.ConfigureUID;
import com.sportschule.rootfit.RecyclerAdapterTraineeCard;
import com.sportschule.rootfit.R;
import com.squareup.picasso.Picasso;

public class TrainerTrainingItemActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference realDataRef = database.getReference("");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    RecyclerView recyclerView;
    TextView SCHEDULE_DATE,SCHEDULE_TIME,SCHEDULE_JOIN_TRAINEE,TRAINER_NAME,MAX_PARTICIPANT;
    Bundle bundle;
    RatingBar ratingBar;
    Intent BACK_TRAINER_ACTIVITY;
    FirestoreRecyclerAdapter adapter;
    Button buttonCancel,buttonCheckout;
    private View parentLayout;
    private String GET_TIME,GET_DATE,GET_EXPERT,GET_RATE,GET_TRAINER_NAME;
    private int GET_MAX;
    private Boolean GET_PAST_STATE;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.startActivity(BACK_TRAINER_ACTIVITY);
        this.finish();
    }
    private void returnActivity()
    {
        this.startActivity(BACK_TRAINER_ACTIVITY);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getIntent().getExtras();
        setContentView(R.layout.activity_trainer_training_item);
        parentLayout = findViewById(android.R.id.content);
        BACK_TRAINER_ACTIVITY = new Intent(this, TrainerActivity.class);
        GET_DATE = bundle.getBundle("itemActivity").getString("getDate").replace(" ", "_");
        GET_EXPERT = bundle.getBundle("itemActivity").getString("getTrainerExpertise");
        GET_MAX = bundle.getBundle("itemActivity").getInt("getMaxParticipant");
        GET_RATE = bundle.getBundle("itemActivity").getString("getRating");
        GET_TIME = bundle.getBundle("itemActivity").getString("getTime");
        GET_TRAINER_NAME = bundle.getBundle("itemActivity").getString("getTrainerName");
        GET_PAST_STATE = bundle.getBundle("itemActivity").getBoolean("getState");
        buttonCancel = findViewById(R.id.cancel_training_button_item);
        buttonCheckout = findViewById(R.id.checkout_training_button_item);
        ratingBar = findViewById(R.id.rating_trainer_training_item);
        SCHEDULE_DATE = findViewById(R.id.training_schedule_date_edit);
        SCHEDULE_TIME = findViewById(R.id.training_schedule_time_edit);
        TRAINER_NAME = findViewById(R.id.training_schedule_trainer_name_edit);
        SCHEDULE_JOIN_TRAINEE = findViewById(R.id.training_schedule_participate_edit);
        MAX_PARTICIPANT = findViewById(R.id.training_schedule_max_participate_edit);
        recyclerView = findViewById(R.id.recycler_trainee_detail_item);
        SCHEDULE_DATE.setText(GET_DATE.replace("_"," "));
        SCHEDULE_TIME.setText(GET_TIME);
        TRAINER_NAME.setText(GET_TRAINER_NAME);
        MAX_PARTICIPANT.setText(String.valueOf(GET_MAX));
        GET_TIME = GET_TIME.substring(0,2)+"_"+GET_TIME.substring(6,8);
        if(GET_PAST_STATE){
            buttonCancel.setVisibility(View.INVISIBLE);
            buttonCheckout.setVisibility(View.INVISIBLE);
            ratingBar.setRating(Float.parseFloat(GET_RATE));
            listenerForTrainerRate();
        }
        else {
            checkStateCheckout();
            ratingBar.setVisibility(View.INVISIBLE);
        }
        listenerForCurrentTrainee();
        initTrainingQueryFireBase();
    }
    private void initTrainingQueryFireBase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        //Query
        Query query = firebaseFirestore.collection(GET_EXPERT).document(GET_DATE+GET_TIME).collection("TraineeIdentifier");
        FirestoreRecyclerOptions<RecyclerAdapterTraineeCard> response = new FirestoreRecyclerOptions.Builder<RecyclerAdapterTraineeCard>()
                .setQuery(query, RecyclerAdapterTraineeCard.class)
                .build();
        //Adapter
        adapter = new FirestoreRecyclerAdapter<RecyclerAdapterTraineeCard, TraineeViewHolder>(response) {
            @NonNull
            @Override
            public TraineeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainee_details_recyclear_trainer_view,parent,false);
                return new TraineeViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull TraineeViewHolder holder, int position, @NonNull RecyclerAdapterTraineeCard model) {
                holder.name.setText(model.getName());
                holder.last.setText(model.getLast());
                holder.age.setText(model.getAge());
                storageReference.child("users").child(model.getUid()).child("profile").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("profileImage", "profile image loaded");
                        if (uri!=null) {
                            Picasso.get().load(uri).into(holder.traineeDetailImageProfile);
                        }
                    }
                });
            }
        };
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }


    private class TraineeViewHolder extends RecyclerView.ViewHolder {
        private final TextView age,name,last;
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
    public void cancelTrainingTraining(View view)
    {
        new CancelTrainerTraining(GET_EXPERT,GET_DATE,GET_TIME,ConfigureUID.getUID(),this);
    }
    private void listenerForCurrentTrainee() {
        realDataRef.child(GET_EXPERT).child("PersonalPlanTraining").child(ConfigureUID.getUID()).child(GET_DATE+GET_TIME).child("Current_Participant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            SCHEDULE_JOIN_TRAINEE.setText(snapshot.getValue(String.class));
                            Log.i("snapshot.getValue(String.class)", snapshot.getValue(String.class));
                        }
                        else
                        {
                            if(GET_PAST_STATE!=true) { returnActivity();}
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void TrainerTrainingCheckOut(View view)
    {
        ManageForPastPlanTraining manageForPastPlanTraining = new ManageForPastPlanTraining();
        int checkout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkout=manageForPastPlanTraining.trainerCheckOutTraining(GET_EXPERT,GET_DATE,GET_TIME);
            switch (checkout)
            {
                case 1:
                    buttonCancel.setEnabled(false);
                    buttonCheckout.setEnabled(false);
                    realDataRef.child("AdminDashboard").child(GET_EXPERT+"_"+GET_DATE+GET_TIME+ConfigureUID.getUID()).child("isCheckout")
                            .setValue("1");
                    Snackbar.make(parentLayout, "Training is checked out\nThanks you", Snackbar.LENGTH_LONG).show();
                    break;

                default:
                    Snackbar.make(parentLayout, "The training has not finished yet", Snackbar.LENGTH_LONG).show();
            }
        }
    }
    private void checkStateCheckout()
    {
        realDataRef.child("AdminDashboard").child(GET_EXPERT+"_"+GET_DATE+GET_TIME+ConfigureUID.getUID()).child("isCheckout")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue().equals("1"))
                        {
                            buttonCancel.setEnabled(false);
                            buttonCheckout.setEnabled(false);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void listenerForTrainerRate()
    {
        realDataRef.child(GET_EXPERT).child("PastTraining").child(ConfigureUID.getUID()).child(GET_DATE+GET_TIME)
                .child("Rating").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingBar.setRating(Float.parseFloat(snapshot.getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}