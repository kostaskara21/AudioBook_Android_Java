package com.example.unipiaudiobook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PlayBook extends AppCompatActivity {


    ImageView imgview;

    //TextView For displayng the author and the name of the book we choose
    TextView author;
    TextView name;

    //Making instances for firebase
    FirebaseDatabase db;
    DatabaseReference rf;


    //making an object for the MyText To Speech class
    TextToSpeech textToSpeech;
    ProgressBar progressBar;

    //This is for the PlayButton(we did it with Listener)
    Button speakButton;

    //Set the Story from the Db
    //Must be Private because inside the Listener the variables must be final
    private String storyfromdb;


    //Declaration Shared prefs
    SharedPreferences sh;

    //Declaration of the time var
    long start;
    long stop;

    //f is the final time
    long f;

    //for geting the current user UID
    String uid;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //in the message we store the value that we get in the intent (its the books id name like B1 etc)
    String message ;


    //Declaration for Refering to the Db to see each User how many times has seen a book

    FirebaseDatabase dbUsersLikes;
    DatabaseReference rfUsersLikes;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_book);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //We are geting curret User UId
        if (user != null) {
             uid = user.getUid(); // Unique user ID
        }


        // Retrieve the intent that started this activity
        Intent intent = getIntent();

        // Get data from the intent using the keys We passed the Id)
        //Also Specifies the book in the Database
        message = intent.getStringExtra("Id");


        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();


        //Geting from the Db from Books Instance
        db = FirebaseDatabase.getInstance();
        rf = db.getReference("Books");


        //Instanciating what we need to get the Instance of the DB to see how many times he saw a Book
        dbUsersLikes = FirebaseDatabase.getInstance();
        rfUsersLikes = dbUsersLikes.getReference("UsersLikes");

        //Initializing progresbar
        progressBar = findViewById(R.id.progressBar);
        //Initializing the Button
        speakButton = findViewById(R.id.SpeakButton);

        //geting values from Db for Desplaying the book
        rf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //We have Save The Images to the Db(images saved in the Db Using the name the have in the Drawable ex img1)
                imgview = findViewById(R.id.imageViewDb);
                //We retrive the image from the Db
                String imagename = snapshot.child(message).child("Pic").getValue().toString();
                //This goes to the resources Drawable and searches for the Img name witch was retrived from Db
                int res = getResources().getIdentifier(imagename, "drawable", getPackageName());
                //Here we set the resource witch was found above  in the ImageView
                imgview = findViewById(R.id.imageViewDb);
                imgview.setImageResource(res);

                //Seting the author from Db dor the story we listen
                author = findViewById(R.id.AuthorDb);
                author.setText(snapshot.child(message).child("Author").getValue().toString());
                //Seting the name from Db dor the story we listen
                name = findViewById(R.id.NameDb);
                name.setText(snapshot.child(message).child("Name").getValue().toString());
                //Seting the Story from Db dor the story we listen
                storyfromdb=snapshot.child(message).child("Story").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d("TTS", "Speech started");
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.VISIBLE); // Show progress bar
                            progressBar.setProgress(0);
                        });

                        // Start the progress bar animation
                        startProgressBar(utteranceId);

                        // Start measuring execution time
                        start = System.nanoTime();

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d("TTS", "Speech completed");
                        runOnUiThread(() -> {
                           // progressBar.setVisibility(View.GONE); // Hide progress bar when done
                        });


                        //When the TTs is dun  we need to get the time again so we can get the time user listened
                        stop= System.nanoTime();
                        //count the full time by subtractimg the timestamp we started and the timestamped we stoped
                        f=stop-start;
                        //making milesec to seconds
                        f=f/1000000000;
                        //Saving them in shared prefs
                        sh=getSharedPreferences("time",MODE_PRIVATE);
                        SharedPreferences.Editor editor=sh.edit();

                        //in order to add the extra time we get the already saved one and we add the new one
                        long temp=sh.getLong(uid,0);
                        temp=temp+f;
                        editor.putLong(uid,temp);
                        editor.apply();



                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.d("TTS", "Error occurred during speech");
                    }
                });
            }
        });

        // Set click listener for the Speak button
        speakButton.setOnClickListener(v -> {
            speakWithProgress(storyfromdb);
        });



        //Toast.makeText(this,"Your favorite is "+FavBook,Toast.LENGTH_LONG).show();

    }



    // Function to speak and update progress bar dynamically
    public void speakWithProgress(String text) {
        int estimatedDuration = estimateSpeechDuration(text); // Get estimated duration
        progressBar.setMax(estimatedDuration); // Set max progress
        progressBar.setProgress(0); // Reset progress bar

        // Speak the text
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TTS_ID");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "TTS_ID");


    }


    // Function to estimate speech duration dynamically
    private int estimateSpeechDuration(String text) {
        float wordsPerSecond = 3.5f; // Adjust based on TTS speed
        int wordsCount = text.split("\\s+").length; // Count words
        return (int) (wordsCount / wordsPerSecond * 1000); // Convert to milliseconds
    }

    // Function to start updating the progress bar
    private void startProgressBar(String utteranceId) {
        int estimatedDuration = progressBar.getMax(); // Get max duration
        new Thread(() -> {
            int progress = 0;
            while (progress < estimatedDuration && textToSpeech.isSpeaking()) {
                try {

                    Thread.sleep(100); // Update every 100ms
                    progress += 100; // Increment progress
                    final int finalProgress = progress;
                    runOnUiThread(() -> progressBar.setProgress(finalProgress));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> progressBar.setProgress(estimatedDuration)); // Ensure it completes
        }).start();
    }


    public  void Pause(View v ){

        textToSpeech.stop();
        //on Pause we need to get the time again so we can get the time user listened until on pause is pressed and set them in shared prefs
        stop= System.nanoTime();
        //count the full time by subtracting the timestamp we started and the timestamped we stoped
        f=stop-start;
        //making milesec to seconds
        f=f/1000000000;
        //setting it in shared prefs
        sh=getSharedPreferences("time",MODE_PRIVATE);
        SharedPreferences.Editor editor=sh.edit();

        //in order to add the extra time we get the already saved one and we add the new one
        long temp=sh.getLong(uid,0);
        temp=temp+f;
        editor.putLong(uid,temp);
        editor.apply();

        progressBar.setProgress(0);

    }


    //This function is for showing the Total Streams for each book(it will be removed just keeping the code)
    public  void TotalViews(View v){
        //getting the Views from shared prefs for each book(shared prefs are declared int the Menu Activity on Listen onclick since we want each time we click a book)s
        sh=getSharedPreferences("views",MODE_PRIVATE);
        SharedPreferences.Editor editor=sh.edit();
        int temp=sh.getInt(message,0);
        Toast.makeText(this,"This Book Has total of: "+String.valueOf(temp)+" Views",Toast.LENGTH_LONG).show();

    }

    //This function Showing for each User each book how may times got Listened
    public  void YourViews(View v){

        rfUsersLikes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String text=snapshot.child(uid).child(message).getValue(Integer.class).toString();
                Toast.makeText(getApplicationContext(), "You Have Seen "+text+" times this Book", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}





