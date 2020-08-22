package com.example.tenderrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.util.ArrayList;

public class LogIn extends AppCompatActivity {

    private EditText password;
    private EditText username;
    private Button submit;
    private Button skip;
    private TextView register;

    FirebaseDatabase database;
    DatabaseReference dataRef;

    Intent logInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference("Accounts");

        password = findViewById(R.id.passwordField);
        username = findViewById(R.id.usernameField);
        submit = findViewById(R.id.submitBut);
        register = findViewById(R.id.registerB);

        logInt = new Intent(LogIn.this, MainActivity.class);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String user = username.getText().toString().toLowerCase();
                        String pass = password.getText().toString();
                        boolean isFound = false;
                        for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {

                            Account tempAcc = accountSnapshot.getValue(Account.class);
                            if (tempAcc.getUsername().toLowerCase().equals(user) && tempAcc.getPassword().equals(pass)) {
                                logInt.putExtra("accRef", tempAcc.getId());
                                isFound = true;
                                startActivity(logInt);
                            }

                        }

                        if (!isFound) {
                            Toast.makeText(LogIn.this, "That Username or Password was incorrect!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(LogIn.this, Registration.class));

            }
        });

    }
}
