package com.example.recipebook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeProfileAdapter extends RecyclerView.Adapter<RecipeProfileAdapter.ViewHolder> {

    private List<RecipeModel> list;

    public RecipeProfileAdapter(List<RecipeModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeModel recipe = list.get(position);
        holder.title.setText(recipe.getTitle());
        holder.category.setText(recipe.getCategory());
        Picasso.get().load(recipe.getImageUrl()).into(holder.image);

        holder.editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditRecipeActivity.class);
            intent.putExtra("recipeId", recipe.getId());
            v.getContext().startActivity(intent);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("recipes")
                    .document(recipe.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        list.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category;
        ImageView image;
        ImageButton editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recipeTitle);
            category = itemView.findViewById(R.id.recipeCategory);
            image = itemView.findViewById(R.id.recipeImage);
            editBtn = itemView.findViewById(R.id.editButton);
            deleteBtn = itemView.findViewById(R.id.deleteButton);
        }
    }
}