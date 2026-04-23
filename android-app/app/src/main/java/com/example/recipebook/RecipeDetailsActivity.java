package com.example.recipebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipebook.databinding.ActivityRecipeDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private String creatorId;
    private String videoUrlForSharing = "";
    private RecipeModel currentRecipe;
    private List<String> ingredientsList;

    private List<CommentModel> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private ListenerRegistration commentsListener;
    private ListenerRegistration likesListener;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadRecipeDetails();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recipeId = getIntent().getStringExtra("recipeId");
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        // فحص حالة المفضلات (شغل الفريق)
        FirebaseHelper.checkIsFavorite(recipeId, binding.favoriteBtn);

        loadRecipeDetails();
        setupCommentsRecyclerView();
        setupLikes();
        loadComments();

        // تنصت الأزرار
        binding.sendCommentButton.setOnClickListener(v -> addComment());
        
        binding.favoriteBtn.setOnClickListener(v -> {
            if (currentRecipe != null) {
                FirebaseHelper.toggleFavorite(this, currentRecipe, binding.favoriteBtn);
            }
        });

        binding.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditRecipeActivity.class);
            intent.putExtra("recipeId", recipeId);
            editLauncher.launch(intent);
        });

        binding.btnShoppingList.setOnClickListener(v -> {
            if (ingredientsList == null || ingredientsList.isEmpty()) {
                Toast.makeText(this, "No ingredients found", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ShoppingListActivity.class);
            intent.putStringArrayListExtra("ingredients", new ArrayList<>(ingredientsList));
            startActivity(intent);
        });

        binding.shareButton.setOnClickListener(v -> showShareDialog());
        binding.likeButton.setOnClickListener(v -> toggleLike());
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
        firestore.collection("recipes").document(recipeId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentRecipe = doc.toObject(RecipeModel.class);
                        if (currentRecipe != null) currentRecipe.setId(doc.getId());

                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        videoUrlForSharing = doc.getString("videoUrl");
                        ingredientsList = (List<String>) doc.get("ingredients");
                        List<String> steps = (List<String>) doc.get("steps");
                        String imageUrl = doc.getString("imageUrl");
                        creatorId = doc.getString("userId");

                        binding.titleText.setText(title);
                        binding.categoryText.setText("Category: " + capitalize(category));

                        if (imageUrl != null) Picasso.get().load(imageUrl).into(binding.recipeImage);

                        // تنسيق المكونات والخطوات (شغل الفريق)
                        if (ingredientsList != null) {
                            StringBuilder sb = new StringBuilder();
                            for (String item : ingredientsList) sb.append("• ").append(item).append("\n");
                            binding.ingredientsText.setText(sb.toString().trim());
                            setupTextScrolling(binding.ingredientsText);
                        }

                        if (steps != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < steps.size(); i++) sb.append((i + 1)).append(". ").append(steps.get(i)).append("\n");
                            binding.stepsText.setText(sb.toString().trim());
                            setupTextScrolling(binding.stepsText);
                        }
                    }
                });
    }

    private void setupTextScrolling(TextView tv) {
        tv.post(() -> {
            if (tv.getLineCount() > 7) {
                tv.setMovementMethod(new ScrollingMovementMethod());
                tv.setVerticalScrollBarEnabled(true);
                tv.setMaxLines(7);
                enableSmoothScroll(tv);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableSmoothScroll(TextView tv) {
        tv.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getAction() == MotionEvent.ACTION_UP) v.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private void showShareDialog() {
        String[] options = {"Share as Text & Image", "Share as PDF"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Share Method")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) shareAsTextAndImage();
                    else generateAndOpenPDF();
                }).show();
    }

    private void shareAsTextAndImage() {
        String title = binding.titleText.getText().toString();
        StringBuilder shareBody = new StringBuilder("*").append(title).append("*\n");
        shareBody.append("🍳 Ingredients:\n").append(binding.ingredientsText.getText()).append("\n");
        if (videoUrlForSharing != null && !videoUrlForSharing.isEmpty()) shareBody.append("🎥 Video: ").append(videoUrlForSharing);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody.toString());
        startActivity(Intent.createChooser(shareIntent, "Share via:"));
    }

    private void generateAndOpenPDF() {
        PdfDocument pdfDocument = new PdfDocument();
        try {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(20);
            canvas.drawText(binding.titleText.getText().toString(), 50, 50, textPaint);
            pdfDocument.finishPage(page);

            File sharedFolder = new File(getCacheDir(), "shared");
            if (!sharedFolder.exists()) sharedFolder.mkdirs();
            File pdfFile = new File(sharedFolder, "Recipe.pdf");
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            pdfDocument.close();

            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share PDF:"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) commentsListener.remove();
        if (likesListener != null) likesListener.remove();
    }
}