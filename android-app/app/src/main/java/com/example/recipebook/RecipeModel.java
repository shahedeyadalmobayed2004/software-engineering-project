package com.example.recipebook;

public class RecipeModel {
    private String id;

    private String title;
        private String imageUrl;
        private String category;

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
    }


