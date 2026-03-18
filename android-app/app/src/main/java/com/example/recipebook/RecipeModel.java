package com.example.recipebook;

import java.util.List;

public class RecipeModel {
    private String id;

    private String title;
        private String imageUrl;
        private String category;
    private String videoUrl;
    private String userId;
    private List<String> ingredients;
    private List<String> steps;

        public RecipeModel() {}

        public RecipeModel(String title, String imageUrl, String category) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.category = category;
        }

        public String getTitle() {
            return title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getCategory() {
            return category;
        }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    }


