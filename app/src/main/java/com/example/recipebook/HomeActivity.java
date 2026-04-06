package com.example.recipebook;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.recipebook.databinding.ActivityHomeBinding;
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

        if (binding.searchView != null)
            binding.searchView.setQueryHint("Search recipes...");

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setSupportActionBar(binding.toolbar);

        // تشغيل مراقبة الشبكة
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
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    // اللون الأخضر والرسالة بالإنجليزية عند توفر النت
                    binding.connectivityStatusIcon.setColorFilter(Color.GREEN);
                    Toast.makeText(HomeActivity.this, "Back Online", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    // اللون الأحمر والرسالة بالإنجليزية عند انقطاع النت
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
}