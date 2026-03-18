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

import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ActivityRecipeDetailsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String recipeId;
    private String creatorId;
    private RecipeModel currentRecipe; // أضفت هذا السطر لحفظ بيانات الوصفة الحالية

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

        // 1. فحص حالة المفضلات وتلوين الزر فور فتح الشاشة
        // ملاحظة: تأكدي أن ID الزر في XML هو favoriteBtn
        FirebaseHelper.checkIsFavorite(recipeId, binding.favoriteBtn);

        loadRecipeDetails();

        // 2. تفعيل زر المفضلات عند الضغط (Toggle)
        binding.favoriteBtn.setOnClickListener(v -> {
            if (currentRecipe != null) {
                FirebaseHelper.toggleFavorite(this, currentRecipe, binding.favoriteBtn);
            }
        });

        binding.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditRecipeActivity.class);
            intent.putExtra("recipeId", recipeId);
            editLauncher.launch(intent);
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
                        // تحويل المستند إلى Object من نوع RecipeModel
                        currentRecipe = doc.toObject(RecipeModel.class);
                        if (currentRecipe != null) {
                            currentRecipe.setId(doc.getId()); // التأكد من حفظ الـ ID
                        }

                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String videoUrl = doc.getString("videoUrl");
                        List<String> ingredients = (List<String>) doc.get("ingredients");
                        List<String> steps = (List<String>) doc.get("steps");
                        String imageUrl = doc.getString("imageUrl");
                        creatorId = doc.getString("userId");

                        binding.titleText.setText(title);
                        binding.categoryText.setText("Category: " + capitalize(category));

                        binding.videoLinkText.setOnClickListener(v -> {
                            if (videoUrl != null && !videoUrl.isEmpty()) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                                startActivity(browserIntent);
                            }
                        });

                        // تنسيق المكونات
                        if (ingredients != null && !ingredients.isEmpty()) {
                            StringBuilder formattedIngredients = new StringBuilder();
                            for (String item : ingredients) {
                                formattedIngredients.append("• ").append(item.trim()).append("\n");
                            }
                            binding.ingredientsText.setText(formattedIngredients.toString().trim());
                            setupTextScrolling(binding.ingredientsText);
                        }

                        // تنسيق الخطوات
                        if (steps != null && !steps.isEmpty()) {
                            StringBuilder formattedSteps = new StringBuilder();
                            for (int i = 0; i < steps.size(); i++) {
                                formattedSteps.append((i + 1)).append(". ").append(steps.get(i).trim()).append("\n");
                            }
                            binding.stepsText.setText(formattedSteps.toString().trim());
                            setupTextScrolling(binding.stepsText);
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

    // دالة مساعدة لتنظيم السكرول داخل النص
    private void setupTextScrolling(TextView tv) {
        tv.post(() -> {
            if (tv.getLineCount() > 7) {
                tv.setMovementMethod(new ScrollingMovementMethod());
                tv.setVerticalScrollBarEnabled(true);
                tv.setMaxLines(7);
                enableSmoothScroll(tv);
            } else {
                tv.setMovementMethod(null);
                tv.setVerticalScrollBarEnabled(false);
                tv.setMaxLines(Integer.MAX_VALUE);
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
    }
}