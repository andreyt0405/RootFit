package com.sportschule.rootfit.Trainer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;
public class CancelTrainerTraining {
    private final String trainerExpert;
    private final String date;
    private final String time;
    private final String uid;
    private final Context context;
    private final ArrayList<String> traineeUID = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference realDataRef = database.getReference();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CancelTrainerTraining(String trainerExpert,String date,String time, String uid,Context context)
    {
        this.trainerExpert=trainerExpert;
        this.date=date;
        this.time=time;
        this.uid=uid;
        this.context = context;
        showWarningAlert(context);
    }
    private void removeDataByTime()
    {
        realDataRef.child(trainerExpert).child(date.replace(" ","_")).child(time)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("removeDataByTime","removeDataByTime Succeed");
                removeDataAllPlanedTraining();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("removeDataByTime","removeDataByTime Failed");
            }
        });
    }
    private void removeDataAllPlanedTraining()
    {
        realDataRef.child(trainerExpert).child("AllPlanedTraining").child(date.replace(" ","_")+time+uid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("removeDataAllPlanedTraining","removeDataAllPlanedTraining Succeed");
                removeDataPersonalPlanTraining();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("removeDataAllPlanedTraining","removeDataAllPlanedTraining Failed");
            }
        });
    }
    private void removeDataPersonalPlanTraining()
    {
                realDataRef.child(trainerExpert).child("PersonalPlanTraining").child(uid).child(date+time).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.i("removeDataPersonalPlanTraining", "removeDataPersonalPlanTraining Succeed");
                            removeDataAdminPlanTraining();
                        }
                        else
                        {
                            Log.i("removeDataPersonalPlanTraining", "removeDataPersonalPlanTraining error");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("removeDataPersonalPlanTraining", "removeDataPersonalPlanTraining Failed");
                    }
                });
    }
    private void showWarningAlert(Context context) { //Added argument
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cancel the Training");
        builder.setMessage("The remove the training may influence on trainees\n" +
                "Do you sure you want apply this action");
        builder.setPositiveButton("Cancel the Training", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDataByTime();
                Toast.makeText(context, "The training has cancelled", LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void removeDataAdminPlanTraining()
    {
        realDataRef.child("AdminDashboard").child(trainerExpert+"_"+date.replace(" ","_")+time+uid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("removeAdminDashboard","removeAdminDashboard Succeed");
                trainerTrainingRemoveFromFireStore();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("removeDataPersonalPlanTraining","removeDataPersonalPlanTraining Failed");
            }
        });
    }
    private void trainerTrainingRemoveFromFireStore()
    {
        firebaseFirestore.collection(uid).document(date+time).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    firebaseFirestore.collection(uid).document(date+time).delete();
                    Log.i("participantDelTrainingFireStore","Training Removed successfully");
                    firebaseFirestore.collection(trainerExpert).document(date+time).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            documentSnapshot.getReference().collection("TraineeIdentifier").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(int i=0; i<queryDocumentSnapshots.size(); i++)
                                    {
                                        traineeUID.add(queryDocumentSnapshots.getDocuments().get(i).getId());
                                        firebaseFirestore.collection(trainerExpert).document(date+time)
                                                .collection("TraineeIdentifier").document(queryDocumentSnapshots.getDocuments().get(i).getId())
                                                .delete();

                                    }
                                    documentSnapshot.getReference().delete();
                                    removeTraineeTrainingFireStore();
                                }
                            });
                        }
                    });

                }
            }
        });
    }
    private void removeTraineeTrainingFireStore()
    {
        Log.i("trainerExpert+date+time",trainerExpert+date+time);
        for (int i=0; i<traineeUID.size(); i++)
        {
            firebaseFirestore.collection(traineeUID.get(i)).document("AllReservedTraining")
                    .collection("AllReservedTraining").document(trainerExpert+"_"+date+time).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("TrineeTrainingRemoved","Training has been removed from all reserved");
                }
            });

        }
        for (int i=0; i<traineeUID.size(); i++)
        {
            firebaseFirestore.collection(traineeUID.get(i)).document(date+time).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("TrineeTrainingRemoved","Training has been removed");
                }
            });

        }
        for (int i=0; i<traineeUID.size(); i++)
        {
            firebaseFirestore.collection(traineeUID.get(i)).document(trainerExpert).collection(date).document(date+time).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("TrineeTrainingRemoved","Training has been removed");
                }
            });
        }
    }
}
