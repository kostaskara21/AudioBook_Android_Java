package com.example.unipiaudiobook;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    EditText email;
    EditText pass;


    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.EmailEditText);
        pass = findViewById(R.id.PasswordEditText);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }

    public void SignIn(View view) {
        if (email.getText().toString().isEmpty() || pass.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill your informations",
                    Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    // Login success
                    user = auth.getCurrentUser();
                    Toast.makeText(this, "Log In succes",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, Menu.class);
                    intent.putExtra("tag", true);
                    finish();
                    startActivity(intent);
                } else {
                    // Login failed
                    Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void gosignup(View v){
        Intent intent=new Intent(this, SignUp.class);
        startActivity(intent);
    }


    public void language(View v) {
        //we make the View type of the Image View we want to use to get its Properties
        //and we will get its id to know witch language to change to
        ImageView img = (ImageView) v;
        // giving us the Id of the button(like 63247)
        int imgid = img.getId();
        //get the name of the button ID (from the XML ID)
        String imgidname = getResources().getResourceEntryName(imgid);
        if (imgidname.equals("frid")) {
            Locale locale = new Locale("fr"); // ή "fr" για Γαλλικά
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
            //in order to apply changes we need to recreate the activity
            this.recreate();
        } else if (imgidname.equals("spid")) {
            Locale locale = new Locale("es"); //
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
            this.recreate();
        } else {
            Locale locale = new Locale("en"); //
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
            this.recreate();
        }

    }
}