package com.sportschule.rootfit.Trainee;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.TrainingPropertyCollector;

public class TraineeRecyclerviewViewAdapterRealTime extends FirebaseRecyclerAdapter<TrainingPropertyCollector, TraineeRecyclerviewViewAdapterRealTime.trainingViewholder> {
    Bundle bundle = new Bundle();
    private final Context context;

    public TraineeRecyclerviewViewAdapterRealTime(@NonNull FirebaseRecyclerOptions<TrainingPropertyCollector> options, Context context)
    {
        super(options);
        this.context=context;

    }

    @Override
    protected void onBindViewHolder(@NonNull trainingViewholder holder, int position, @NonNull TrainingPropertyCollector model) {
        holder.date.setText(model.getDate());
        holder.partic.setText(model.getMax_Participant());
        holder.currentPartic.setText(model.getCurrent_Participant());
        holder.time.setText(model.getTime());
        holder.IMAGE_FITNESS.setImageResource(model.getImage(model.getExpertise()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onClick","onClick holder itemView");
                Intent ITEM_ACTIVITY = new Intent(context, TraineeTrainingItemActivity.class);
                context.startActivity(ITEM_ACTIVITY);
                bundle.putString("getDate",model.getDate());
                bundle.putString("getTrainerExpertise",model.getExpertise());
                bundle.putInt("getMaxParticipant",Integer.parseInt(model.getMax_Participant()));
                bundle.putString("getRating",model.getRating());
                bundle.putString("getTime",model.getTime());
                bundle.putString("getTrainerName",model.getTrainer_name());
                bundle.putString("getTrainerUid",model.getTrainer_uid());
                bundle.putBoolean("getState",false);
                ITEM_ACTIVITY.putExtra("itemActivity",bundle);
                context.startActivity(ITEM_ACTIVITY);
            }
        });
    }

    @NonNull
    @Override
    public trainingViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainee_recyclear_view_plan_item, parent, false);
        return new trainingViewholder(view);

    }
    class trainingViewholder extends RecyclerView.ViewHolder {
        private final TextView date, partic, time, currentPartic;
        private final ImageView IMAGE_FITNESS;
        public trainingViewholder(@NonNull View itemView)
        {
            super(itemView);
            date = itemView.findViewById(R.id.trainee_plan_date_item);
            partic = itemView.findViewById(R.id.trainee_plan_max_partic_item);
            currentPartic = itemView.findViewById(R.id.trainee_plan_cur_partic_item);
            time = itemView.findViewById(R.id.trainee_plan_time_item);
            IMAGE_FITNESS = itemView.findViewById(R.id.image_fitness_type);
        }
    }
}
