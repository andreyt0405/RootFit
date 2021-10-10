package com.sportschule.rootfit.Trainee;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sportschule.rootfit.ConfigureUID;

import java.util.HashMap;
import java.util.Map;

public class ManageParticipantTraining {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DatabaseReference realDataRef = database.getReference("");

    private final String trainerUid;
    private final String date;
    private final String time;
    private final String expert;
    private final String trainerName;
    private final String rating;
    private final String max;
    private int totalCount;

    ManageParticipantTraining(String trainerUid, String date, String time, String expert, String trainerName,String rating,String max)
    {
        this.trainerUid=trainerUid;
        this.date=date;
        this.time=time;
        this.expert=expert;
        this.trainerName=trainerName;
        this.rating = rating;
        this.max = max;
        getCurrentParticipant();
    }
    public void increaseTrainingCount()
    {
        increaseTrainingAdminCount();
        increaseTrainingByDateCount();
        increaseAllPlanedCount();
        increasePersonalCount();
    }
    public void decreaseTrainingCount()
    {
        decreaseTrainingAdminCount();
        decreaseTrainingByDateCount();
        decreaseAllPlanedCount();
        decreasePersonalCount();
    }
    private void increaseTrainingAdminCount()
    {
        realDataRef.child("AdminDashboard").child(this.expert+"_"+date.replace(" ","_")+time+this.trainerUid)
                .child("Current_Participant").setValue(String.valueOf(totalCount+1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("AdminCurrent_ParticipantIncrease","CountIncreased");
            }
        });
    }
    private void increaseTrainingByDateCount()
    {
        realDataRef.child(expert).child(this.date.replace(" ","_")).child(this.time)
                .child("Current_Participant").setValue(String.valueOf(totalCount+1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("ByDateCurrent_ParticipantIncrease","CountIncreased");
            }
        });

    }
    private void increaseAllPlanedCount()
    {
        realDataRef.child(expert).child("AllPlanedTraining").child(this.date+this.time+this.trainerUid)
                .child("Current_Participant").setValue(String.valueOf(totalCount+1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("AllPlanedCurrent_ParticipantIncrease","CountIncreased");
            }
        });

    }
    private void increasePersonalCount()
    {
        realDataRef.child(expert).child("PersonalPlanTraining").child(this.trainerUid).child(this.date+this.time)
                .child("Current_Participant").setValue(String.valueOf(totalCount+1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                totalCount+=1;
                Log.i("PersonalCurrent_ParticipantIncrease","CountIncreased");
            }
        });
    }
    private void decreaseTrainingAdminCount()
    {
        realDataRef.child("AdminDashboard").child(this.expert+"_"+date.replace(" ","_")+time+this.trainerUid)
                .child("Current_Participant").setValue(String.valueOf(totalCount-1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("AdminCurrent_ParticipantIncrease","CountIncreased");
            }
        });
    }
    private void decreaseTrainingByDateCount()
    {
        realDataRef.child(expert).child(this.date.replace(" ","_")).child(this.time)
                .child("Current_Participant").setValue(String.valueOf(totalCount-1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("ByDateCurrent_ParticipantIncrease","CountIncreased");
            }
        });
    }
    private void decreaseAllPlanedCount()
    {
        realDataRef.child(expert).child("AllPlanedTraining").child(this.date+this.time+this.trainerUid)
                .child("Current_Participant").setValue(String.valueOf(totalCount-1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("AllPlanedCurrent_ParticipantIncrease","CountIncreased");
            }
        });
    }
    private void decreasePersonalCount()
    {
        realDataRef.child(expert).child("PersonalPlanTraining").child(this.trainerUid).child(this.date+this.time)
                .child("Current_Participant").setValue(String.valueOf(totalCount-1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("PersonalCurrent_ParticipantIncrease","CountIncreased");
                totalCount-=1;
            }
        });
    }
    public void participantAddTrainingFireStore()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("Date", date.replace("_"," "));
        map.put("Expertise", expert);
        map.put("Max_Participant", max);
        map.put("Rating", rating);
        map.put("Time", time.substring(0, 2) + ":00-"+time.substring(3, 5)+":00");
        map.put("Trainer_name", trainerName);
        map.put("Trainer_uid", trainerUid);
        firebaseFirestore.collection(ConfigureUID.getUID()).document(date+time).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                        firebaseFirestore.collection(ConfigureUID.getUID()).document(expert).collection(date)
                                .document(date+time).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                firebaseFirestore.collection(ConfigureUID.getUID()).document("AllReservedTraining").collection("AllReservedTraining")
                                        .document(expert+"_"+date+time).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("participantAddTraining","Training Added successfully");
                                    }
                                });
                            }
                });
            }
        });
    }
    public void participantDelTrainingFireStore()
    {
        firebaseFirestore.collection(ConfigureUID.getUID()).document(date+time).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseFirestore.collection(ConfigureUID.getUID()).document(expert).collection(date)
                        .document(date+time).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseFirestore.collection(expert).document(date+time).collection("TraineeIdentifier").document(ConfigureUID.getUID()).delete();
                        firebaseFirestore.collection(ConfigureUID.getUID()).document("AllReservedTraining").collection("AllReservedTraining")
                                .document(expert+"_"+date+time).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                decreaseTrainingCount();
                                Log.i("participantDelTrainingFireStore","Training Removed successfully");
                            }
                        });
                    }
                });
            }
        });
    }
    private void getCurrentParticipant()
    {
        realDataRef.child(expert).child("PersonalPlanTraining").child(this.trainerUid).child(this.date+this.time)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren())
                        {
                            if(ds.getKey().equals("Current_Participant"))
                            {
                                totalCount = Integer.parseInt(ds.getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
