package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.recipebook.databinding.ActivityAddRecipeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecipeActivity extends AppCompatActivity {
    ActivityAddRecipeBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Uri imageUri = null;
    String imageUrl = "";
    String recipeId = "";

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
                                public void onStart(String requestId) {}

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {}

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    imageUrl = (String) resultData.get("secure_url");
                                    runOnUiThread(() -> {
                                        Toast.makeText(EditRecipeActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    Log.e("CloudinaryUpload", "Upload error: " + error.getDescription());
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {}
                            }).dispatch();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Recipe");
        }

        recipeId = getIntent().getStringExtra("recipeId");



        firestore.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        binding.titleEditText.setText(doc.getString("title"));
                        binding.ingredientsEditText.setText(String.join(",", (List<String>) doc.get("ingredients")));
                        binding.stepsEditText.setText(String.join(",", (List<String>) doc.get("steps")));
                        binding.categoryEditText.setText(doc.getString("category"));
                        binding.videoUrlEditText.setText(doc.getString("videoUrl"));
                        imageUrl = doc.getString("imageUrl");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(binding.recipeImageView);
                        }
                    }
                });



        binding.selectImageButton.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(pickIntent);
        });

        binding.addButton.setText("Update Recipe");
        binding.addButton.setOnClickListener(v ->{
            String title = binding.titleEditText.getText().toString().trim();
            String ingredients = binding.ingredientsEditText.getText().toString().trim();
            String steps = binding.stepsEditText.getText().toString().trim();
            String category = binding.categoryEditText.getText().toString().trim();
            String videoUrl = binding.videoUrlEditText.getText().toString().trim();

            if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updatedRecipe = new HashMap<>();
            updatedRecipe.put("title", title);
            updatedRecipe.put("ingredients", Arrays.asList(ingredients.split(",")));
            updatedRecipe.put("steps", Arrays.asList(steps.split(",")));
            updatedRecipe.put("category", category.toLowerCase().trim());
            updatedRecipe.put("videoUrl", videoUrl);
            updatedRecipe.put("imageUrl", imageUrl);

            firestore.collection("recipes").document(recipeId)
                    .update(updatedRecipe)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Recipe updated!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });


        });
    }
}
