package com.sportschule.rootfit.Trainer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mcsoft.timerangepickerdialog.RangeTimePickerDialog;
import com.shawnlin.numberpicker.NumberPicker;
import com.sportschule.rootfit.ConfigureUID;
import com.sportschule.rootfit.DatePickerFragment;
import com.sportschule.rootfit.LoginActivity;
import com.sportschule.rootfit.PersonalUserActivity;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.ToastCustomMessage;
import com.sportschule.rootfit.TrainingPropertyCollector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainerActivity extends AppCompatActivity implements RangeTimePickerDialog.ISelectedTime {
    DatePickerFragment datePickerFragment = new DatePickerFragment();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference Statistics = database.getReference().child("Statistics");
    DatabaseReference realDataRef = database.getReference("");
    TrainerRecyclerviewViewAdapterRealTime recycleViewAdapter;
    DatePickerDialog datePickerDialog;
    Spinner spinnerSportType,spinnerSportTypePast;
    Button createTrainButton,dateButtonPlan,timSelectButton, dateButtonPast;
    RecyclerView recyclerView;
    static LayoutInflater inflater;
    static View toastLayout;
    ToastCustomMessage toast;
    Intent trainerIntent;
    private View parentLayout;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS = "sharedPrefs";
    private String timeSelected = null;
    private boolean criticalSectionBlock = false;
    private int DAY_OF_MONTH;
    private boolean onSelectTime = false;
    private String getSelectedSpinnerItem = "", trainerExpertise;
    private String startTime = "",endTime ="";
    private String planDate = datePickerFragment.getTodayDate(),
            pastDate = datePickerFragment.getYesterdayDate();
    ArrayMap<String,Integer> StatisticsHashMap = new ArrayMap<>();
    public void onBackPressed() {
        super.onBackPressed();
        UpdateStatisticsDB();
        trainerIntent = new Intent(TrainerActivity.this, LoginActivity.class);
        trainerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(trainerIntent);
        toast.toastMessage("Hope you see you again,Going logout..");
        this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        trainerExpertise = sharedPreferences.getString(ConfigureUID.getUID() + "expert", "");
        parentLayout = findViewById(android.R.id.content);
        setContentView(R.layout.activity_trainer);
        findViewById(R.id.trainer_home_page);
        findViewById(R.id.trainer_training_page);
        findViewById(R.id.trainee_unbook_page);
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
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.toast_message_custom, (ViewGroup)findViewById(R.id.toast_root));
        toast = new ToastCustomMessage(this,toastLayout);
        setFragmentLayout();
    }

    private void initDatePickerPlan(int tabCount) {
        spinnerSportType = findViewById(R.id.spinner_sport_types);
        spinnerSportTypePast = findViewById(R.id.spinner_sport_types_over);
        initSearchProgress(((tabCount == 0 || tabCount == 1) ? spinnerSportType : spinnerSportTypePast), tabCount,
                tabCount == 0 || tabCount == 1 ? planDate : pastDate);
        dateButtonPlan = findViewById(R.id.pickerButtonDate);
        dateButtonPast = findViewById(R.id.picker_button_date_over);
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                if(tabCount==1) {
                    createTrainButton.setEnabled(true);
                }
                month = month + 1;
                DAY_OF_MONTH = day;
                if (tabCount == 0 || tabCount == 1) {
                    planDate = datePickerFragment.makeDateString(day, month, year);
                    if(planDate.length()<11){planDate=convertOddDayDate(planDate);}
                    dateButtonPlan.setText(planDate.contains("_") ? planDate.replace("_", " ") : planDate);
                    initSearchProgress(spinnerSportType, tabCount, planDate);
                } else {
                    pastDate = datePickerFragment.makeDateString(day, month, year);
                    if(pastDate.length()<11){pastDate=convertOddDayDate(pastDate);}
                    dateButtonPast.setText(pastDate);
                    initSearchProgress(spinnerSportTypePast, tabCount, pastDate);
                }
            }
        };
        datePickerDialog = new DatePickerDialog(this,AlertDialog.THEME_HOLO_LIGHT, dateSetListener, datePickerFragment.year, datePickerFragment.month, datePickerFragment.day);
        if (tabCount == 0 || tabCount == 1) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dateButtonPlan.setText(planDate.contains("_") ? planDate.replace("_", " ") : planDate);
        } else {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000*(24*60*60));
            dateButtonPast.setText(pastDate);
        }
    }

    private void setFragmentLayout() {
        TabLayout tabLayoutTrainer = findViewById(R.id.tablayout_trainer);
        ViewPager viewPagerTrainer = findViewById(R.id.viewPager_trainer);
        PageAdapterTrainer pageAdapterTrainer = new PageAdapterTrainer(getSupportFragmentManager(), tabLayoutTrainer.getTabCount());
        viewPagerTrainer.setAdapter(pageAdapterTrainer);
        viewPagerTrainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position==0)
                {
                    initDatePickerPlan(0);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPagerTrainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        tabLayoutTrainer.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerTrainer.setCurrentItem(tab.getPosition());
                pageAdapterTrainer.notifyDataSetChanged();
                switch (tab.getPosition()) {
                    case 1:
                        Calendar cal = Calendar.getInstance();
                        timSelectButton = findViewById(R.id.pickerButtonTime);
                        if(timeSelected!=null){timSelectButton.setText(timeSelected);}
                        DateFormat dateFormat = new SimpleDateFormat("dd");
                        DAY_OF_MONTH = Integer.parseInt(dateFormat.format(cal.getTime()));
                        createTrainButton = findViewById(R.id.save_data_trinee);
                        initDatePickerPlan(tab.getPosition());
                        break;
                    default:
                        getSelectedSpinnerItem = "";
                        initDatePickerPlan(tab.getPosition());
                        break;
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
        Intent trainerIntent;
        if (item.getTitle().toString().equals("Personal")) {
            trainerIntent = new Intent(TrainerActivity.this, PersonalUserActivity.class);
        } else {
            UpdateStatisticsDB();
            trainerIntent = new Intent(TrainerActivity.this, LoginActivity.class);
            trainerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            toast.toastMessage("Hope you see you again,Going logout..");
        }
        TrainerActivity.this.startActivity(trainerIntent);
        TrainerActivity.this.finish();
        return false;
    }

    public void pickUpDateTrainer(View view) {
        datePickerDialog.show();
    }

    public void pickUpTimeTrainer(View view) {
        createTrainButton.setEnabled(true);
        RangeTimePickerDialog dialog = new RangeTimePickerDialog().newInstance();
        dialog.setIs24HourView(true);
        dialog.setColorTabSelected(R.color.gray_low_opacity);
        dialog.setColorTextButton(R.color.white);
        dialog.setColorBackgroundTimePickerHeader(R.color.blue_low_opacity);
        dialog.setColorBackgroundHeader(R.color.blue_low_opacity);
        FragmentManager fragmentManager = getFragmentManager();
        dialog.show(fragmentManager, "");
    }

    @Override
    public void onSelectedTime(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        boolean sectionBlock = false;
        Calendar datetime = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, hourStart);
        datetime.set(Calendar.MINUTE, minuteStart);
        datetime.set(Calendar.DAY_OF_MONTH, DAY_OF_MONTH);
        if (datetime.getTimeInMillis() >= c.getTimeInMillis()) {
            createTrainButton.setEnabled(true);
        } else {
            toast.toastMessage(R.string.time_has_over);
            createTrainButton.setEnabled(false);
            sectionBlock = true;
            hourStart = 10;
            hourEnd = 12;
        }
        if (sectionBlock == false) {
            if (hourStart < 10 || hourStart > 22 || hourEnd < 10 || hourEnd > 22 || (hourEnd - hourStart > 2)) {
                createTrainButton.setEnabled(false);
                hourStart = 10;
                hourEnd = 12;
                toast.toastMessage("Training is 10:00 - 22:00\nMaximum Training 2 Hours");
            } else {
                createTrainButton.setEnabled(true);
            }
            startTime = String.valueOf(hourStart);
            endTime = String.valueOf(hourEnd);
            timSelectButton.setText(hourStart + ":00" + "-" + hourEnd + ":00");
            timeSelected = hourStart + ":00" + "-" + hourEnd + ":00";
            onSelectTime = true;
        }
    }

    public void createPlanTraining(View view) {
        if (onSelectTime == true) {
            NumberPicker numberPicker = findViewById(R.id.number_partic_picker);
            Map<String, String> creationTrain = new HashMap<>();
            String fireBaseRealTime;
            creationTrain.put("Time", startTime + ":00" + "-" + endTime + ":00");
            creationTrain.put("Date", planDate.replaceAll("_", " "));
            creationTrain.put("Max_Participant", String.valueOf(numberPicker.getValue()));
            creationTrain.put("Current_Participant", "0");
            creationTrain.put("Trainer_uid", ConfigureUID.getUID());
            creationTrain.put("Trainer_name",sharedPreferences.getString(ConfigureUID.getUID() + "firstName", ""));
            creationTrain.put("Rating","0");
            creationTrain.put("Expertise",trainerExpertise);
            creationTrain.put("NumberTraineeRate","0");
            creationTrain.put("isCheckout","0");
            fireBaseRealTime = startTime + "_" + endTime;
            checkValidationDateTime(fireBaseRealTime, creationTrain);
        }
        else
        {
            toast.toastMessage(R.string.prompt_check_time);
            createTrainButton.setEnabled(false);
        }
    }

    private void addTrainingPlanRealTimeBase(String fireBaseRealTime, Map trainPlan, String date) {
        realDataRef.child(trainerExpertise).child(date).child(fireBaseRealTime).setValue(trainPlan)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("onComplete", "Train onComplete creation");
                        addTrainerPlanDbStructure(trainPlan, date, fireBaseRealTime);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("onFailure", "Train onFailure creation");
            }
        });
    }

    private void checkValidationDateTime(String fireBaseRealTime, Map creationTrain) {
        criticalSectionBlock = false;
        planDate = planDate.replaceAll(" ", "_");
        String time = startTime + "_" + endTime;
        realDataRef.child(trainerExpertise).child(planDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.i("training_create", "snapshot not exists training_created");
                    addTrainingPlanRealTimeBase(fireBaseRealTime, creationTrain, planDate);
                    createTrainButton.setEnabled(true);
                    criticalSectionBlock = true;
                } else if (snapshot.exists() && criticalSectionBlock == false) {
                    int trainStartH;
                    int trainEndH;
                    List<String> listTime = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        listTime.add(ds.getKey());
                        trainStartH = Integer.parseInt(ds.getKey().substring(0, 2));
                        trainEndH = Integer.parseInt(ds.getKey().substring(3));
                        if (ds.getKey().equals(time)) {
                            createTrainButton.setEnabled(false);
                            toast.toastMessage(R.string.training_taken);
                            criticalSectionBlock = true;
                            break;
                        }
                        if ((Integer.parseInt(startTime) >= trainStartH) && (Integer.parseInt(startTime) < trainEndH)) {
                            createTrainButton.setEnabled(false);
                            toast.toastMessage(R.string.training_taken);
                            criticalSectionBlock = true;
                            break;
                        }
                        if ((Integer.parseInt(endTime) > trainStartH) && (Integer.parseInt(endTime) <= trainEndH)) {
                            createTrainButton.setEnabled(false);
                            toast.toastMessage(R.string.training_taken);
                            criticalSectionBlock = true;
                            break;
                        }
                    }
                    if (criticalSectionBlock != true) {
                        addTrainingPlanRealTimeBase(fireBaseRealTime, creationTrain, planDate);
                        criticalSectionBlock = true;
                        createTrainButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addTrainerPlanDbStructure(Map trainPlan, String date, String time) {
        Map<String, String> db_account = new HashMap<>();
        realDataRef.child(trainerExpertise).child("AllPlanedTraining").child(date + time + ConfigureUID.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                realDataRef.child(trainerExpertise).child("AllPlanedTraining").child(date + time + ConfigureUID.getUID()).setValue(trainPlan);
                realDataRef.child(trainerExpertise).child("PersonalPlanTraining").child(ConfigureUID.getUID()).child(date + time).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){
                        realDataRef.child(trainerExpertise).child("PersonalPlanTraining").child(ConfigureUID.getUID()).child(date + time).setValue(trainPlan);
                        realDataRef.child("AdminDashboard").child(trainerExpertise+date + time+ConfigureUID.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                realDataRef.child("AdminDashboard").child(trainerExpertise+"_"+date + time+ConfigureUID.getUID()).setValue(trainPlan);
                                db.collection(trainerExpertise).document(date + time).set(db_account);
                                Snackbar.make(parentLayout, "Training subscribed Successfully",
                                        Snackbar.LENGTH_LONG).show();
                            }

                            @Override

                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initTrainingQueryFireBase(String referenceChild, int tabCount, String date) {
        final DatabaseReference mbase = FirebaseDatabase.getInstance().getReference().child(referenceChild.equals("My Training") ? trainerExpertise : referenceChild)
                .child(tabCount==0 || tabCount==1?referenceChild.equals("My Training") ? "PersonalPlanTraining" : date.equals("") ? "AllPlanedTraining"
                        : (date.contains(" ") ? date.replace(" ", "_") : date):referenceChild.equals("My Training") ? "PastTraining":!date.equals("")?date.replace(" ","_"):"")
                .child(referenceChild.equals("My Training") ? ConfigureUID.getUID() : "");

        recyclerView = findViewById((tabCount == 0 || tabCount == 1) ? R.id.recycler_trainer_plan : R.id.recycler_past_training);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<TrainingPropertyCollector> options = new FirebaseRecyclerOptions.Builder<TrainingPropertyCollector>()
                .setQuery(mbase, TrainingPropertyCollector.class).build();
        recycleViewAdapter = new TrainerRecyclerviewViewAdapterRealTime(options, referenceChild.equals("My Training") ? trainerExpertise : referenceChild, referenceChild.equals("My Training"),
                tabCount != 0 && tabCount != 1,ConfigureUID.getUID(), this);
        recyclerView.setAdapter(recycleViewAdapter);
        super.onStart();
        recycleViewAdapter.startListening();
    }
    public void initSearchProgress(Spinner spinnerType, int tabCount, String date) {
        ArrayAdapter<String> spinnerSportTypeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
        getResources().getStringArray(R.array.spinnerTrainerPlanItems));
        spinnerType.setAdapter(spinnerSportTypeAdapter);
        spinnerType.setSelection(setDefaultValueSpinner(getSelectedSpinnerItem.equals("") || getSelectedSpinnerItem.equals("My Training") ? trainerExpertise : getSelectedSpinnerItem));
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSelectedSpinnerItem = spinnerType.getSelectedItem().toString();
                String BTN_STRING = getSelectedSpinnerItem.equals("My Training")?tabCount==0 || tabCount==1?
                        "Scheduled Training":"Previous Training":date.contains("_") ? date.replace("_", " "):date;
                Button BTN_DATE = tabCount==0 || tabCount==1?dateButtonPlan:dateButtonPast;
                BTN_DATE.setText(BTN_STRING);
                initTrainingQueryFireBase(getSelectedSpinnerItem, tabCount, date);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private int setDefaultValueSpinner(String trinerExperties)  {
        switch (trinerExperties) {
            case "Crossfit":
                return 0;
            case "Aerobics":
                return 1;
            case "Yoga":
                return 2;
            case "Fitness":
                return 3;
            case "My Training":
                return 4;
            default:
                return 4;
        }
    }

    public void initTrainingQueryPlanResult(View view) {
        dateButtonPlan.setText(R.string.string_plan_training);
        initTrainingQueryFireBase(getSelectedSpinnerItem, 0, "");
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
    private void UpdateStatisticsDB() {
        int SignOUTCntr;
        SignOUTCntr = Integer.valueOf(StatisticsHashMap.get("SignOUT"));
        SignOUTCntr++;
        Statistics.child("SignOUT").setValue(SignOUTCntr);
    }
}