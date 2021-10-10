package com.sportschule.rootfit.Trainee;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.TrainingPropertyCollector;

public class TraineeRecyclerviewViewAdapterFireStore extends FirestoreRecyclerAdapter<TrainingPropertyCollector, TraineeRecyclerviewViewAdapterFireStore.trainingViewholder> {
        Bundle bundle = new Bundle();
        private final Context context;
        private final int fragmentNumber;

public TraineeRecyclerviewViewAdapterFireStore(@NonNull FirestoreRecyclerOptions<TrainingPropertyCollector> options,int fragmentNumber, Context context)
        {
        super(options);
        this.fragmentNumber = fragmentNumber;
        this.context=context;

        }

@Override
protected void onBindViewHolder(@NonNull trainingViewholder holder, int position, @NonNull TrainingPropertyCollector model) {
        holder.dateT.setText(model.getDate());
        holder.timeT.setText(model.getTime());
        holder.trainerT.setText(model.getTrainer_name());
        holder.IMAGE_FITNESS.setImageResource(getImage(model.getExpertise()));
        if(fragmentNumber==2)
        {
            holder.ratingBar.setRating(Float.parseFloat(model.getRating()));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Log.i("onClick","onClick holder itemView");
        Intent ITEM_ACTIVITY = new Intent(context, fragmentNumber==0?TraineeTrainingItemActivity.class: TraineeTrainingRateItemActivity.class);
        context.startActivity(ITEM_ACTIVITY);
        bundle.putString("getDate",model.getDate());
        bundle.putString("getTrainerExpertise",model.getExpertise());
        bundle.putInt("getMaxParticipant",Integer.parseInt(model.getMax_Participant()));
        bundle.putString("getRating",model.getRating());
        bundle.putString("getTime",model.getTime());
        bundle.putString("getTrainerName",model.getTrainer_name());
        bundle.putString("getTrainerUid",model.getTrainer_uid());
        ITEM_ACTIVITY.putExtra("itemActivity",bundle);
        context.startActivity(ITEM_ACTIVITY);
        }
        });
        }

@NonNull
@Override
public trainingViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(fragmentNumber==0?R.layout.suscribed_user_card:
                R.layout.past_suscribed_user_card, parent, false);
        return new trainingViewholder(view);

        }
class trainingViewholder extends RecyclerView.ViewHolder {
    private final RatingBar ratingBar;
    private final TextView dateT,timeT,trainerT;
    private final ImageView IMAGE_FITNESS;
    public trainingViewholder(@NonNull View itemView)
    {
        super(itemView);
        ratingBar = itemView.findViewById(R.id.trainee_plan_training_rate);
        dateT = itemView.findViewById(R.id.trainee_plan_training_date);
        timeT = itemView.findViewById(R.id.trainee_plan_training_time);
        trainerT = itemView.findViewById(R.id.trainee_plan_training_trainer_name);
        IMAGE_FITNESS = itemView.findViewById(R.id.trainee_plan_training_image_sport);
    }
}
    public int getImage(String expert)
    {
        switch (expert){
            case "Crossfit":
                return R.drawable.crossfit;
            case "Yoga":
                return R.drawable.yoga;
            case "Aerobics":
                return R.drawable.aerobics;
            case "Fitness":
                return R.drawable.fitness;
            default:
                return R.drawable.fitness;
        }
    }
}
