package com.example.recipebook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ShoppingListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> ingredients;
    private SharedPreferences prefs;

    private static final String PREF_NAME = "shopping_prefs";

    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        listView = findViewById(R.id.shoppingListView);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        ingredients = getIntent().getStringArrayListExtra("ingredients");
        recipeId = getIntent().getStringExtra("recipeId");

        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }

        if (recipeId == null) {
            recipeId = "default";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                ingredients
        );

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadCheckedItems();

        listView.setOnItemClickListener((parent, view, position, id) -> saveCheckedItems());
    }

    private void saveCheckedItems() {
        Set<String> checkedSet = new HashSet<>();

        for (int i = 0; i < ingredients.size(); i++) {
            if (listView.isItemChecked(i)) {
                checkedSet.add(ingredients.get(i));
            }
        }

        prefs.edit()
                .putStringSet("checked_items_" + recipeId, checkedSet)
                .apply();
    }

    private void loadCheckedItems() {
        Set<String> checkedSet = prefs.getStringSet("checked_items_" + recipeId, new HashSet<>());

        for (int i = 0; i < ingredients.size(); i++) {
            if (checkedSet.contains(ingredients.get(i))) {
                listView.setItemChecked(i, true);
            }
        }
    }
}
