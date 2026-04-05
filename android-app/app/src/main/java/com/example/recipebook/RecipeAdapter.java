package com.example.recipebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

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

        Picasso.get().load(recipe.getImageUrl())
                .into(holder.image);

        FirebaseHelper.checkIsFavorite(recipe.getId(), holder.favoriteBtn);

        holder.favoriteBtn.setOnClickListener(v -> {
            FirebaseHelper.toggleFavorite(holder.itemView.getContext(), recipe, holder.favoriteBtn);
        });

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
        ImageButton favoriteBtn;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recipeTitle);
            category = itemView.findViewById(R.id.recipeCategory);
            image = itemView.findViewById(R.id.recipeImage);
            favoriteBtn = itemView.findViewById(R.id.favoriteBtn);
        }
    }
}
