package com.ptitfinal.covidtracing.repository.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ptitfinal.covidtracing.model.Notification;
import com.ptitfinal.covidtracing.model.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDAO {
    private static FirebaseDAO instance;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private static final String TAG = FirebaseDAO.class.getSimpleName();
    public static FirebaseDAO getInstance(){
        if(instance==null){
            return new FirebaseDAO();
        }
        return instance;
    }

    public FirebaseDAO() {
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference();
    }
    private interface onUserChange{
        public void onUserUpdate(List<User> user);
    }
    private onUserChange userChange;
    public onUserChange getIOnUserChange(){
        return userChange;
    }
    public void addKeyToDB(User user){
        mFirebaseDatabase.child("user").push().setValue(user);
    }
    public void getAllUser(){
        final List<User> userList = new ArrayList<>();
        mFirebaseDatabase.child("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        User user = child.getValue(User.class);
                        userList.add(user);
                    }
                    userChange.onUserUpdate(userList);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void update(String key, final String expose){
     mFirebaseDatabase.child("user").orderByChild("key").equalTo(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child:dataSnapshot.getChildren()){
                    child.getRef().child("expose").setValue(expose);
                    System.out.println(child.getRef().child("key"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //for only testing - no use in deploy
    public void addNoti(Notification notification){
        mFirebaseDatabase.child("notification").push().setValue(notification);
    }
    public void getAllNoti(final OnNotificationListener onNotificationListener){
         final List<Notification> listNoti = new ArrayList<>();
//        mFirebaseDatabase.keepSynced(true);
        mFirebaseDatabase.child("notification").addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    if(child.exists()){
                        String content = child.child("content").getValue(String.class);
                        String title = child.child("title").getValue(String.class);
                        Notification notification = new Notification(content,title);
                        notification.setID(child.getKey());
                        listNoti.add(notification);
                    }

                }
                onNotificationListener.onNotificationChange(listNoti);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public interface OnNotificationListener{
        public void onNotificationChange(List<Notification> notificationList);
    }
}
