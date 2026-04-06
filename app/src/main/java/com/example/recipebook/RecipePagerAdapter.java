package com.example.recipebook;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class RecipePagerAdapter extends FragmentStateAdapter {

     ArrayList<String> categories;

    public RecipePagerAdapter(@NonNull FragmentActivity fa, ArrayList<String> categories) {
        super(fa);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String category = categories.get(position);
        return RecipeFragment.newInstance(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
    public void updateCategories(List<String> newCats) {
        this.categories = new ArrayList<>(newCats);
        notifyDataSetChanged();
    }


}