package com.example.recipebook;

public class RecipeModel {
    private String id;
    private String title;
    private String imageUrl;
    private String category;
    private String calories;
    private String preparationTime;

    public RecipeModel() {
        // Constructor فارغ ضروري لعمل Firebase Firestore
    }

    public RecipeModel(String title, String imageUrl, String category, String calories, String preparationTime) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.category = category;
        this.calories = calories;
        this.preparationTime = preparationTime;
    }

    // Getters
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public String getId() { return id; }
    public String getCalories() { return calories; }
    public String getPreparationTime() { return preparationTime; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setCalories(String calories) { this.calories = calories; }
    public void setPreparationTime(String preparationTime) { this.preparationTime = preparationTime; }
}
