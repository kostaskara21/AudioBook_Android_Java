package com.example.unipiaudiobook;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ActivityViewModelLazyKt;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.unipiaudiobook.databinding.ActivityMenuBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class Menu extends AppCompatActivity {

    ActivityMenuBinding binding;

    //We are instanciating the Shared Prefs
    SharedPreferences sharedPreferences;

    //Instanciating var to get UId of each User from DB
    String uid;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //This is for counting how many views we have on  each book from all the users (used in shared prefs a counter)
    int countfullviews;


    //objects to refer to the Db for for(for seeing each user clicks on each book)
    //Is for The stat of each User to be able to see how many time he Pressed a Song
     FirebaseDatabase mDatabase;
     DatabaseReference mUsersRef;



    //for seeing each user clicks on each book
    int counterViewUsersForEachBook;


    String FavBook="";
    String FavAuthor="";

    String Moral="";
    FirebaseDatabase Db;
    DatabaseReference Mr;

    //for findint the max  vies for a user to find his fav book
    private Integer max=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Intent intent=getIntent();


        //Geting Uid from each User
        if (user != null) {
             uid = user.getUid(); // Unique user ID
        }


        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance();
        mUsersRef = mDatabase.getReference("UsersLikes"); //geting the instance where we will store



        binding=ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.home){
                replaceFragment(new HomeFragment());

            } else if (item.getItemId()==R.id.stats) {



                replaceFragment(new StatsFragment());

            }
            else {

                // This makes a dialog with message if user presses okay he logs out
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //Here we are getting Users Most seing book
        mUsersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (DataSnapshot snap : snapshot.getChildren()) {
                    Integer value = snap.getValue(Integer.class);
                    if (value != null && value > max){ // Handle first iteration properly
                            max = value;
                            FavBook = snap.getKey();
                        }
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        Db = FirebaseDatabase.getInstance();
        Mr= mDatabase.getReference("Books"); //geting the instance where we will store

        Mr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    FavAuthor = snapshot.child(FavBook).child("Author").getValue(String.class);
                    Moral = snapshot.child(FavBook).child("Moral").getValue(String.class);
                    FavBook = snapshot.child(FavBook).child("Name").getValue(String.class);
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }



    private void replaceFragment(Fragment fragment) {
        //we are pasing to the Fragment the stats for the total View time of each user(So the hi oclick above can be deleted)
        sharedPreferences=getSharedPreferences("time",MODE_PRIVATE);
        long t=sharedPreferences.getLong(uid,0);
        Bundle bundle=new Bundle();
        bundle.putLong("time",t);

        bundle.putString("Author",FavAuthor);
        bundle.putString("Moral",Moral);
        bundle.putString("Book",FavBook);
        fragment.setArguments(bundle);


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }




    //For Every Button We Have The listen Onclick Function
    public void Listen(View v){
        //Each ImageButton Represents a Book
        //In the Db the Books are saved same as Each Buttons Id name: B1 B2..
        //We cast the Button because every UI property comes from the mother class View
        //So we make the View type of the Image Button we want to use to get its Properties
        //In Order to know witch Book to Retrive In the Db we cast the View of the Onclick Button to Image Button and we pass this Buttons Id name to the Activity
        //Now We know witch Button was pressed and also witch Story to Listen Since each Buttons id name  is the same as every book in Db

        ImageButton img=(ImageButton)v;
        Intent intent=new Intent(this,PlayBook.class);
        // giving us the Id of the button(like 63247)
        int buttonId = img.getId();
        //get the name of the button ID (from the XML ID)
        String buttonName = getResources().getResourceEntryName(buttonId);


        //here we are making the share prefs(User can see total Streams of book)
        //key of the shared pref will be
        sharedPreferences = getSharedPreferences("views", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //we are getting the total Views from shared prefs
        countfullviews=sharedPreferences.getInt(buttonName,0);
        //if its 0 means there is now Views
        if(countfullviews==0) {
            countfullviews++;
            editor.putInt(buttonName,countfullviews);
            editor.apply();
        }else{
            //if its not 0 we are getting the value and we add 1
            countfullviews=sharedPreferences.getInt(buttonName,0);
            countfullviews++;
            //putting again to shared prefs
            editor.putInt(buttonName,countfullviews);
            editor.apply();
        }

        //This is for Desplaying For a User how many times He  Listened a Book
        //Every time a User press to Listen to A book We get the times that he already had before and add one
        mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                counterViewUsersForEachBook=snapshot.child(uid).child(buttonName).getValue(Integer.class);
                counterViewUsersForEachBook++;
                mUsersRef.child(uid).child(buttonName).setValue(counterViewUsersForEachBook);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Giving it to the other activity(with this way we retrive the Book from Db)
        intent.putExtra("Id",buttonName);
        startActivity(intent);
    }




}