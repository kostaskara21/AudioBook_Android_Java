package com.example.unipiaudiobook;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class SignUp extends AppCompatActivity {


    TextView emailsignup;
    TextView passsignup;

    FirebaseAuth authe;
    FirebaseUser userr;


     FirebaseDatabase mDatabase;
     DatabaseReference mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailsignup=findViewById(R.id.EmailEditTextSignUp);
        passsignup=findViewById(R.id.PasswordEditTextSignUp);

        authe= FirebaseAuth.getInstance();

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance();
        mUsersRef = mDatabase.getReference("UsersLikes"); //geting the instance where we will store

    }



    public void SignUpgo(View view) {
        if (emailsignup.getText().toString().isEmpty() || passsignup.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill your informations",
                    Toast.LENGTH_SHORT).show();
        } else {
            authe.createUserWithEmailAndPassword(emailsignup.getText().toString(), passsignup.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userr = authe.getCurrentUser();
                    if(userr!=null) {

                        //Storing for each user(eatch uid) all books in Firebase
                        mUsersRef.child(userr.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //every user who signed in automatically assigned also in the UsersLikes Db
                                //So every User can have a counter for all the Books
                                //checking if user exists if not generating an instance in the Db for that User
                                if(!snapshot.exists()){
                                    Map<String, Integer> userDetails = new HashMap<>();
                                    for (int i=1;i<=12 ;i++){
                                        userDetails.put("B"+String.valueOf(i),0);
                                    }
                                    mUsersRef.child(userr.getUid()).setValue(userDetails);
                                }else{

                                    Toast.makeText(getApplicationContext(),"this exiists",Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    // Login failed
                    Toast.makeText(this, "This email already exist ",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    }