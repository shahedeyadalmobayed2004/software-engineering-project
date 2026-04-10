package com.example.recipebook;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.recipebook.databinding.ActivityRecipeDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ActivityRecipeDetailsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String recipeId;
    private String creatorId;
    private String videoUrlForSharing = "";

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

        loadRecipeDetails();

        binding.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditRecipeActivity.class);
            intent.putExtra("recipeId", recipeId);
            editLauncher.launch(intent);
        });

        binding.deleteButton.setOnClickListener(v -> {
            firestore.collection("recipes").document(recipeId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // تفعيل كود المشاركة ليشمل الصورة والتفاصيل بصيغة PDF
        // Implementing sharing logic to include both image and details as PDF
        binding.shareButton.setOnClickListener(v -> {
            generateAndOpenPDF();
        });

        // منطق زر المفضلة (تبديل الحالة)
        // Favorite button logic (simple toggle)
        binding.favoriteButton.setOnClickListener(v -> {
            boolean isFav = v.isSelected();
            v.setSelected(!isFav);
            if (!isFav) {
                Toast.makeText(this, "Added to Favorites (تمت الإضافة للمفضلة)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Removed from Favorites (تم الحذف من المفضلة)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeDetails() {
        firestore.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String videoUrl = doc.getString("videoUrl");
                        videoUrlForSharing = videoUrl != null ? videoUrl : "";
                        List<String> ingredients = (List<String>) doc.get("ingredients");
                        List<String> steps = (List<String>) doc.get("steps");
                        String imageUrl = doc.getString("imageUrl");
                        creatorId = doc.getString("userId");

                        binding.titleText.setText(title);
                        binding.categoryText.setText("Category: " + capitalize(category));
                        binding.videoLinkText.setOnClickListener(v -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                            startActivity(browserIntent);
                        });

                        if (ingredients != null && !ingredients.isEmpty()) {
                            StringBuilder formattedIngredients = new StringBuilder();
                            for (String item : ingredients) {
                                formattedIngredients.append("• ").append(item.trim()).append("\n");
                            }
                            binding.ingredientsText.setText(formattedIngredients.toString().trim());
                            binding.ingredientsText.post(() -> {
                                if (binding.ingredientsText.getLineCount() > 7) {
                                    binding.ingredientsText.setMovementMethod(new ScrollingMovementMethod());
                                    binding.ingredientsText.setVerticalScrollBarEnabled(true);
                                    binding.ingredientsText.setMaxLines(7);
                                    enableSmoothScroll(binding.ingredientsText);
                                } else {
                                    binding.ingredientsText.setMovementMethod(null);
                                    binding.ingredientsText.setVerticalScrollBarEnabled(false);
                                    binding.ingredientsText.setMaxLines(Integer.MAX_VALUE);
                                }
                            });
                        }

                        if (steps != null && !steps.isEmpty()) {
                            StringBuilder formattedSteps = new StringBuilder();
                            for (int i = 0; i < steps.size(); i++) {
                                formattedSteps.append((i + 1)).append(". ").append(steps.get(i).trim()).append("\n");
                            }
                            binding.stepsText.setText(formattedSteps.toString().trim());
                            binding.stepsText.post(() -> {
                                if (binding.stepsText.getLineCount() > 7) {
                                    binding.stepsText.setMovementMethod(new ScrollingMovementMethod());
                                    binding.stepsText.setVerticalScrollBarEnabled(true);
                                    binding.stepsText.setMaxLines(7);
                                    enableSmoothScroll(binding.stepsText);
                                } else {
                                    binding.stepsText.setMovementMethod(null);
                                    binding.stepsText.setVerticalScrollBarEnabled(false);
                                    binding.stepsText.setMaxLines(Integer.MAX_VALUE);
                                }
                            });
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(binding.recipeImage);
                        }

                        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                        if (currentUserId.equals(creatorId)) {
                            binding.editDeleteLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableSmoothScroll(TextView tv) {
        tv.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    /**
     * دالة مساعدة لاستخراج الصورة من ImageView وتحويلها إلى Uri للمشاركة
     * Helper method to extract bitmap from ImageView and convert it to a shareable Uri
     */
    private Uri getImageUri() {
        Drawable drawable = binding.recipeImage.getDrawable();
        if (!(drawable instanceof BitmapDrawable)) {
            return null;
        }
        
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        try {
            // حفظ الصورة في ملف مؤقت في الـ Cache
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "shared_recipe.png");
            
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            
            // استخدام FileProvider للحصول على رابط آمن للمشاركة
            return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * دالة مبسطة لإنشاء ملف PDF ومشاركته (طريقة أكثر استقراراً)
     * Simplified method to generate and share the PDF (More stable way)
     */
    private void generateAndOpenPDF() {
        PdfDocument pdfDocument = new PdfDocument();
        try {
            // إنشاء صفحة A4
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            TextPaint textPaint = new TextPaint();
            Paint paint = new Paint();

            int x = 50;
            int y = 50;
            int pageWidth = 500;

            // 1. رسم الصورة
            Drawable drawable = binding.recipeImage.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, pageWidth, 280, true);
                canvas.drawBitmap(scaledBitmap, x, y, paint);
                y += 320;
            }

            // 2. النصوص الأساسية
            textPaint.setColor(android.graphics.Color.BLACK);
            
            // العنوان
            textPaint.setTextSize(26);
            textPaint.setFakeBoldText(true);
            drawWrappedText(canvas, binding.titleText.getText().toString(), textPaint, pageWidth, x, y);
            y += 60;

            // التصنيف
            textPaint.setTextSize(18);
            textPaint.setFakeBoldText(false);
            canvas.drawText(binding.categoryText.getText().toString(), x, y, textPaint);
            y += 40;

            // المكونات والخطوات
            textPaint.setTextSize(20);
            textPaint.setFakeBoldText(true);
            canvas.drawText("Recipe Details:", x, y, textPaint);
            y += 40;

            textPaint.setTextSize(16);
            textPaint.setFakeBoldText(false);
            String allDetails = "Ingredients:\n" + binding.ingredientsText.getText().toString() + 
                               "\n\nSteps:\n" + binding.stepsText.getText().toString();
            drawWrappedText(canvas, allDetails, textPaint, pageWidth, x, y);

            pdfDocument.finishPage(page);

            // حفظ الملف في الكاش (Cache) لمشاركته
            File pdfFile = new File(getCacheDir(), "Recipe_Share.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            // استخدام طريقة المشاركة (ACTION_SEND) لأنها أسهل وأكثر أماناً
            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "وصفة: " + binding.titleText.getText().toString());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "مشاركة وصفة PDF عبر:"));

        } catch (Exception e) {
            if (pdfDocument != null) pdfDocument.close();
            e.printStackTrace();
            Toast.makeText(this, "حدث خطأ أثناء إنشاء الملف: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * دالة مساعدة لرسم النص مع التفاف تلقائي (Text Wrapping)
     */
    private void drawWrappedText(Canvas canvas, String text, TextPaint paint, int width, int x, int y) {
        StaticLayout staticLayout = StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build();
        canvas.save();
        canvas.translate(x, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }
}