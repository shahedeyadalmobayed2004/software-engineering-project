package com.example.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;

import com.example.recipebook.databinding.ActivityHomeBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import com.google.firebase.firestore.Source;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.view.View;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import androidx.core.content.ContextCompat;
import android.net.ConnectivityManager;
import android.graphics.Color;
import android.net.Network;
import android.net.NetworkRequest;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    ArrayList<String> tabs;
    RecipePagerAdapter adapter;
    private TabLayoutMediator mediator;
    private ConnectivityManager.NetworkCallback networkCallback;
    private List<String> selectedIngredients = new ArrayList<>();
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadCategoriesFromFirestore();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (binding.searchView != null)
            binding.searchView.setQueryHint("Search recipes...");


        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setSupportActionBar(binding.toolbar);

        checkNetworkStatus();

        loadCategoriesFromFirestore();

        binding.addRecipeFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            launcher.launch(intent);
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    addIngredientChip(query.trim());
                    performSmartSearch();
                    binding.searchView.setQuery("", false);
                    binding.searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        binding.filterBtn.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void loadCategoriesFromFirestore() {

        FirebaseFirestore.getInstance()
                .collection("recipes")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    Set<String> unique = new HashSet<>();
                    unique.add("All");
                    for (DocumentSnapshot doc : querySnapshot) {
                        String cat = doc.getString("category");
                        if (cat != null && !cat.trim().isEmpty())
                            unique.add(capitalize(cat.trim().toLowerCase(Locale.ROOT)));
                    }
                    List<String> newTabs = new ArrayList<>(unique);
                    Collections.sort(newTabs);

                    // 2) إذا كانت هذه أول مرة
                    if (adapter == null) {
                        adapter = new RecipePagerAdapter(this, (ArrayList<String>) newTabs);
                        binding.viewPager.setAdapter(adapter);
                        mediator = new TabLayoutMediator(
                                binding.tabLayout, binding.viewPager,
                                (tab, pos) -> tab.setText(newTabs.get(pos)));
                        mediator.attach();
                        tabs = new ArrayList<>(newTabs);
                        return;
                    }

                    if (!newTabs.equals(tabs)) {

                        if (mediator != null) mediator.detach();

                        tabs.clear();
                        tabs.addAll(newTabs);
                        adapter.updateCategories(tabs);

                        mediator = new TabLayoutMediator(
                                binding.tabLayout, binding.viewPager,
                                (tab, pos) -> tab.setText(tabs.get(pos)));
                        mediator.attach();
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("HomeActivity", "Failed to load categories: " + e.getMessage()));
    }



    private void performSearch(String query) {
        if (tabs == null || adapter == null) return;

        int currentTabPosition = binding.tabLayout.getSelectedTabPosition();
        String currentCategory = tabs.get(currentTabPosition);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + adapter.getItemId(currentTabPosition));
        if (fragment instanceof RecipeFragment) {
            ((RecipeFragment) fragment).onSearchRequested(query, currentCategory);
        } else {
            Log.w("HomeActivity", "Fragment at position " + currentTabPosition + " is not a RecipeFragment or is null.");
        }
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        MaterialButton applyBtn = sheetView.findViewById(R.id.applyFilterBtn);
        Slider caloriesSlider = sheetView.findViewById(R.id.caloriesSlider);
        ChipGroup timeGroup = sheetView.findViewById(R.id.timeChipGroup);

        applyBtn.setOnClickListener(v -> {
            int maxCalories = (int) caloriesSlider.getValue();
            int maxTime = 0;
            int checkedId = timeGroup.getCheckedChipId();
            if (checkedId == R.id.chip15) maxTime = 15;
            else if (checkedId == R.id.chip30) maxTime = 30;
            else if (checkedId == R.id.chip60) maxTime = 60;

            applyAdvancedFilters(maxCalories, maxTime);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void applyAdvancedFilters(int calories, int time) {
        if (tabs == null || adapter == null) return;
        int currentTabPosition = binding.tabLayout.getSelectedTabPosition();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + adapter.getItemId(currentTabPosition));
        if (fragment instanceof RecipeFragment) {
            ((RecipeFragment) fragment).onAdvancedFilterRequested(calories, time);
        }
    }

    private void addIngredientChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(R.color.primarycolor);
        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        chip.setCloseIconTintResource(android.R.color.white);
        chip.setElevation(6f);
        
        chip.setOnCloseIconClickListener(v -> {
            binding.ingredientsChipGroup.removeView(chip);
            selectedIngredients.remove(text);
            performSmartSearch();
        });

        binding.ingredientsChipGroup.addView(chip);
        selectedIngredients.add(text);
    }

    private void performSmartSearch() {
        if (tabs == null || adapter == null) return;
        int currentTabPosition = binding.tabLayout.getSelectedTabPosition();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + adapter.getItemId(currentTabPosition));
        if (fragment instanceof RecipeFragment) {
            ((RecipeFragment) fragment).performSmartSearch(selectedIngredients);
        }
    }

    private void checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> binding.connectivityStatusIcon.setColorFilter(Color.GREEN));
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> binding.connectivityStatusIcon.setColorFilter(Color.RED));
            }
        };

        if (cm != null) {
            cm.registerNetworkCallback(networkRequest, networkCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm != null) {
                cm.unregisterNetworkCallback(networkCallback);
            }
        }
    }
}
