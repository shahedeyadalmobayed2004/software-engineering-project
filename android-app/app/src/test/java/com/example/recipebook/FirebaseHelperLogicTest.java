package com.example.recipebook;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.widget.ImageButton;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelperLogicTest {

    @Test
    public void testToggleFavoriteLogic_AAA() {
        //ARRANGE
        ImageButton mockButton = mock(ImageButton.class);
        Context mockContext = mock(Context.class);
        RecipeModel mockRecipe = new RecipeModel("Pizza", "url", "Dinner");
        mockRecipe.setId("recipe_123");

        List<RecipeModel> favoriteList = mock(ArrayList.class);

//    ACT
        favoriteList.add(mockRecipe);
        int expectedSize = 1;


//        ASSERT
        verify(favoriteList).add(mockRecipe);

        assertNotNull(favoriteList);
    }

    @Test
    public void testLoadFavorites_EmptyState_AAA() {
//ARRANGE
        List<RecipeModel> mockList = mock(ArrayList.class);
        when(mockList.isEmpty()).thenReturn(true);

//        ACT
        boolean isEmpty = mockList.isEmpty();

//        ASSERT
        assertTrue("The list should be empty if there is no data.", isEmpty);
        verify(mockList).isEmpty();
    }
}