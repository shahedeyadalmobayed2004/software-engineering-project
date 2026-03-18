package com.example.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.recipebook.databinding.ActivityFavoritesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity {
    private ActivityFavoritesBinding binding;
    private RecipeAdapter adapter;
    private ArrayList<RecipeModel> favoriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        favoriteList = new ArrayList<>();
        adapter = new RecipeAdapter(favoriteList, null);

        binding.favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.favoritesRecyclerView.setAdapter(adapter);

        loadFavorites();
    }
    private void loadFavorites() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "يرجى تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("Favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            RecipeModel recipe = document.toObject(RecipeModel.class);

                            if (recipe != null) {
                                recipe.setId(document.getId());
                                favoriteList.add(recipe);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("FavoritesError", "خطأ في تحويل الوصفة: " + e.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (favoriteList.isEmpty()) {
                        binding.emptyMessageLayout.setVisibility(View.VISIBLE);
                        binding.favoritesRecyclerView.setVisibility(View.GONE);
                    } else {
                        binding.emptyMessageLayout.setVisibility(View.GONE);
                        binding.favoritesRecyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FirestoreError", "فشل جلب البيانات: " + e.getMessage());
                    Toast.makeText(this, "خطأ في الاتصال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}