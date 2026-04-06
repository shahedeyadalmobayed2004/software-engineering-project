package com.example.recipebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeModel> list;
    private RecipeBookListener listener;

    public RecipeAdapter(List<RecipeModel> list, RecipeBookListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {

        RecipeModel recipe = list.get(position);

        holder.title.setText(recipe.getTitle());
        holder.category.setText(recipe.getCategory());

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView title, category;
        ImageView image;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.recipeTitle);
            category = itemView.findViewById(R.id.recipeCategory);
            image = itemView.findViewById(R.id.recipeImage);
        }
    }
}