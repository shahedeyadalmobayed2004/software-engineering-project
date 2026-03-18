package com.example.recipebook;


import org.junit.Test;
import static org.junit.Assert.*;

public class RecipeModelTest {

    @Test
    public void testRecipeModelCreation_AAA_Pattern() {
        // 1. ARRANGE
        String expectedTitle = "Classic Burger";
        String expectedImage = "https://example.com/image.jpg";
        String expectedCategory = "Main Dishes";

        // 2. ACT
        RecipeModel recipe = new RecipeModel(expectedTitle, expectedImage, expectedCategory);

        // 3. ASSERT
        assertEquals("The address must be accurate.ً", expectedTitle, recipe.getTitle());
        assertEquals(expectedImage, recipe.getImageUrl());
        assertEquals(expectedCategory, recipe.getCategory());
    }

    @Test
    public void testSetAndGetId() {
        // ARRANGE
        RecipeModel recipe = new RecipeModel();
        String expectedId = "FIREBASE_ID_123";

        // ACT
        recipe.setId(expectedId);

        // ASSERT
        assertEquals("The ID must be set and read correctly.", expectedId, recipe.getId());
    }
    @Test
    public void testEmptyConstructorNotNull() {
        // ACT
        RecipeModel recipe = new RecipeModel();

        // ASSERT
        assertNotNull("The object must not be null when using an empty constructor.",recipe);
    }
    @Test
    public void testRecipeWithEmptyTitle() {
        // ARRANGE
        String emptyTitle = "";

        // ACT
        RecipeModel recipe = new RecipeModel(emptyTitle, "url", "category");

        // ASSERT
        assertEquals("The model should accept empty text without problems.", emptyTitle, recipe.getTitle());
    }
}