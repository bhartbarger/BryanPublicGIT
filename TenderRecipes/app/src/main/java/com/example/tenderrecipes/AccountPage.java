package com.example.tenderrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AccountPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView userT;
    TextView settingsNameT;
    TextView settingsIdT;
    EditText oldPassInp;
    EditText newPassInp;
    EditText newPassConf;
    Button changePassB;
    Button delAccB;
    ImageView upArrow;
    LinearLayout recipeListLayout;
    ScrollView recipeScroll;
    ConstraintLayout mainLayout;
    ConstraintLayout settingsLayout;
    Boolean delFavActive;
    Toolbar toolbar;
    Account account;
    DrawerLayout drawer;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    NavigationView navigationView;

    int counter = 0;
    int lastItem;
    int favLastItem;
    int favListSize;

    String menuOption;

    FirebaseDatabase database;
    DatabaseReference recipeDataRef;
    DatabaseReference accountDataRef;
    Query lastQuery;
    Query favLastQuery;

    ArrayList<Integer> savedRecipes;
    ArrayList<Integer> favRecipes = new ArrayList<>();
    Spinner sortMenu;

    Intent detailIntent;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        declareVariables();

        setNavDrawer(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        recipeDataRef = database.getReference("Recipes");

        detailIntent = new Intent(AccountPage.this, RecipeDetail.class);

        Intent intent = getIntent();
        account = intent.getParcelableExtra("account");

        savedRecipes = intent.getIntegerArrayListExtra("accList");

        accountDataRef = database.getReference("Accounts").child("" + account.getId()).child("savedRecipes");
        lastQuery = accountDataRef.orderByKey().limitToLast(1);
        favLastQuery = accountDataRef.getParent().child("favorites").orderByKey().limitToLast(1);
        getLastInList();
        getLastInFavList();

        String[] sortMenuOptions = new String[]{"All", "Breakfast", "Lunch", "Dinner", "Dessert", "Favorites"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortMenuOptions);
        sortMenu.setAdapter(adapter);

        if (account.getAdmin().equals("true")) {
            Menu menu = navigationView.getMenu();
            Menu admMenu = menu.addSubMenu("Admin");
            MenuItem item = admMenu.add(R.id.nav_allRecipes, Menu.NONE, Menu.NONE, "All Recipes");
            item.setIcon(R.drawable.ic_allrec);
        }

        loadFavorites();
        setFavListSize();

        setListeners();

        settingsNameT.setText("Name: " + account.getUsername());
        settingsIdT.setText("ID: " + account.getId());


    }

    public void declareVariables() {

        userT = findViewById(R.id.usernameT);
        recipeListLayout = findViewById(R.id.recipeList);
        recipeScroll = findViewById(R.id.recipeScroll);
        upArrow = findViewById(R.id.upArrow);
        sortMenu = findViewById(R.id.sortMenu);
        mainLayout = findViewById(R.id.mainLayout);
        settingsLayout = findViewById(R.id.settingsLayout);
        settingsLayout.setVisibility(View.INVISIBLE);
        delFavActive = false;
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        settingsNameT = findViewById(R.id.settingsNameT);
        settingsIdT = findViewById(R.id.settingsIdT);
        oldPassInp = findViewById(R.id.oldPassInp);
        newPassInp = findViewById(R.id.newPassInp);
        newPassConf = findViewById(R.id.newPassConf);
        changePassB = findViewById(R.id.changePassB);
        delAccB = findViewById(R.id.deleteAccB);
        navigationView = findViewById(R.id.nav_view);
    }

    public void setNavDrawer(Bundle savedInstanceState) {

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.invalidate();

        toolbar.setTitle("My Saved Recipes");
        setSupportActionBar(toolbar);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MyRecipesFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_recipes);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setListeners() {
        sortMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recipeListLayout.removeAllViews();
                menuOption = (String) parent.getItemAtPosition(position);
                loadRecipes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        upArrow.setOnTouchListener(new OnSwipeTouchListener(AccountPage.this) {
            public void onSwipeTop() {
                finish();
            }

        });

        changePassB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changePassword();

            }
        });

        delAccB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteAccount();

            }
        });
    }

    public void changePassword() {
        String oldPass = oldPassInp.getText().toString();
        String newPass = newPassInp.getText().toString();
        String newPassC = newPassConf.getText().toString();

        if (oldPass.equals(account.getPassword())) {
            if (newPass.equals(newPassC) && !newPass.isEmpty()) {
                accountDataRef.getParent().child("password").setValue(newPass);
                Toast.makeText(AccountPage.this, "Password successfully changed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AccountPage.this, "The new password does not match!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AccountPage.this, "The password you entered was incorrect!", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteAccount() {
        builder = new AlertDialog.Builder(AccountPage.this);
        builder.setTitle("Are you sure you want to delete your account? This is irreversible.");
        builder.setPositiveButton("Yes Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                accountDataRef.getParent().removeValue();
                Toast.makeText(AccountPage.this, "You're account and all connected data has been deleted.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("No, take me back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void loadFavorites() {


        accountDataRef.getParent().child("favorites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    favRecipes.add(Integer.parseInt(recipeSnapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void loadRecipes() {
        recipeDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (menuOption == "Favorites") {
                    for (int i = 0; i < favRecipes.size(); i++) {
                        for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                            final Recipe re = recipeSnapshot.getValue(Recipe.class);
                            if (re.getTempC() == favRecipes.get(i)) {
                                drawRecipeCard(re);
                            }

                        }

                    }
                } else if (menuOption == "AdminAllRecipes") {

                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        final Recipe re = recipeSnapshot.getValue(Recipe.class);
                        drawRecipeCard(re);

                    }

                } else {
                    for (int i = 0; i < savedRecipes.size(); i++) {
                        for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                            final Recipe re = recipeSnapshot.getValue(Recipe.class);
                            if ((re.getTempC() == savedRecipes.get(i) && re.getMealTime().equals(menuOption))
                                    || (re.getTempC() == savedRecipes.get(i) && menuOption == "All")) {
                                drawRecipeCard(re);
                            }

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_up);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void drawRecipeCard(final Recipe re) {

        final CardView recipeCard = new CardView(getApplicationContext());
        final TextView nameText = new TextView(getApplicationContext());
        final TextView diff = new TextView(getApplicationContext());
        final ImageView img = new ImageView(getApplicationContext());
        final Button del = new Button(getApplicationContext());
        final Button fav = new Button(getApplicationContext());
        final Button unFav = new Button(getApplicationContext());

        nameText.setText(re.getName());
        nameText.setPadding(0, 90, 0, 0);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams delPar = new LinearLayout.LayoutParams(100, 100);
        delPar.setMargins(850, 0, 0, 0);
        del.setLayoutParams(delPar);
        del.setBackgroundResource(R.drawable.del_x);

        LinearLayout.LayoutParams favPar = new LinearLayout.LayoutParams(100, 100);
        favPar.setMargins(980, 0, 0, 0);
        fav.setLayoutParams(favPar);
        fav.setBackgroundResource(R.drawable.check_not_fav);

        unFav.setLayoutParams(favPar);
        unFav.setBackgroundResource(R.drawable.check_fav);
        unFav.setVisibility(View.INVISIBLE);

        img.setImageResource(AccountPage.this.getResources().getIdentifier(re.getImgName(),
                "drawable", AccountPage.this.getPackageName()));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        recipeCard.addView(img);
        recipeCard.addView(nameText);
        recipeCard.addView(diff);
        recipeCard.addView(del);
        recipeCard.addView(fav);
        recipeCard.addView(unFav);
        recipeCard.setClickable(true);

        if (delFavActive) {
            del.setVisibility(View.VISIBLE);
            fav.setVisibility(View.VISIBLE);
            for (int i = 0; i < favRecipes.size(); i++) {
                if (re.getTempC() == favRecipes.get(i)) {
                    fav.setVisibility(View.INVISIBLE);
                    unFav.setVisibility(View.VISIBLE);
                }
            }
        } else {
            del.setVisibility(View.INVISIBLE);
            fav.setVisibility(View.INVISIBLE);
            unFav.setVisibility(View.INVISIBLE);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
        LinearLayout.LayoutParams spacePar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20);
        recipeCard.setLayoutParams(params);
        final Space space = new Space(getApplicationContext());
        space.setLayoutParams(spacePar);

        recipeListLayout.addView(recipeCard);
        recipeListLayout.addView(space);

        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecipe(recipeCard, space, re);
            }
        });
        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteRecipe(re.getTempC(), fav, unFav);
            }
        });

        unFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unFavoriteRecipe(re.getTempC(), fav, unFav, re, recipeCard, space);
            }
        });

        recipeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailIntent.putExtra("recipeNum", re.getTempC());
                startActivity(detailIntent);
            }
        });


    }

    public void deleteRecipe(final CardView recipeCard, final Space space, final Recipe re) {
        builder = new AlertDialog.Builder(AccountPage.this);
        builder.setTitle("Delete this recipe?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recipeListLayout.removeView(recipeCard);
                recipeListLayout.removeView(space);
                for (int i = 0; i < savedRecipes.size(); i++) {
                    if (savedRecipes.get(i) == re.getTempC()) {
                        savedRecipes.remove(i);
                    }
                }

                accountDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean start = false;
                        counter = 0;
                        for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {

                            int temp = Integer.parseInt(accountSnapshot.getValue().toString());

                            if (re.getTempC() == temp) {
                                start = true;
                            }

                            if (start && re.getTempC() != temp) {
                                accountDataRef.child("" + (counter - 1)).setValue(temp);
                            }

                            counter++;

                            if (counter == lastItem) {
                                removeLast();
                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog = builder.create();
        dialog.show();
    }


    public void favoriteRecipe(final int recN, final Button fav, final Button unFav) {
        builder = new AlertDialog.Builder(AccountPage.this);
        builder.setTitle("Favorite this recipe?");
        builder.setPositiveButton("Favorite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fav.setVisibility(View.INVISIBLE);
                unFav.setVisibility(View.VISIBLE);
                accountDataRef.getParent().child("favorites").child("" + favListSize).setValue(recN);
                accountDataRef.getParent().child("favListSize").setValue(favListSize + 1);
                setFavListSize();
                Toast.makeText(AccountPage.this, "Favorited", Toast.LENGTH_LONG).show();
                fav.setVisibility(View.INVISIBLE);
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void unFavoriteRecipe(final int recN, final Button fav, final Button unFav, final Recipe re, final CardView recipeCard, final Space space) {
        builder = new AlertDialog.Builder(AccountPage.this);
        builder.setTitle("Un-favorite this recipe?");
        builder.setPositiveButton("Un-favorite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fav.setVisibility(View.VISIBLE);
                unFav.setVisibility(View.INVISIBLE);
                if (menuOption.equals("favorites")) {
                    recipeListLayout.removeView(recipeCard);
                    recipeListLayout.removeView(space);
                }
                for (int i = 0; i < favRecipes.size(); i++) {
                    if (favRecipes.get(i) == re.getTempC()) {
                        favRecipes.remove(i);
                    }
                }

                accountDataRef.getParent().child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean start = false;
                        counter = 0;
                        for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                            int temp = Integer.parseInt(accountSnapshot.getValue().toString());

                            if (re.getTempC() == temp) {
                                start = true;
                            }

                            if (start && re.getTempC() != temp) {
                                accountDataRef.getParent().child("favorites").child("" + (counter - 1)).setValue(temp);
                            }
                            counter++;
                            if (counter == favLastItem) {
                                removeLastFav();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Toast.makeText(AccountPage.this, "Removed from favorites", Toast.LENGTH_LONG).show();
                unFav.setVisibility(View.INVISIBLE);
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void getLastInList() {
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String[] split = dataSnapshot.getValue().toString().replaceAll("[^0-9=]", "").split("=");
                    lastItem = (Integer.parseInt(split[0]));
                } else lastItem = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getLastInFavList() {
        favLastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String[] split = dataSnapshot.getValue().toString().replaceAll("[^0-9=]", "").split("=");
                    favLastItem = (Integer.parseInt(split[0]));
                } else favLastItem = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeLast() {

        accountDataRef.child("" + (savedRecipes.size())).removeValue();
        accountDataRef.getParent().child("recListSize").setValue(savedRecipes.size());

    }

    public void removeLastFav() {
        accountDataRef.getParent().child("favorites").child("" + (favRecipes.size())).removeValue();
        accountDataRef.getParent().child("favListSize").setValue(favRecipes.size());

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    public void setFavListSize() {
        accountDataRef.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favListSize = Integer.parseInt(dataSnapshot.child("favListSize").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.nav_recipes:
                settingsLayout.setVisibility(View.INVISIBLE);
                recipeScroll.setVisibility(View.VISIBLE);
                sortMenu.setVisibility(View.VISIBLE);
                recipeListLayout.removeAllViews();
                menuOption = "All";
                loadRecipes();
                toolbar.setTitle("My Saved Recipes");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MyRecipesFragment()).commit();
                break;
            case R.id.nav_settings:
                recipeScroll.setVisibility(View.INVISIBLE);
                sortMenu.setVisibility(View.INVISIBLE);
                settingsLayout.setVisibility(View.VISIBLE);
                toolbar.setTitle("Settings");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;
            case 0:
                settingsLayout.setVisibility(View.INVISIBLE);
                recipeScroll.setVisibility(View.VISIBLE);
                sortMenu.setVisibility(View.VISIBLE);
                recipeListLayout.removeAllViews();
                menuOption = "AdminAllRecipes";
                loadRecipes();
                toolbar.setTitle("All recipes in database");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AdminFragment()).commit();
                break;
            case R.id.nav_deleteRecipes:
                Toast.makeText(AccountPage.this, "Select recipes to delete", Toast.LENGTH_LONG).show();
                delFavActive = !delFavActive;
                recipeListLayout.removeAllViews();
                loadRecipes();
                break;
            case R.id.nav_logout:

                builder = new AlertDialog.Builder(AccountPage.this);
                builder.setTitle("Are you sure you want to log out?");
                builder.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(getApplicationContext(), LogIn.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        startActivity(intent);
                        Toast.makeText(AccountPage.this, "Logged out", Toast.LENGTH_LONG).show();

                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialog = builder.create();
                dialog.show();

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
