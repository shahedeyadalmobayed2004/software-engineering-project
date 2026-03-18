package com.example.recipebook;


import android.content.Context;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
public class FirebaseHelper {
    public static void checkIsFavorite(String recipeId, ImageButton favoriteBtn) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        DocumentReference favRef = FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("Favorites").document(recipeId);

        favRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                favoriteBtn.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                favoriteBtn.setImageResource(android.R.drawable.btn_star_big_off);
            }
        });
    }

    public static void toggleFavorite(Context context, RecipeModel recipe, ImageButton favoriteBtn) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference favRef = FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("Favorites").document(recipe.getId());

        favRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                favRef.delete().addOnSuccessListener(aVoid -> {
                    favoriteBtn.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                });
            } else {
                favRef.set(recipe).addOnSuccessListener(aVoid -> {
                    favoriteBtn.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
