package com.example.recipebook;

public interface RecipeBookListener {
    void onSearchRequested(String query, String category);
    void onRecipeClick(RecipeModel recipe);

}
