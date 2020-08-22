package com.example.tenderrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetail extends AppCompatActivity {

    ImageView imgV;
    TextView nameText, diffText, timeText, ingText, instText, swipeDetect;

    int recipeNum;

    FirebaseDatabase database;
    DatabaseReference recipeDataRef;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        imgV = findViewById(R.id.recipeImage);
        nameText = findViewById(R.id.nameText);
        diffText = findViewById(R.id.diffText);
        timeText = findViewById(R.id.timeText);
        ingText = findViewById(R.id.ingredientText);
        instText = findViewById(R.id.instructionsText);
        swipeDetect = findViewById(R.id.swipeDetect);

        database = FirebaseDatabase.getInstance();
        recipeDataRef = database.getReference("Recipes");

        Intent detailIntent = getIntent();
        Bundle extras = detailIntent.getExtras();
        recipeNum = extras.getInt("recipeNum");

        recipeDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {

                    Recipe re = recipeSnapshot.getValue(Recipe.class);
                    if (re.getTempC() == recipeNum) {
                        nameText.setText(re.getName());
                        imgV.setImageResource(RecipeDetail.this.getResources().getIdentifier(re.getImgName(),
                                "drawable", RecipeDetail.this.getPackageName()));
                        imgV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        diffText.setText(re.getDifficulty());
                        timeText.setText(re.getTime() + "\n");
                        ingText.setText("Ingredients: \n" + re.getIngredients().replace("_n", "\n") + "\n");
                        instText.setText("Instructions: \n" + re.getInstructions().replace("_n", "\n") + "\n");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        swipeDetect.setOnTouchListener(new OnSwipeTouchListener(RecipeDetail.this) {
            public void onSwipeBottom() {
                finish();
            }
        });

    }
}
