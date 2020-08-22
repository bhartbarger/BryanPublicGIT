package com.example.tenderrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {

    Button registration;
    EditText userField;
    EditText passField;
    EditText checkPW;

    ArrayList<Account> accounts = new ArrayList<>();

    FirebaseDatabase database;
    DatabaseReference dataRef;

    Intent logInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        registration = findViewById(R.id.submitButton);
        userField = findViewById(R.id.userField);
        passField = findViewById(R.id.pwField);
        checkPW = findViewById(R.id.checkPWField);

        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference("Accounts");

        logInt = new Intent(Registration.this, MainActivity.class);

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {

                    Account acc = accountSnapshot.getValue(Account.class);
                    accounts.add(acc);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = (accounts.get(accounts.size() - 1).getId() + 1);
                String newUser = userField.getText().toString();
                String newPass = passField.getText().toString();
                String checkPass = checkPW.getText().toString();
                if (!newUser.equals("") && !newPass.equals("")) {
                    boolean userAvailable = true;
                    for (int i = 0; i < accounts.size(); i++) {
                        if (accounts.get(i).getUsername().equals(newUser)) {
                            userAvailable = false;
                        }
                    }
                    if (userAvailable && checkPasswordValidity(newPass) && checkPasswordValidity(checkPass)) {
                        Toast.makeText(Registration.this, "Account Created!", Toast.LENGTH_LONG).show();
                        Account temp = new Account(newUser, newPass, "false", n);
                        dataRef.child("" + n).setValue(temp);
                        logInt.putExtra("accRef", n);
                        startActivity(logInt);

                    } else if (!userAvailable) {
                        Toast.makeText(Registration.this, "Username Unavailable!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Registration.this, "Password must contain: 1 Number, Special, Upper, and Lower case character & 5-10 characters long ", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(Registration.this, "You must enter a Username and Password.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public boolean checkPasswordValidity(String passString) {
        boolean returnValidity = false;
        boolean hasNumber = false;
        boolean hasSpecChar = false;
        boolean hasUpCase = false;
        boolean hasLowCase = false;
        boolean properLength = false;


        if (passString.length() >= 5 && passString.length() <= 10) {
            properLength = true;

            Pattern number = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
            Pattern cap = Pattern.compile("[A-Z]");
            Pattern low = Pattern.compile("[a-z]");

            if (number.matcher(passString).find()) {
                Log.d("passCheck", "Has Number");
                hasNumber = true;
            }

            if (special.matcher(passString).find()) {
                Log.d("passCheck", "Has special");
                hasSpecChar = true;
            }

            if (cap.matcher(passString).find()) {
                Log.d("passCheck", "Has caps");
                hasUpCase = true;
            }

            if (cap.matcher(passString).find()) {
                Log.d("passCheck", "Has low");
                hasLowCase = true;
            }

        }

        if (properLength && hasNumber && hasSpecChar && hasUpCase && hasLowCase) {
            returnValidity = true;
        }
        return returnValidity;
    }

    ;
}
