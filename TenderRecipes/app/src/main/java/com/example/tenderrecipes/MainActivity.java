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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity{

    int counter = 0;

    TextView nameT;
    TextView timeT;
    TextView diffT;
    TextView swipeDetect;

    ImageView recImg;

    FirebaseDatabase database;
    DatabaseReference recipeDataRef;
    DatabaseReference accountDataRef;
    DatabaseReference listRef;
    Query lastQuery;

    int accRef;
    int lastItem;

    ArrayList<Recipe> recipes = new ArrayList<>();

    Account currAccount;
    Intent detailIntent;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detailIntent = new Intent(MainActivity.this, RecipeDetail.class);

        database = FirebaseDatabase.getInstance();
        recipeDataRef = database.getReference("Recipes");
        accountDataRef = database.getReference("Accounts");

        Intent logInt = getIntent();
        Bundle extras = logInt.getExtras();
        accRef = extras.getInt("accRef");
        setAccount(accRef);
        listRef = accountDataRef.child("" + accRef).child("savedRecipes");
        lastQuery = listRef.orderByKey().limitToLast(1);

        getLastInList();

        recImg = findViewById(R.id.recipeImg);
        recImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        nameT = findViewById(R.id.nameText);
        nameT.setPadding(20,0,0,0);
        timeT = findViewById(R.id.timeText);
        timeT.setPadding(20,0,0,0);
        diffT = findViewById(R.id.diffText);
        diffT.setPadding(20,0,0,0);
        swipeDetect = findViewById(R.id.swipeDetect);

        addRecipes();

        swipeDetect.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this){
            public void onSwipeTop() {
                detailIntent.putExtra("recipeNum", recipes.get(counter).getTempC());
                startActivity(detailIntent);
            }
            public void onSwipeRight() {

                saveRecipe();

            }
            public void onSwipeLeft() {

                skipRecipe();

            }
            public void onSwipeBottom() {
                accountPage(currAccount);
            }
        });

    }

    public void saveRecipe(){
        if(recipes.size() == 2){
            Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Recipe saved", Toast.LENGTH_SHORT).show();
            save(currAccount, recipes.get(counter).getTempC());
            recipes.remove(counter);
            Random randInt = new Random();
            int randCount = counter;
            while(randCount == counter){
                counter = randInt.nextInt(recipes.size());
            }

            setCurrentRecipe(recipes, nameT, timeT, diffT, counter);
        }
    }

    public void skipRecipe(){
        if(recipes.size() == 2){
            Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Recipe Skipped", Toast.LENGTH_SHORT).show();
            Random randInt = new Random();
            int randCount = counter;
            while(randCount == counter){
                counter = randInt.nextInt(recipes.size());
            }

            setCurrentRecipe(recipes, nameT, timeT, diffT, counter);
        }
    }

    public void setAccount(final int dataRef){

        accountDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot accountSnapshot : dataSnapshot.getChildren()){

                    Account tempAccount = accountSnapshot.getValue(Account.class);
                    if(dataRef == tempAccount.getId()) {
                        currAccount = accountSnapshot.getValue(Account.class);
                        currAccount.getSavedRecipes().removeAll(Collections.singleton(null)); }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void save(Account currAccount, Integer rNum){

        if(!currAccount.contains(rNum)) {

            currAccount.addRecipe(rNum);
            getLastInList();
            accountDataRef.child("" + currAccount.getId()).child("savedRecipes")
                    .child(""+currAccount.getRecListSize()).setValue(rNum);
            accountDataRef.child(""+currAccount.getId()).child("recListSize").setValue(currAccount.getRecListSize()+1);

        }else{
            Toast.makeText(MainActivity.this, "This recipe is already saved", Toast.LENGTH_SHORT).show();
        }


    }

    public void getLastInList(){
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    String[] split = dataSnapshot.getValue().toString().replaceAll("[^0-9=]","").split("=");
                    lastItem = (Integer.parseInt(split[0])+1);
                }else lastItem= 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void accountPage(Account current){

        Intent i = new Intent(MainActivity.this, AccountPage.class);
        i.putExtra("account", current);
        i.putIntegerArrayListExtra("accList", current.getSavedRecipes());
        startActivity(i);
        overridePendingTransition(R.anim.slide_down, R.anim.no_animation);

    }

    public void setCurrentRecipe(ArrayList<Recipe> recipes, TextView nameT, TextView timeT, TextView diffT, int counter){

        Recipe currRec = recipes.get(counter);
        nameT.setText(currRec.getName());
        timeT.setText("Time: " + currRec.getTime());
        diffT.setText("Difficulty: " + currRec.getDifficulty());
        recImg.setImageResource(MainActivity.this.getResources().getIdentifier(recipes.get(counter).getImgName(),
                "drawable", MainActivity.this.getPackageName()));

    }


    public void addRecipes(){

        recipeDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot recipeSnapshot : dataSnapshot.getChildren()){

                    Recipe re = recipeSnapshot.getValue(Recipe.class);
                    recipes.add(re);

                }

                Random rand = new Random();
                int n = rand.nextInt(recipes.size());
                setCurrentRecipe(recipes, nameT, timeT, diffT, n);
                recImg.setImageResource(MainActivity.this.getResources().getIdentifier(recipes.get(n).getImgName(),
                        "drawable", MainActivity.this.getPackageName()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
