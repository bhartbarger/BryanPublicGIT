package com.example.tenderrecipes;

public class Recipe {

    private String name;
    private String instructions;
    private String ingredients;
    private String difficulty;
    private String time;
    private String imgName;
    private String culture;
    private String mealTime;
    private int tempC;

    public Recipe() {

    }

    public Recipe(String n, String inst, String ing, String diff, String t, String imgN, String c, String mT, int tC) {
        name = n;
        instructions = inst;
        ingredients = ing;
        difficulty = diff;
        time = t;
        imgName = imgN;
        culture = c;
        mealTime = mT;
        tempC = tC;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTempC() {
        return tempC;
    }

    public void setTempC(int tempC) {
        this.tempC = tempC;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }
}
