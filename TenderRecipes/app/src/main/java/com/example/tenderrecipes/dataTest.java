package com.example.tenderrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class dataTest extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference dataRef;

    ArrayList<Recipe> recipes = new ArrayList<>();

    Button test;
    TextView name;
    TextView diff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_test);

        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference("Recipes");

        test = findViewById(R.id.testB);
        name = findViewById(R.id.nameText);
        diff = findViewById(R.id.diffT);

        //addRecipes();

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });


    }

    public void test(){

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot recipeSnapshot : dataSnapshot.getChildren()){

                    //String diff = recipeSnapshot.getKey();
                    //String name = recipeSnapshot.getKey();
                    Recipe re = recipeSnapshot.getValue(Recipe.class);
                    //Toast.makeText(dataTest.this, ""+diff, Toast.LENGTH_SHORT).show();
                    //Recipe r = new Recipe(name,diff);
                    recipes.add(re);

                }
                StringBuilder temp = new StringBuilder();
                for(int i = 0; i < recipes.size(); i++){

                    if(recipes.get(i).getTempC() == 1){

                        name.setText(recipes.get(i).getName());
                        diff.setText(recipes.get(i).getDifficulty());

                    }

                }
                Toast.makeText(dataTest.this, ""+temp, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void addRecipes(){

        /*Recipe r1 = new Recipe("Grilled Cheese", "testInst", "testIngredient", "Easy", "20 Min", "grilled_cheese", 0);
        Recipe r2 = new Recipe("Fettuccini Alfredo", "testInst", "testIngredient", "Medium", "45 Min", "fettuccini_alfredo", 1);

        dataRef.child(""+r1.getTempC()).setValue(r1);
        dataRef.child(""+r2.getTempC()).setValue(r2);*/

    }
}
