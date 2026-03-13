package com.example.recipebook;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipebook.databinding.FragmentRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class RecipeFragment extends Fragment implements RecipeBookListener {

    private FragmentRecipeBinding binding;
    private List<RecipeModel> fullList = new ArrayList<>();
    private List<RecipeModel> filteredList = new ArrayList<>();
    ActivityResultLauncher<Intent> launcher;

    private RecipeAdapter adapter;
    private String currentCategory;
    FirebaseFirestore firestore;

    public RecipeFragment() {
        // Required empty public constructor
    }


    public static RecipeFragment newInstance(String category) {
        RecipeFragment fragment = new RecipeFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeBinding.inflate(inflater, container, false);

        firestore = FirebaseFirestore.getInstance();
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadRecipes();
                        requireActivity().setResult(RESULT_OK);
                    }
                });
        currentCategory = getArguments().getString("category", "All");

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RecipeAdapter(filteredList, this);
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setAdapter(new ShimmerAdapter());

        loadRecipes();
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadRecipes();
        });

        return binding.getRoot();
    }


    private void applyFilter(String query, String category) {
        filteredList.clear();

        String queryLower = query.toLowerCase();
        String categoryLower = category.toLowerCase();

        for (RecipeModel recipe : fullList) {
            String titleLower = recipe.getTitle().toLowerCase();
            String recipeCategoryLower = recipe.getCategory().toLowerCase();

            boolean matchesTitle = titleLower.contains(queryLower);
            boolean matchesRecipeCategory = recipeCategoryLower.contains(queryLower);
            boolean matchesTabCategory = category.equalsIgnoreCase("All") || recipeCategoryLower.equals(categoryLower);

            if (category.equalsIgnoreCase("All")) {
                if (matchesTitle || matchesRecipeCategory) {
                    filteredList.add(recipe);
                }
            } else {
                if (matchesTabCategory && matchesTitle) {
                    filteredList.add(recipe);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    @Override
    public void onSearchRequested(String query, String categoryFromActivity) {
        applyFilter(query, categoryFromActivity);
    }

    @Override
    public void onRecipeClick(RecipeModel recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailsActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        launcher.launch(intent);
    }

    private void loadRecipes() {
        binding.swipeRefresh.setRefreshing(true);
        binding.recyclerView.setAdapter(new ShimmerAdapter());

        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        RecipeModel recipe = doc.toObject(RecipeModel.class);
                        if (recipe != null) {
                            recipe.setId(doc.getId());
                            if (currentCategory.equals("All") || recipe.getCategory().equalsIgnoreCase(currentCategory)) {
                                fullList.add(recipe);
                            }
                        }
                    }
                    adapter = new RecipeAdapter(filteredList, this);
                    binding.recyclerView.setAdapter(adapter);

                    applyFilter("", currentCategory);

                    // إيقاف التحميل
                    binding.swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    // عند فشل التحميل أيضًا نوقف المؤشر
                    binding.swipeRefresh.setRefreshing(false);
                });


    }
}