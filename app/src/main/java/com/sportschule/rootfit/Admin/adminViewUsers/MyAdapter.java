package com.sportschule.rootfit.Admin.adminViewUsers;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sportschule.rootfit.R;
import com.sportschule.rootfit.RemoveUserAccount;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;

public class MyAdapter extends RecyclerView.Adapter <MyAdapter.MyViewHolder>{
    Context context;
    ArrayList<Users> list;
    ArrayList<String> listFirebaseIDs;
    FirebaseFirestore db;
    FirebaseDatabase dbPhone = FirebaseDatabase.getInstance();
    DatabaseReference root = dbPhone.getReference("");
    ArrayList<String> firebasePhoneList = new ArrayList<>();
    ArrayMap<String,String> arrayMap = new ArrayMap<>();

    public MyAdapter(Context context, ArrayList<Users> list, ArrayList<String> listIDs) {/*,ArrayList<String> listIDs*/
        this.context = context;
        this.list = list;
        this.listFirebaseIDs = listIDs;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_item_card_view,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        root.child("PhoneUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                    arrayMap.put(ds.getKey(),ds.getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error){
                Log.e("myfirebase",error.getMessage());
            }
        });
        root.child("PhoneUsers").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d("MyAdapter", "onChildAdded:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d("MyAdapter", "onChildChanged:" + dataSnapshot.getKey());

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d("MyAdapter", "onChildRemoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Log.d("myfirebase", "user Exists: datasnapshot: " + ds);
                    firebasePhoneList.add(ds.getKey());
                }
                Log.d("MyAdapter", "onChildMoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("MyAdapter", "postComments:onCancelled", databaseError.toException());
                Toast.makeText(context,"Failed to load comments.",Toast.LENGTH_SHORT).show();
            }
        });
        db = FirebaseFirestore.getInstance();
        Users user = list.get(position);
        String uID =  listFirebaseIDs.get(position);
        holder.First.setText(user.getFirst());
        holder.Last.setText(user.getLast());
        holder.Age.setText(user.getAge());
        holder.Phone.setText(user.getPhone());
        holder.Gender.setText(user.getGender());
        holder.Email.setText(user.getEmail());
        holder.State.setText(user.getState());
        holder.Username.setText(user.getUsername());
        Picasso.get().load(user.getProfileIcon(uID)).into(holder.userProfile);

        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MyAdapterAdminViewUsers", position + ": Firebase Uid: " + uID + " User Data: " + user.createAccountInfoString());
                if(user.getState().equalsIgnoreCase("Admin")){
                    Toast.makeText(context,"Admin Users Cannot be deleted!",LENGTH_LONG).show();
                    Log.d("MyAdapter", "Admin user cannot be deleted!");
                    return;
                }
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure to Delete?")
                        .setPositiveButton("Yes-Delete User", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new RemoveUserAccount(uID);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        }).show();
            }
        });

        holder.Update.setOnClickListener(new View.OnClickListener() {
            final HashMap<String,String> temp = new HashMap<String,String>();
            @Override
            public void onClick(View v) {
                Log.d("MyAdapterAdminViewUsers", position + ": Firebase Uid: " + uID + " User Data: " + user.createAccountInfoString());
                DialogPlus dialogPlus = DialogPlus.newDialog(holder.Update.getContext())
                        .setContentHolder(new ViewHolder(R.layout.updatedialogcontent))
                        .setExpanded(true,1150)
                        .create();

                View updateView = dialogPlus.getHolderView();
                EditText First =  updateView.findViewById(R.id.updatefirstname_admin_update);
                EditText Last =  updateView.findViewById(R.id.updatelastname_admin_update);
                EditText Age =  updateView.findViewById(R.id.updateage);
                EditText Phone =  updateView.findViewById(R.id.updatephone);
                Button updateButton = updateView.findViewById(R.id.updatesubmit);
                Button updateCancel = updateView.findViewById(R.id.updatecancel);
                First.setText(user.getFirst());
                Last.setText(user.getLast());
                Age.setText(user.getAge());
                Phone.setText(user.getPhone());

                updateCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogPlus.dismiss();
                    }
                });

                updateButton.setOnClickListener(new View.OnClickListener() {
                    /*boolean succssesflag = true;*/
                    @Override
                    public void onClick(View v) {
                        First.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        Last.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        Age.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        Phone.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        if (!user.getState().equalsIgnoreCase("Admin")) {
                            if (!(Phone.getText().toString().equals("")) && Phone.getText().toString().length()==10 &&
                                    (!(Age.getText().toString().equals("")) &&Integer.parseInt(Age.getText().toString()) <= 99 && Integer.parseInt(Age.getText().toString()) >=10)
                            && First.getText().toString().matches("^[a-zA-Z]+$") && Last.getText().toString().matches("^[a-zA-Z]+$")){
                                boolean phoneFlag = checkIfPhoneNumberExists(Phone.getText().toString(), firebasePhoneList, uID);
                            temp.put("First", First.getText().toString());
                            temp.put("Last", Last.getText().toString());
                            temp.put("Age", Age.getText().toString());
                            temp.put("Phone", Phone.getText().toString());
                            Log.d("MyAdapter", "phoneFlag: " + phoneFlag);
                            if (phoneFlag) {
                                Toast.makeText(holder.Update.getContext(), "Phone Number already exists in DB!please enter different one!", LENGTH_LONG).show();
                                Phone.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                                return;
                            }
                            db.collection("users").document(uID)
                                    .update("First", First.getText().toString()
                                            , "Last", Last.getText().toString()
                                            , "Age", Age.getText().toString()
                                            , "Phone", Phone.getText().toString())/**/
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("MyAdapter", "DocumentSnapshot successfully updated!");
                                            updateAgeStatistics(Integer.parseInt(Age.getText().toString()),Integer.parseInt(user.Age));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("MyAdapter", "Error updating document", e);
                                }
                            });

                            Map<String, Object> updatedPhoneNumber = new HashMap<>();
                            updatedPhoneNumber.put(Phone.getText().toString(), uID);
                            root.child("PhoneUsers").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        if (ds.getValue().equals(uID)) {
                                            ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("DataPhoneNumber", "DataPhoneNumber Deleted");
                                                    root.child("PhoneUsers").updateChildren(updatedPhoneNumber);
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            dialogPlus.dismiss();
                            Toast.makeText(holder.Update.getContext(), "User Info successfully updated!", LENGTH_LONG).show();
                        }
                            else
                            {
                                if(First.getText().toString().matches(".*\\d.*")||First.getText().toString().matches(".*[^A-Za-z0-9].*")||First.getText().toString().equals("")) {
                                    First.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                                }
                                if(Last.getText().toString().matches(".*\\d.*")||Last.getText().toString().matches(".*[^A-Za-z0-9].*")||Last.getText().toString().equals("")) {
                                    Last.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                                }
                                if((Age.getText().toString().equals("") || Integer.parseInt(Age.getText().toString()) > 99 || Integer.parseInt(Age.getText().toString()) <10))
                                {
                                    Age.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);}
                                if((Phone.getText().toString().equals("")) || Phone.getText().toString().length()!=10) {
                                    Phone.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_error, 0);
                                }
                            }
                        }
                        else{
                            Toast.makeText(holder.Update.getContext(),"Admin data not updatable!",Toast.LENGTH_LONG).show();
                        }



                    }
                });

                dialogPlus.show();

            }

            private boolean checkIfPhoneNumberExists(String phoneNum, ArrayList<String> firebasePhoneList,String uid) {
                for(String i:firebasePhoneList){
                    /*Log.d("checkIfPhoneNumberExists", "check..." + i.equals(phoneNum));*/
                    if(i.equals(phoneNum)){
                        if (uid.equals(arrayMap.get(phoneNum)))
                        {
                            Log.i("thereuild","equal");
                            return false;
                        }
                        else {
                                Log.i("thereuild","NO equal");
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView First, Last, Age, Phone, Gender, Email, State, Username;
        Button Delete,Update;
        ImageView userProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfile = itemView.findViewById(R.id.admin_user_profile);
            First = itemView.findViewById(R.id.firstName);
            Last = itemView.findViewById(R.id.lastName);
            Age = itemView.findViewById(R.id.age);
            Phone = itemView.findViewById(R.id.phone);
            Gender = itemView.findViewById(R.id.gender);
            Email = itemView.findViewById(R.id.email);
            State = itemView.findViewById(R.id.state);
            Username = itemView.findViewById(R.id.username);
            Delete = itemView.findViewById(R.id.singleItemCardView_Delete);
            Update = itemView.findViewById(R.id.singleItemCardView_updateInfo);
        }
    }
    private void updateAgeStatistics(int newAgeUpdate,int oldAge) {
        Log.i("newAgeUpdate",String.valueOf(newAgeUpdate));
        Log.i("oldAge",String.valueOf(oldAge));
        root.child("Statistics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    int numStart, numEnd;
                    String var = ds.getKey().replaceAll("[^0-9]+", "");
                    if (var.length() != 0) {
                        numStart = Integer.parseInt(var.substring(0, 2).trim());
                        numEnd = Integer.parseInt(var.substring(2, 4).trim());
                        if(newAgeUpdate >= numStart && newAgeUpdate <= numEnd && oldAge >= numStart && oldAge <= numEnd){break;}
                        if (oldAge >= numStart && oldAge <= numEnd) {
                            ds.getRef().setValue(Integer.parseInt(ds.getValue().toString()) - 1);
                        }
                        if (newAgeUpdate >= numStart && newAgeUpdate <= numEnd) {
                            ds.getRef().setValue(Integer.parseInt(ds.getValue().toString()) + 1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
