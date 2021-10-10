package com.sportschule.rootfit.Trainee;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.sportschule.rootfit.ConfigureUID;
import com.sportschule.rootfit.DatePickerFragment;
import com.sportschule.rootfit.LoginActivity;
import com.sportschule.rootfit.PersonalUserActivity;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.ToastCustomMessage;
import com.sportschule.rootfit.Trainer.ManageForPastPlanTraining;
import com.sportschule.rootfit.TrainingPropertyCollector;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;


public class TraineeActivity extends AppCompatActivity {
    DatePickerFragment datePickerFragment = new DatePickerFragment();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference realDataRef = database.getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    StorageReference storageReference = storage.getReference();
    DatabaseReference Statistics = database.getReference().child("Statistics");
    TraineeRecyclerviewViewAdapterRealTime recycleViewAdapterRealTime;
    TraineeRecyclerviewViewAdapterFireStore recycleViewAdapterFireStore;
    Spinner spinnerSportTrainee;
    Button dateButtonPlan, dateButtonPast;
    DatePickerDialog datePickerDialog;
    RecyclerView recyclerView;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    ImageView profile;
    private ArrayList<String> listTraining = new ArrayList<String>();
    private SharedPreferences sharedPreferences;
    private String getSelectedSpinnerItem = "";
    private static final String SHARED_PREFS = "sharedPrefs";
    private TextView welcomeName;
    private String nameLast = "";
    Intent traineeIntent;
    private int fragmentTabCount;
    private String planDate = datePickerFragment.getTodayDate(),
            pastDate = datePickerFragment.getYesterdayDate();
    ArrayMap<String,Integer> StatisticsHashMap = new ArrayMap<>();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UpdateStatisticsDB();
        traineeIntent = new Intent(TraineeActivity.this, LoginActivity.class);
        traineeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(traineeIntent);
        mAuth.signOut();
        toast.toastMessage("Hope you see you again,Going logout..");
        TraineeActivity.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        ManageForPastPlanTraining checkForPastTrainerTraining = new ManageForPastPlanTraining();
        checkForPastTrainerTraining.moveTraineeReservedToPast(ConfigureUID.getUID());
        setContentView(R.layout.activity_trainee);
        findViewById(R.id.trainee_home_page);
        findViewById(R.id.trainee_training_page);
        findViewById(R.id.trainee_unbook_page);
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        Statistics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    StatisticsHashMap.put(ds.getKey(),Integer.parseInt(ds.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        setFragmentLayout();
        new CountDownTimer(500, 100) {
            public void onTick(long millisUntilFinished) {
                {

                }
            }

            public void onFinish() {
                lisetnerForValueRemoved();
                checkAdminDashboardTraining();
                loadHomePageWelcomeName();
                profile = findViewById(R.id.profile_icon);
                loadProfileImageSharedPref(sharedPreferences);
                initDatePickerTrainee(0);
            }
        }.start();
    }

    private void setFragmentLayout() {
        TabLayout tabLayoutTrainee = findViewById(R.id.tablayout_trainee);
        ViewPager viewPagerTrainee = findViewById(R.id.viewPager_trainee);
        PageAdapterTrainee pageAdapterTrainee = new PageAdapterTrainee(getSupportFragmentManager(), tabLayoutTrainee.getTabCount());
        viewPagerTrainee.setAdapter(pageAdapterTrainee);
        viewPagerTrainee.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        tabLayoutTrainee.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerTrainee.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    pageAdapterTrainee.notifyDataSetChanged();
                    profile = findViewById(R.id.profile_icon);
                    loadHomePageWelcomeName();
                    loadProfileImageSharedPref(sharedPreferences);
                    initDatePickerTrainee(tab.getPosition());
                } else if (tab.getPosition() == 1) {
                    pageAdapterTrainee.notifyDataSetChanged();
                    initDatePickerTrainee(tab.getPosition());
                } else if (tab.getPosition() == 2) {
                    pageAdapterTrainee.notifyDataSetChanged();
                    initDatePickerTrainee(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_fragment_menu, menu);
        Log.i("onCreateOptionsMenu", "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("onOptionsItemSelected", item.getTitle().toString());
        if (item.getTitle().toString().equals("Personal")) {
            traineeIntent = new Intent(TraineeActivity.this, PersonalUserActivity.class);
        } else {
            UpdateStatisticsDB();
            mAuth.signOut();
            traineeIntent = new Intent(TraineeActivity.this, LoginActivity.class);
            traineeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(traineeIntent);
            toast.toastMessage("Hope you see you again,Going logout..");
        }
        TraineeActivity.this.startActivity(traineeIntent);
        TraineeActivity.this.finish();
        return false;
    }

    public void loadHomePageWelcomeName() {
        welcomeName = findViewById(R.id.welcome_back_edit);
        nameLast = sharedPreferences.getString(ConfigureUID.getUID() + "firstName", "") +
                " " + sharedPreferences.getString(ConfigureUID.getUID() + "lastName", "");
        welcomeName.setText(nameLast);
    }

    private void downloadProfileImageFirebase() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            storageReference.child("users").child(ConfigureUID.getUID()).child("profile").getDownloadUrl().
                    addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i("uri",uri.toString());
                            Picasso.get().load(uri).into(profile);
                            editor.putString(ConfigureUID.getUID() + "profileImage",uri.toString()).apply();
                            Log.i("profileImage", "profile image loaded");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    int errorCode = ((StorageException) exception).getErrorCode();
                    String errorMessage = exception.getMessage();
                    Log.i("errorCode", String.valueOf(errorCode));
                }
            });
        }

    private void loadProfileImageSharedPref(SharedPreferences sharedPreferences) {
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
        } else {
            downloadProfileImageFirebase();
        }
    }

    private void initDatePickerTrainee(int tabCount) {
        fragmentTabCount = tabCount;
        spinnerSportTrainee = findViewById(R.id.spinner_sport_trainee);
        initTraineeSearchProgress(spinnerSportTrainee, tabCount,
                (tabCount == 0 || tabCount == 1) ? planDate : pastDate);
        dateButtonPlan = findViewById(R.id.picker_date_plan_button);
        dateButtonPast = findViewById(R.id.picker_date_plan_button);
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                if (tabCount == 0 || tabCount == 1) {
                    planDate = datePickerFragment.makeDateString(day, month, year);
                    if(planDate.length()<11){planDate=convertOddDayDate(planDate);}
                    dateButtonPlan.setText(planDate.contains("_") ? planDate.replace("_", " ") : planDate);
                } else {
                    pastDate = datePickerFragment.makeDateString(day, month, year);
                    if(pastDate.length()<11){pastDate=convertOddDayDate(pastDate);}
                    dateButtonPast.setText(pastDate);
                }
                spinnerSportTrainee.setVisibility(View.VISIBLE);
                initTraineeSearchProgress(spinnerSportTrainee, tabCount, tabCount == 0 || tabCount == 1 ? planDate : pastDate);
            }
        };
        datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener, datePickerFragment.year, datePickerFragment.month, datePickerFragment.day);
        if (tabCount == 0 || tabCount == 1) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dateButtonPlan.setText(planDate.contains("_") ? planDate.replace("_", " ") : planDate);
        } else {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000 * (24 * 60 * 60));
            dateButtonPast.setText(pastDate);
        }

    }

    public void datePickerTraineeDialog(View view) {
        datePickerDialog.show();
    }

    public void allPlanedTrainingByExpertise(View view) {
        initTrainingQueryRealTime(getSelectedSpinnerItem, "");
        spinnerSportTrainee.setVisibility(View.GONE);
        dateButtonPlan.setText(R.string.string_plan_training);
    }

    private void initTraineeSearchProgress(Spinner spinnerType, int tabCount, String date) {
        ArrayAdapter<String> spinnerSportTypeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.spinnerTraineePlanItem));
        spinnerType.setAdapter(spinnerSportTypeAdapter);
        spinnerType.setSelection(setDefaultValueSpinner(getSelectedSpinnerItem));
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateButtonPlan.setText(date);
                getSelectedSpinnerItem = spinnerType.getSelectedItem().toString();
                if (tabCount == 1) {
                    initTrainingQueryRealTime(getSelectedSpinnerItem, date);
                } else {
                    initTrainingQueryFireStore(getSelectedSpinnerItem, tabCount, date);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int setDefaultValueSpinner(String getSelectedSpinnerItem) {
        switch (getSelectedSpinnerItem) {
            case "Crossfit":
                return 0;
            case "Aerobics":
                return 1;
            case "Yoga":
                return 2;
            case "Fitness":
                return 3;
            default:
                return 0;
        }
    }

    private void initTrainingQueryRealTime(String referenceChild, String date) {
        final DatabaseReference mbase = FirebaseDatabase.getInstance().getReference()
                .child(date.equals("") ? "AdminDashboard" : referenceChild).child(date.equals("") ? "" : date
                        .replace(" ", "_"));
        recyclerView = findViewById(R.id.recycler_show_trainee_training);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<TrainingPropertyCollector> options = new FirebaseRecyclerOptions.Builder<TrainingPropertyCollector>()
                .setQuery(mbase, TrainingPropertyCollector.class).build();
        recycleViewAdapterRealTime = new TraineeRecyclerviewViewAdapterRealTime(options, this);
        recyclerView.setAdapter(recycleViewAdapterRealTime);
        super.onStart();
        recycleViewAdapterRealTime.startListening();
    }

    private void initTrainingQueryFireStore(String referenceChild, int tabCount, String date) {
        Log.i("initTrainingQueryFireStore","initTrainingQueryFireStore");
        final Query query = firebaseFirestore.collection(ConfigureUID.getUID()).document(date.equals("") && tabCount == 0 ? "AllReservedTraining" :
                (date.equals("") && tabCount == 2 ? "PastAllReservedTraining" : referenceChild))
                .collection(date.equals("") && tabCount == 0 ? "AllReservedTraining" : date.equals("") && tabCount == 2 ? "PastAllReservedTraining" :
                        date.replace(" ", "_"));
        recyclerView = findViewById(R.id.recycler_show_trainee_training);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirestoreRecyclerOptions<TrainingPropertyCollector> response = new FirestoreRecyclerOptions.Builder<TrainingPropertyCollector>()
                .setQuery(query, TrainingPropertyCollector.class)
                .build();
        recycleViewAdapterFireStore = new TraineeRecyclerviewViewAdapterFireStore(response,tabCount, this);
        recyclerView.setAdapter(recycleViewAdapterFireStore);
        super.onStart();
        recycleViewAdapterFireStore.startListening();
    }

    public void initSearchByFitnessType(View view) {
        if (fragmentTabCount == 0) {
            dateButtonPlan.setText(R.string.string_plan_training);
        } else {
            dateButtonPast.setText(R.string.string_plan_training);
        }
        spinnerSportTrainee.setVisibility(View.GONE);
        initTrainingQueryFireStore(getSelectedSpinnerItem, fragmentTabCount, "");
    }

    private void checkAdminDashboardTraining() {
        listTraining.clear();
        realDataRef.child("AdminDashboard").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(!Arrays.asList(listTraining).contains(ds.getKey())){
                        listTraining.add(ds.getKey());
                    }
                }
                Log.i("training", listTraining.toString());
                    checkFireStoreTraineeTraining();
            }
        });
    }
    private String convertOddDayDate(String date) {
        String oddDayDate = "";
        for (int i = 0; i < date.length(); i++) {
            oddDayDate += date.charAt(i);

            if (i == date.indexOf(" ")) {
                oddDayDate += "0";
            }
        }
        return oddDayDate;
    }
    private void checkFireStoreTraineeTraining() {
        if (!listTraining.isEmpty()) {
            firebaseFirestore.collection(ConfigureUID.getUID()).document("AllReservedTraining").collection("AllReservedTraining")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot qds : queryDocumentSnapshots.getDocuments()) {
                        if (!Arrays.asList(listTraining).contains(qds.getId())) {
                            final String expert = qds.getId().substring(0, qds.getId().indexOf("_"));
                            final String date = qds.getId().substring(qds.getId().indexOf("_") + 1);
                        }
                    }
                }
            });
        } else {
            firebaseFirestore.collection(ConfigureUID.getUID()).document("AllReservedTraining").collection("AllReservedTraining")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot qds : queryDocumentSnapshots.getDocuments()) {
                        String expert = qds.getId().substring(0, qds.getId().indexOf("_"));
                        String date = qds.getId().substring(qds.getId().indexOf("_") + 1);
                        participantDelTrainingFireStore(expert, date);
                    }
                }
            });
        }
    }
    private void participantDelTrainingFireStore(String EXPERT,String DATE_TIME)
    {
        Log.i("date",EXPERT+DATE_TIME);
        firebaseFirestore.collection(ConfigureUID.getUID()).document(DATE_TIME).delete();
        firebaseFirestore.collection(ConfigureUID.getUID()).document(EXPERT).collection(DATE_TIME.substring(0,DATE_TIME.length()-5))
                .document(DATE_TIME).delete();
        Log.i("DATE_TIME.substring(0,DATE_TIME.length()-4)",DATE_TIME.substring(0,DATE_TIME.length()-4));
        firebaseFirestore.collection(ConfigureUID.getUID()).document("AllReservedTraining").collection("AllReservedTraining")
                .document(EXPERT+"_"+DATE_TIME).delete();
        firebaseFirestore.collection(EXPERT).document(DATE_TIME).collection("TraineeIdentifier")
                .document(ConfigureUID.getUID()).delete();
    }
    private void lisetnerForValueRemoved()
    {
        realDataRef.child("AdminDashboard").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listTraining.clear();
                checkAdminDashboardTraining();
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

}
