package com.example.recipebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipebook.databinding.ActivityRecipeDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ActivityRecipeDetailsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String recipeId;
    private String currentUserId;

    private List<CommentModel> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private ListenerRegistration commentsListener;
    private ListenerRegistration likesListener;
    private List<String> ingredientsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recipeId = getIntent().getStringExtra("recipeId");
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        loadRecipeDetails();
        setupCommentsRecyclerView();
        setupLikes();
        loadComments();

        binding.sendCommentButton.setOnClickListener(v -> addComment());
    }
    private void setupLikes() {
        DocumentReference recipeRef = firestore.collection("recipes").document(recipeId);

        likesListener = recipeRef.addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists()) {

                List<String> likes = (List<String>) doc.get("likes");
                if (likes == null) likes = new ArrayList<>();

                binding.likeCount.setText(likes.size() + " likes");

                if (likes.contains(currentUserId)) {
                    binding.likeButton.setText("Unlike");
                    binding.likeButton.setIconResource(R.drawable.ic_favorite_filled);
                } else {
                    binding.likeButton.setText("Like");
                    binding.likeButton.setIconResource(R.drawable.ic_favorite_border);
                }
            }
        });

        binding.likeButton.setOnClickListener(v -> toggleLike());
    }

    private void toggleLike() {
        DocumentReference recipeRef = firestore.collection("recipes").document(recipeId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(recipeRef);
            List<String> likes = (List<String>) snapshot.get("likes");

            if (likes == null) likes = new ArrayList<>();

            if (likes.contains(currentUserId)) {
                transaction.update(recipeRef, "likes", FieldValue.arrayRemove(currentUserId));
            } else {
                transaction.update(recipeRef, "likes", FieldValue.arrayUnion(currentUserId));
            }

            return null;
        });
    }

    private void setupCommentsRecyclerView() {
        commentAdapter = new CommentAdapter(commentList, currentUserId, this::deleteComment);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void loadComments() {
        commentsListener = firestore.collection("recipes").document(recipeId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snapshots, e) -> {

                    if (snapshots != null) {
                        commentList.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            CommentModel comment = doc.toObject(CommentModel.class);
                            if (comment != null) {
                                comment.setCommentId(doc.getId());
                                commentList.add(comment);
                            }
                        }

                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void addComment() {
        String text = binding.commentInput.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Write a comment first", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {

                    String userName = doc.getString("name");
                    if (userName == null) userName = "User";

                    Map<String, Object> comment = new HashMap<>();
                    comment.put("userId", currentUserId);
                    comment.put("userName", userName);
                    comment.put("text", text);
                    comment.put("timestamp", System.currentTimeMillis());

                    firestore.collection("recipes").document(recipeId)
                            .collection("comments")
                            .add(comment)
                            .addOnSuccessListener(ref -> {
                                binding.commentInput.setText("");
                                Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void deleteComment(CommentModel comment) {
        firestore.collection("recipes").document(recipeId)
                .collection("comments")
                .document(comment.getCommentId())
                .delete();
    }

    private void loadRecipeDetails() {

        firestore.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        binding.titleText.setText(doc.getString("title"));
                        binding.categoryText.setText(doc.getString("category"));
                        ingredientsList = (List<String>) doc.get("ingredients");
                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl != null) {
                            Picasso.get().load(imageUrl).into(binding.recipeImage);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) commentsListener.remove();
        if (likesListener != null) likesListener.remove();
    }
}