package com.example.recipebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipebook.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private List<RecipeModel> myRecipes = new ArrayList<>();
    private RecipeProfileAdapter adapter;
    private ActivityResultLauncher<Intent> editProfileLauncher;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);


        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUserData();
            loadMyRecipes();
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadUserData();
                        loadMyRecipes();
                    }
                });

        loadUserData();
        loadMyRecipes();

        binding.logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            sharedPreferences.edit().clear().apply();
            finish();
        });

        binding.editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });
//
//        binding.addRecipeFromEmptyStateButton.setOnClickListener(v -> {
//            startActivity(new Intent(this, AddRecipeActivity.class));
//        });
//
//        binding.addRecipeFab.setOnClickListener(v -> {
//            startActivity(new Intent(this, AddRecipeActivity.class));
//        });
    }

    private void loadUserData() {
        String email = auth.getCurrentUser().getEmail();

        firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot user = query.getDocuments().get(0);
                        binding.userName.setText(user.getString("name"));
                        binding.userEmail.setText(user.getString("email"));

                        String imageUrl = user.getString("profileImage");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(binding.userImage);
                        }
                    }
                });
    }

    private void loadMyRecipes() {
        String uid = auth.getCurrentUser().getUid();

        firestore.collection("recipes")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    myRecipes.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        RecipeModel recipe = doc.toObject(RecipeModel.class);
                        if (recipe != null) {
                            recipe.setId(doc.getId());
                            myRecipes.add(recipe);
                        }
                    }

                    adapter = new RecipeProfileAdapter(myRecipes);
                    binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    binding.recipesRecyclerView.setAdapter(adapter);

                    updateUIState();
                    binding.swipeRefreshLayout.setRefreshing(false);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    myRecipes.clear();
                    updateUIState();
                    binding.swipeRefreshLayout.setRefreshing(false);

                });

    }

    private void updateUIState() {
        if (myRecipes.isEmpty()) {
            binding.recipesRecyclerView.setVisibility(View.GONE);
            binding.emptyMessageLayout.setVisibility(View.VISIBLE);
//            binding.addRecipeFromEmptyStateButton.setVisibility(View.VISIBLE);
//            binding.addRecipeFab.setVisibility(View.GONE);
        } else {
            binding.recipesRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyMessageLayout.setVisibility(View.GONE);
//            binding.addRecipeFromEmptyStateButton.setVisibility(View.GONE);
//            binding.addRecipeFab.setVisibility(View.VISIBLE);
        }
    }
}
