package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.recipebook.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private Uri imageUri;
    private String imageUrl = "";

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Picasso.get()
                            .load(imageUri)
                            .transform(new CropCircleTransformation())
                            .into(binding.userIV);

                    MediaManager.get().upload(imageUri)
                            .option("folder", "users")
                            .option("public_id", "profile_" + System.currentTimeMillis())
                            .callback(new UploadCallback() {
                                @Override
                                public void onStart(String requestId) {
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    imageUrl = (String) resultData.get("secure_url");

                                    runOnUiThread(() -> {
                                        Toast.makeText(RegisterActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                                        Picasso.get()
                                                .load(imageUrl)
                                                .transform(new CropCircleTransformation())
                                                .into(binding.userIV);
                                    });
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    Log.e("CloudinaryUpload", "Upload error: " + error.getDescription());
                                    Toast.makeText(RegisterActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show(); // أضف رسالة فشل
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {
                                }
                            }).dispatch();
                }
            });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        String[] countries = Locale.getISOCountries();
        List<String> countryList = new ArrayList<>();
        for (String code : countries) {
            Locale locale = new Locale("", code);
            countryList.add(locale.getDisplayCountry());
        }
        Collections.sort(countryList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countryList);
        binding.countrySpinner.setAdapter(adapter);

        Picasso.get()
                .load(R.drawable.ic_person)
                .transform(new  CropCircleTransformation())
                .into(binding.userIV);

        binding.addPhotoFab.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(pickIntent);
        });

        binding.registerButton.setOnClickListener(v -> {
            String name = binding.nameEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String country = binding.countrySpinner.getText().toString();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Please fill all fields and upload image", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = auth.getCurrentUser().getUid();

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", name);
                                user.put("email", email);
                                user.put("country", country);
                                user.put("profileImage", imageUrl);

                                firestore.collection("users")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(RegisterActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error adding user document", e);
                                            Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show(); // عرض رسالة الخطأ
                                Log.e("Registration", "Registration failed", task.getException());
                            }
                        }
                    });
        });

        binding.goToLoginText.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}