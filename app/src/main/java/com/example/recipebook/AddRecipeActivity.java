package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.recipebook.databinding.ActivityAddRecipeBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {
    ActivityAddRecipeBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Uri imageUri;
    String imageUrl = "";
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.recipeImageView.setImageURI(imageUri);

                    MediaManager.get().upload(imageUri)
                            .option("folder", "recipes")
                            .option("public_id", "recipe_" + System.currentTimeMillis())
                            .callback(new UploadCallback() {
                                @Override
                                public void onStart(String requestId) {
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    imageUrl = (String) resultData.get("secure_url");

                                    runOnUiThread(() -> {
                                        showSnack("Image uploaded!");

                                    });
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    Snackbar.make(binding.getRoot(),
                                                    "Image upload failed. Check your internet and try again.",
                                                    Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Retry", v1 -> binding.selectImageButton.performClick())
                                            .show();
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {
                                }
                            }).dispatch();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clearErrorsOnTextChange();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Recipe");
        }


        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        binding.selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(intent);
        });

        binding.addButton.setOnClickListener(v -> {
            String title = binding.titleEditText.getText().toString().trim();
            String ingredients = binding.ingredientsEditText.getText().toString().trim();
            String steps = binding.stepsEditText.getText().toString().trim();
            String category = binding.categoryEditText.getText().toString().trim();
            String videoUrl = binding.videoUrlEditText.getText().toString().trim();

            if (imageUrl.isEmpty()) {
                showSnack(" select an image");

                return;
            }
            if (title.isEmpty()) {
                markError(binding.titleInputLayout, "Title is required");
                showSnack("Please enter the recipe title 📝");
                return;
            }
            if (ingredients.isEmpty()) {
                markError(binding.ingredientsInputLayout, "Ingredients are required");
                showSnack("Please enter ingredients 🧂");
                return;
            }
            if (steps.isEmpty()) {
                markError(binding.stepsInputLayout, "Steps are required");
                showSnack("Please enter preparation steps 🍳");
                return;
            }
            if (category.isEmpty()) {
                markError(binding.categoryInputLayout, "Category is required");
                showSnack("Please enter the recipe category 🗂️");
                return;
            }
            if (imageUrl.isEmpty()) {
                showSnack("Please select an image for the recipe 📷");
                return;
            }

            String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";

            Map<String, Object> recipe = new HashMap<>();
            recipe.put("title", title);
            recipe.put("ingredients", Arrays.asList(ingredients.split(",")));
            recipe.put("steps", Arrays.asList(steps.split(",")));
            recipe.put("category", category.trim().toLowerCase());
            recipe.put("videoUrl", videoUrl);
            recipe.put("userId", uid);
            recipe.put("imageUrl", imageUrl);

            firestore.collection("recipes")
                    .add(recipe)
                    .addOnSuccessListener(documentReference -> {
                        showSnack("Recipe added successfully!");

                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(binding.getRoot(),
                                        "Failed to add recipe. Check your internet and try again.",
                                        Snackbar.LENGTH_INDEFINITE)
                                .setAction("Retry", v1 -> binding.addButton.performClick())
                                .show();
                    });
        });
    }

    private void markError(com.google.android.material.textfield.TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
    }

    private void showSnack(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.white))
                .setTextColor(ContextCompat.getColor(this, android.R.color.black))
                .setAction("OK", v -> {
                })
                .show();
    }

    private void clearErrorsOnTextChange() {
        binding.titleEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.titleInputLayout.setError(null);
        });
        binding.ingredientsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.ingredientsInputLayout.setError(null);
        });
        binding.stepsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.stepsInputLayout.setError(null);
        });
        binding.categoryEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.categoryInputLayout.setError(null);
        });
    }


}