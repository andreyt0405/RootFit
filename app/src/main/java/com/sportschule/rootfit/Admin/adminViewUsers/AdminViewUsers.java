package com.sportschule.rootfit.Admin.adminViewUsers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sportschule.rootfit.Admin.AdminActivity;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.R.id;
import com.sportschule.rootfit.R.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminViewUsers extends AppCompatActivity {

    final List<String> userKindToShow = Arrays.asList("All Users","Trainees","Trainers","Admins");
    private final static String TAG = "adminViewUsers";
    String selectedItem = "All Users";

    RecyclerView recyclerView;
    FirebaseFirestore db;
    MyAdapter myAdapter;
    ArrayList<Users> list;
    ArrayList<String> uIDlist;
    String filteredNumber = "";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        list.clear();
        uIDlist.clear();
        Intent intent = new Intent(AdminViewUsers.this, AdminActivity.class);
        AdminViewUsers.this.startActivity(intent);
        AdminViewUsers.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_admin_view_users);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();



        final Spinner spinner = findViewById(R.id.static_spinner);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), layout.spinner_item_drop_down_custome,userKindToShow);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getSelectedItem().toString();
                Log.d(TAG, "onItemSelected: " + selectedItem + ": " + selectedItem.substring(0,selectedItem.length()-1));
                list.clear();
                uIDlist.clear();
                db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e(TAG, "firestore error onEvent: " + error.getMessage() );
                            return;
                        }
                        for(DocumentChange doc:value.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                Log.d(TAG, "onEvent: add: " + doc.toString());
                                list.clear();
                                uIDlist.clear();
                                for(DocumentSnapshot snapshot:value){
                                    Log.d(TAG, "onSuccess: " + snapshot.getId());
                                    Log.d(TAG, "onSuccess: " + snapshot.getString("State"));
                                    Log.d(TAG, "onSuccess: " + selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State")));
                                    Log.d(TAG, "Number filter and: " + filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")));
                                    if(filteredNumber.isEmpty()){
                                        if(selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))){
                                            list.add(snapshot.toObject(Users.class));
                                            uIDlist.add(snapshot.getId());
                                        }
                                        if(selectedItem.equalsIgnoreCase("All Users")){
                                            list.add(snapshot.toObject(Users.class));
                                            uIDlist.add(snapshot.getId());
                                        }
                                    }
                                    else{
                                        Log.d(TAG, "(spinner + number)Number filter and: " + filteredNumber);
                                        if((selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                                (filteredNumber.isEmpty())){
                                            list.add(snapshot.toObject(Users.class));
                                            uIDlist.add(snapshot.getId());
                                            continue;
                                        }
                                        if((selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                                (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                            list.add(snapshot.toObject(Users.class));
                                            uIDlist.add(snapshot.getId());
                                        }
                                        if((selectedItem.equalsIgnoreCase("All Users")) &&
                                                (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                            list.add(snapshot.toObject(Users.class));
                                            uIDlist.add(snapshot.getId());
                                        }
                                    }

                                }
                                break;
                            }

                            if(doc.getType() == DocumentChange.Type.REMOVED){
                                Log.d(TAG, "onEvent: removed: " + doc.toString());
                                list.clear();
                                uIDlist.clear();
                                for(DocumentSnapshot snapshot:value){
                                    Log.d(TAG, "onSuccess: " + snapshot.getId());
                                    Log.d(TAG, "onSuccess: " + snapshot.getString("State"));
                                    Log.d(TAG, "onSuccess: " + selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State")));
                                    if(selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))){
                                        list.remove(snapshot.toObject(Users.class));
                                        uIDlist.remove(snapshot.getId());
                                    }
                                    if(selectedItem.equalsIgnoreCase("All Users")){
                                        list.remove(snapshot.toObject(Users.class));
                                        uIDlist.remove(snapshot.getId());
                                    }
                                }
                                break;
                            }

                            if(doc.getType() == DocumentChange.Type.MODIFIED){
                                Log.d(TAG, "onEvent: modified: " + doc.toString());
                                Log.d(TAG, "onEvent: add: " + value);
                                list.clear();
                                uIDlist.clear();
                                for(DocumentSnapshot snapshot:value){
                                    Log.d(TAG, "onSuccess: " + snapshot.getId());
                                    Log.d(TAG, "onSuccess: " + snapshot.getString("State"));
                                    Log.d(TAG, "onSuccess: " + selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State")));
                                    if(selectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                    if(selectedItem.equalsIgnoreCase("All Users")){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                }
                                break;
                            }
                        }

                        myAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recyclerView = findViewById(R.id.userList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<Users>();
        uIDlist = new ArrayList<String>();
        myAdapter = new MyAdapter(this,list,uIDlist);/*,uIDlist)*/
        recyclerView.setAdapter(myAdapter);

        EditText filterByPhone = findViewById(id.filterByPhone);
        filterByPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredNumber = filterByPhone.getText().toString();
                String spinnerSelectedItem = spinner.getSelectedItem().toString();
                Log.d(TAG, "onTextChanged: spinner selected item:" + spinnerSelectedItem);

                list.clear();
                uIDlist.clear();
                db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e(TAG, "firestore error onEvent: " + error.getMessage() );
                            return;
                        }

                        for(DocumentChange doc:value.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                Log.d(TAG, "onEvent: add: " + doc.toString());
                                list.clear();
                                uIDlist.clear();
                                for(DocumentSnapshot snapshot:value){
                                    Log.d(TAG, "(Text Listener)onEvent: " + filteredNumber.isEmpty());
                                    Log.d(TAG, "(Text Listener)spinnerSelectedItem: " + spinnerSelectedItem);
                                    if((spinnerSelectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) && (filteredNumber.isEmpty())){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                    if((spinnerSelectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                            (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                    if((spinnerSelectedItem.equalsIgnoreCase("All Users"))
                                            && (filteredNumber.isEmpty())){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                        continue;
                                    }
                                    if((spinnerSelectedItem.equalsIgnoreCase("All Users")) &&
                                            (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                }
                                break;
                            }

                            if(doc.getType() == DocumentChange.Type.REMOVED){
                                Log.d(TAG, "onEvent: removed: " + doc.toString());
                                list.clear();
                                uIDlist.clear();
                                for(DocumentSnapshot snapshot:value){
                                    Log.d(TAG, "(Text Listener)Number filter and: " + filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")));
                                    if((spinnerSelectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                            (filteredNumber.isEmpty())){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                        continue;
                                    }
                                    if((spinnerSelectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                            (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                    if((spinnerSelectedItem.equalsIgnoreCase("All Users")) &&
                                            (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                }
                                break;
                            }

                            if(doc.getType() == DocumentChange.Type.MODIFIED){
                                Log.d(TAG, "onEvent: modified: " + doc.toString());
                                Log.d(TAG, "onEvent: add: " + value);
                                list.clear();
                                uIDlist.clear();
                                for(DocumentSnapshot snapshot:value){
                                    Log.d(TAG, "(Text Listener)Number filter and: " + filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")));
                                    if((spinnerSelectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                            (filteredNumber.isEmpty())){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                        continue;
                                    }
                                    if((spinnerSelectedItem.substring(0,selectedItem.length()-1).equalsIgnoreCase(snapshot.getString("State"))) &&
                                            (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                    if((spinnerSelectedItem.equalsIgnoreCase("All Users")) &&
                                            (filteredNumber.equalsIgnoreCase(snapshot.getString("Phone")))){
                                        list.add(snapshot.toObject(Users.class));
                                        uIDlist.add(snapshot.getId());
                                    }
                                }
                                break;
                            }
                        }

                        myAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
}