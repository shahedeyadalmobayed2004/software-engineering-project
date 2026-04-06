package com.example.recipebook;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.recipebook.databinding.ActivityHomeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    ArrayList<String> tabs;
    RecipePagerAdapter adapter;
    private TabLayoutMediator mediator;
    private ConnectivityManager.NetworkCallback networkCallback;

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

        if (binding.searchView != null) {
            binding.searchView.setQueryHint("Search recipes...");
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setSupportActionBar(binding.toolbar);

        checkNetworkStatus();
        loadCategoriesFromFirestore();

        // 1. برمجة زر الفلتر (شغل رهف)
        binding.filterBtn.setOnClickListener(v -> {
            showFilterBottomSheet();
        });

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

        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    addIngredientChip(query.trim());
                    performSearch(query);
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
    }

    // 2. دالة إظهار الفلتر المتقدم وقراءة القيم
    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        MaterialButton applyBtn = sheetView.findViewById(R.id.applyFilterBtn);
        Slider caloriesSlider = sheetView.findViewById(R.id.caloriesSlider);
        ChipGroup timeGroup = sheetView.findViewById(R.id.timeChipGroup);

        applyBtn.setOnClickListener(v -> {
            // قراءة السعرات
            int maxCalories = (int) caloriesSlider.getValue();

            // قراءة الوقت المختار من الـ Chips
            int maxTime = 0;
            int checkedId = timeGroup.getCheckedChipId();
            if (checkedId == R.id.chip15) maxTime = 15;
            else if (checkedId == R.id.chip30) maxTime = 30;
            else if (checkedId == R.id.chip60) maxTime = 60;

            // إرسال القيم للجسر للفلترة الفعلية
            applyAdvancedFilters(maxCalories, maxTime);

            Toast.makeText(this, "Filtering: " + maxCalories + " kcal, " + maxTime + " min", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    // 3. الجسر الذي يربط الـ UI مع الـ Fragment (شغل شهد وسارة)
    private void applyAdvancedFilters(int calories, int time) {
        if (tabs == null || adapter == null) return;

        int currentTabPosition = binding.tabLayout.getSelectedTabPosition();
        String currentCategory = tabs.get(currentTabPosition);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + adapter.getItemId(currentTabPosition));

        if (fragment instanceof RecipeFragment) {
            // ملاحظة: شهد يجب أن تنشئ هذه الدالة داخل RecipeFragment
            ((RecipeFragment) fragment).onAdvancedFilterRequested(calories, time);
        }
    }

    private void addIngredientChip(String text) {
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(R.color.primarycolor);
        int whiteColor = androidx.core.content.ContextCompat.getColor(this, android.R.color.white);
        chip.setTextColor(whiteColor);
        chip.setCloseIconTintResource(android.R.color.white);
        chip.setElevation(6f);
        chip.setOnCloseIconClickListener(v -> {
            binding.ingredientsChipGroup.removeView(chip);
        });
        binding.ingredientsChipGroup.addView(chip);
    }

    private void checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().build();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    binding.connectivityStatusIcon.setColorFilter(Color.GREEN);
                    Toast.makeText(HomeActivity.this, "Back Online", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    binding.connectivityStatusIcon.setColorFilter(Color.RED);
                    Toast.makeText(HomeActivity.this, "You are now offline", Toast.LENGTH_SHORT).show();
                });
            }
        };
        cm.registerNetworkCallback(networkRequest, networkCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            cm.unregisterNetworkCallback(networkCallback);
        }
    }

    private void loadCategoriesFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("recipes")
                .get(Source.SERVER)
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
}