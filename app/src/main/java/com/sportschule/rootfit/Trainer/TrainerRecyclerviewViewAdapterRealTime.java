package com.sportschule.rootfit.Trainer;

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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.TrainingPropertyCollector;

public class TrainerRecyclerviewViewAdapterRealTime extends FirebaseRecyclerAdapter<TrainingPropertyCollector, TrainerRecyclerviewViewAdapterRealTime.trainingViewholder> {
    final private String expert;
    final private String uid;
    private final Context context;
    boolean isPast;
    boolean isPersonal;
    Bundle bundle = new Bundle();

    public TrainerRecyclerviewViewAdapterRealTime(@NonNull FirebaseRecyclerOptions<TrainingPropertyCollector> options, String expert, boolean isPersonal, Boolean isPast , String uid, Context context)
    {
        super(options);
        this.context = context;
        this.expert = expert;
        this.uid = uid;
        this.isPersonal = isPersonal;
        this.isPast = isPast;
    }

    @Override
    protected void onBindViewHolder(@NonNull trainingViewholder holder, int position, @NonNull TrainingPropertyCollector model) {
        holder.date.setText(model.getDate());
        holder.partic.setText(model.getMax_Participant());
        holder.currentPartic.setText(model.getCurrent_Participant());
        holder.time.setText(model.getTime());
        holder.image_sport.setImageResource(model.getImage(this.expert));
        if (this.isPersonal) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("onClick","onClick holder itemView");
                    Intent ITEM_ACTIVITY = new Intent(context, TrainerTrainingItemActivity.class);
                    context.startActivity(ITEM_ACTIVITY);
                    bundle.putString("getDate",model.getDate());
                    bundle.putString("getTrainerExpertise",model.getExpertise());
                    bundle.putInt("getMaxParticipant",Integer.parseInt(model.getMax_Participant()));
                    bundle.putString("getRating",model.getRating());
                    bundle.putString("getTime",model.getTime());
                    bundle.putString("getTrainerName",model.getTrainer_name());
                    bundle.putBoolean("getState",isPast);
                    ITEM_ACTIVITY.putExtra("itemActivity",bundle);
                    context.startActivity(ITEM_ACTIVITY);
                }
            });
        }
        if(this.isPast)
        {
            holder.ratingBar.setRating(Float.parseFloat(model.getRating()));
        }
    }

    @NonNull
    @Override
    public trainingViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(isPersonal && isPast!=true?R.layout.trainer_recyclear_view_personal_item:
               isPast && isPersonal!=true ?R.layout.trainer_recyclear_view_past_item:isPast!=true && isPersonal!=true?R.layout.trainer_recyclear_view_plan_item
                :R.layout.trainer_recyclear_view_past_item, parent, false);
        return new trainingViewholder(view);

    }
    class trainingViewholder extends RecyclerView.ViewHolder {
        private final TextView date, partic, time, currentPartic;
        private final ImageView image_sport;
        private final RatingBar ratingBar;
        public trainingViewholder(@NonNull View itemView)
        {
            super(itemView);
            date = itemView.findViewById(R.id.trainer_plan_date_item);
            partic = itemView.findViewById(R.id.trainer_plan_max_partic_item);
            currentPartic = itemView.findViewById(R.id.trainer_plan_cur_partic_item);
            time = itemView.findViewById(R.id.trainer_plan_time_item);
            image_sport = itemView.findViewById(R.id.image_sport);
            ratingBar = itemView.findViewById(R.id.rating);
        }
    }
}
