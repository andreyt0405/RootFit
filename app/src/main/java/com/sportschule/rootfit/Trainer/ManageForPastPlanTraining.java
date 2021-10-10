package com.sportschule.rootfit.Trainer;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.joda.time.DateTimeComparator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManageForPastPlanTraining {
    private String date;
    private String newDateString = "";
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    public void movePreviousTrainerTraining(String expert, String uid)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(expert).child("PersonalPlanTraining").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Log.i("Key", recipeSnapshot.getKey());
                    if (snapshot.getChildren() != null) {
                        if (compareDateWithCurrentDate(
                                changeDateFormatToAndroid(
                                        trimDateFromDataTime(recipeSnapshot.getKey())))) {
                            copyDateFromPlanTraining(recipeSnapshot.getKey(),expert,uid);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String trimDateFromDataTime(String ds)
    {
        date = ds.substring(0,11).trim();
        return date;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Boolean compareDateWithCurrentDate(String date)
    {
        Log.i("data",date);
        Date date1 = null;
        Date currentDate= null;
        DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
        SimpleDateFormat sdf= new SimpleDateFormat("dd-MM-yyyy");
        try {
            date1=sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            currentDate= sdf.parse(sdf.format(new Date()));
            Log.i("currentDate", String.valueOf(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int retVal = dateTimeComparator.compare(date1, currentDate);
        Log.i("retVal", String.valueOf(retVal));
        return retVal==-1?true:false;
    }
    private String changeDateFormatToAndroid(String dateBase)
    {
        String month = dateBase.replaceAll("[^A-Za-z]+", "");
        if (month.equals("JAN")){newDateString = "01_"+dateBase.substring(4,11);}
        if (month.equals("FEB")){newDateString = "02_"+dateBase.substring(4,11);}
        if (month.equals("MAR")){newDateString = "03_"+dateBase.substring(4,11);}
        if (month.equals("APR")){newDateString = "04_"+dateBase.substring(4,11);}
        if (month.equals("MAY")){newDateString = "05_"+dateBase.substring(4,11);}
        if (month.equals("JUN")){newDateString = "06_"+dateBase.substring(4,11);}
        if (month.equals("JUL")){newDateString = "07_"+dateBase.substring(4,11);}
        if (month.equals("AUG")){newDateString = "08_"+dateBase.substring(4,11);}
        if (month.equals("SEP")){newDateString = "09_"+dateBase.substring(4,11);}
        if (month.equals("OCT")){newDateString = "10_"+dateBase.substring(4,11);}
        if (month.equals("NOV")){newDateString = "11_"+dateBase.substring(4,11);}
        if (month.equals("DEC")){newDateString = "12_"+dateBase.substring(4,11);}
        return subStringDate(newDateString.replace("_","-"));
    }
    private String subStringDate(String date)
    {
        String temp = date.substring(0,2);
        date = date.replace(date.substring(0,2),date.substring(3,5));
        date = date.substring(0,3)+temp+date.substring(5).trim();
        return date;
    }
    private void copyDateFromPlanTraining(String dateTime, String expert, String uid)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(expert);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<String, Object>();
                updates.put("/PastTraining/"+uid+"/"+dateTime+"/",snapshot.child("PersonalPlanTraining").child(uid).child(dateTime).getValue());
                removeValueRealTime(dateTime,expert,uid);
                ref.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("onSuccess","onSuccess");
                    }
                });
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void removeValueRealTime(String date, String expert, String uid)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(expert)
                .child("PersonalPlanTraining").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren())
                {
                    if(child.getKey().equals(date))
                    {
                        child.getRef().setValue(null);
                        Log.i("PersonalPlanTraining","PersonalPlanTraining Moved to Past");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(expert).child("AllPlanedTraining");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot child: snapshot.getChildren())
                                        {
                                            if(child.getKey().equals(date+uid))
                                            {
                                                child.getRef().setValue(null);
                                                Log.i("AllPlanedTraining","AllPlanedTraining Moved to Past");
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AdminDashboard");
                                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for(DataSnapshot child:snapshot.getChildren())
                                                                {
                                                                if(child.getKey().equals(expert+"_"+date+uid))
                                                                {
                                                                    child.getRef().setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.i("AdminDashboard","AdminDashboard Moved to Past");
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
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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
    public void moveTraineeReservedToPast(String uid)
    {
        firebaseFirestore.collection(uid).document("AllReservedTraining")
                .collection("AllReservedTraining").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot qs : queryDocumentSnapshots.getDocuments()) {
                    if (queryDocumentSnapshots.getDocuments() != null) {
                        String newDate = changeDateFormatToAndroid(qs.getId().substring(qs.getId().indexOf("_") + 1, qs.getId().length() - 5));
                        if (compareDateWithCurrentDate(newDate)) {
                            copyQueryDocumentSnapshotsToPast(qs.getId(),newDate,uid);
                        }
                    }
                }
            }
        });
    }
    private void copyQueryDocumentSnapshotsToPast(String snapshotDate,String pastDate,String uid)
    {

        Map<String, Object> updates = new HashMap<String, Object>();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(uid).document("AllReservedTraining").collection("AllReservedTraining")
                .document(snapshotDate).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                for(Map.Entry<String,Object>ds:documentSnapshot.getData().entrySet())
                {
                    updates.put(ds.getKey(),ds.getValue());
                }
                firebaseFirestore.collection(uid).document("AllReservedTraining").collection("AllReservedTraining")
                        .document(snapshotDate).delete();
                firebaseFirestore.collection(uid).document("PastAllReservedTraining").collection("PastAllReservedTraining")
                        .document(snapshotDate).set(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("TrainingMoveToPast","TrainingMoveToPast");
                    }
                });
            }
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int trainerCheckOutTraining(String expert,String dateTraining, String timeTraining)
    {
        String convert = changeDateFormatToAndroid(dateTraining);
        String [] dateParts = convert.split("-");
        String day = dateParts[0];
        String month = dateParts[1];
        String year = dateParts[2];
        convert = year.concat("-"+month.concat("-"+day)).concat(" "+timeTraining.substring(timeTraining.indexOf("_")+1
        )).concat(":00");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.now();
        String date = localDateTime.toString().substring(0,localDateTime.toString().indexOf("T"));
        String time = localDateTime.toString().substring(localDateTime.toString().indexOf("T")+1,localDateTime.toString().indexOf("T")+6);
        LocalDateTime date1 = LocalDateTime.parse(convert, dtf);
        LocalDateTime date2 = LocalDateTime.parse(date+" "+time,dtf);
        if (date1.isEqual(date2) || date1.isBefore(date2)) {
            Log.i("isbefore","before");
            Log.i("dateTraining",dateTraining+timeTraining);
            Log.i("exxpr",expert);
            return 1;
        }

        if (date1.isAfter(date2)) {
            Log.i("date1.isAfter(date2)","0");
            return 0;
        }
        return 0;
    }
}
