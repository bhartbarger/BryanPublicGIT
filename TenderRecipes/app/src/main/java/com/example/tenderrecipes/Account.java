package com.example.tenderrecipes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Account implements Parcelable {

    private String username;
    private String password;
    private String admin;
    private int id;
    private int recListSize;
    private int favListSize;

    private ArrayList<Integer> savedRecipes;

    public Account() {
        savedRecipes = new ArrayList<>();
        recListSize = 0;
        favListSize = 0;
    }

    public Account(String u, String p, String a, int i) {
        username = u;
        password = p;
        admin = a;
        id = i;
        savedRecipes = new ArrayList<>();
        recListSize = 0;
        favListSize = 0;
    }


    public void addRecipe(int r) {
        savedRecipes.add(r);
    }

    public boolean contains(int r) {
        for (int i = 0; i < savedRecipes.size(); i++) {
            if (savedRecipes.get(i) == r) {
                return true;
            }
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Integer> getSavedRecipes() {
        return savedRecipes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public void setSavedRecipes(ArrayList<Integer> savedRecipes) {
        this.savedRecipes = savedRecipes;
    }

    public int getRecListSize() {
        return recListSize;
    }

    public void setRecListSize(int recListSize) {
        this.recListSize = recListSize;
    }

    public int getFavListSize() {
        return favListSize;
    }

    public void setFavListSize(int favListSize) {
        this.favListSize = favListSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(admin);
        dest.writeInt(id);
        dest.writeInt(recListSize);
        dest.writeInt(favListSize);

    }

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public Account(Parcel in) {
        username = in.readString();
        password = in.readString();
        admin = in.readString();
        id = in.readInt();
        recListSize = in.readInt();
        favListSize = in.readInt();
        savedRecipes = in.readArrayList(Recipe.class.getClassLoader());
    }
}
