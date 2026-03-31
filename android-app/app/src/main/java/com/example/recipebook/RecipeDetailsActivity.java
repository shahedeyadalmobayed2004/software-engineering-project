package com.example.recipebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recipebook.databinding.ActivityRecipeDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ActivityRecipeDetailsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String recipeId;
    private String creatorId;
    private List<String> ingredientsList;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadRecipeDetails();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        recipeId = getIntent().getStringExtra("recipeId");

        loadRecipeDetails();

        binding.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditRecipeActivity.class);
            intent.putExtra("recipeId", recipeId);
            editLauncher.launch(intent);
        });
        binding.btnShoppingList.setOnClickListener(v -> {
            if (ingredientsList == null || ingredientsList.isEmpty()) {
                Toast.makeText(this, "No ingredients found", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ShoppingListActivity.class);
            intent.putStringArrayListExtra("ingredients", new ArrayList<>(ingredientsList));
            startActivity(intent);
        });

        binding.deleteButton.setOnClickListener(v -> {
            firestore.collection("recipes").document(recipeId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadRecipeDetails() {
        firestore.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String videoUrl = doc.getString("videoUrl");
                        List<String> ingredients = (List<String>) doc.get("ingredients");
                        ingredientsList = (List<String>) doc.get("ingredients");
                        List<String> steps = (List<String>) doc.get("steps");
                        String imageUrl = doc.getString("imageUrl");
                        creatorId = doc.getString("userId");

                        binding.titleText.setText(title);
                        binding.categoryText.setText("Category: " + capitalize(category));
                        binding.videoLinkText.setOnClickListener(v -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                            startActivity(browserIntent);
                        });

                        if (ingredients != null && !ingredients.isEmpty()) {
                            StringBuilder formattedIngredients = new StringBuilder();
                            for (String item : ingredients) {
                                formattedIngredients.append("• ").append(item.trim()).append("\n");
                            }
                            binding.ingredientsText.setText(formattedIngredients.toString().trim());
                            binding.ingredientsText.post(() -> {
                                if (binding.ingredientsText.getLineCount() > 7) {
                                    binding.ingredientsText.setMovementMethod(new ScrollingMovementMethod());
                                    binding.ingredientsText.setVerticalScrollBarEnabled(true);
                                    binding.ingredientsText.setMaxLines(7);
                                    enableSmoothScroll(binding.ingredientsText);
                                } else {
                                    binding.ingredientsText.setMovementMethod(null);
                                    binding.ingredientsText.setVerticalScrollBarEnabled(false);
                                    binding.ingredientsText.setMaxLines(Integer.MAX_VALUE);
                                }
                            });
                        }

                        if (steps != null && !steps.isEmpty()) {
                            StringBuilder formattedSteps = new StringBuilder();
                            for (int i = 0; i < steps.size(); i++) {
                                formattedSteps.append((i + 1)).append(". ").append(steps.get(i).trim()).append("\n");
                            }
                            binding.stepsText.setText(formattedSteps.toString().trim());
                            binding.stepsText.post(() -> {
                                if (binding.stepsText.getLineCount() > 7) {
                                    binding.stepsText.setMovementMethod(new ScrollingMovementMethod());
                                    binding.stepsText.setVerticalScrollBarEnabled(true);
                                    binding.stepsText.setMaxLines(7);
                                    enableSmoothScroll(binding.stepsText);
                                } else {
                                    binding.stepsText.setMovementMethod(null);
                                    binding.stepsText.setVerticalScrollBarEnabled(false);
                                    binding.stepsText.setMaxLines(Integer.MAX_VALUE);
                                }
                            });
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(binding.recipeImage);
                        }

                        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                        if (currentUserId.equals(creatorId)) {
                            binding.editDeleteLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableSmoothScroll(TextView tv) {
        tv.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }}