package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.recipebook.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private Uri imageUri;
    private String imageUrl;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
        }

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        binding.profileImage.setImageURI(imageUri);

                        MediaManager.get().upload(imageUri)
                                .option("folder", "users")
                                .option("public_id", "user_" + System.currentTimeMillis())
                                .callback(new UploadCallback() {
                                    @Override
                                    public void onSuccess(String requestId, Map resultData) {
                                        imageUrl = (String) resultData.get("secure_url");
                                        Toast.makeText(EditProfileActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onStart(String requestId) {
                                    }

                                    @Override
                                    public void onProgress(String requestId, long bytes, long totalBytes) {
                                    }

                                    @Override
                                    public void onError(String requestId, ErrorInfo error) {
                                    }

                                    @Override
                                    public void onReschedule(String requestId, ErrorInfo error) {
                                    }
                                }).dispatch();


                    }
                });

        String emailUser = auth.getCurrentUser().getEmail();

        firestore.collection("users")
                .whereEqualTo("email", emailUser)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        binding.nameEditText.setText(doc.getString("name"));

                        imageUrl = doc.getString("profileImage");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(binding.profileImage);
                        }
                    }
                });
        binding.changeImageBtn.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(pickIntent);
        });

        binding.saveBtn.setOnClickListener(v -> {
            String name = binding.nameEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please fill name", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = auth.getCurrentUser().getUid();

            firestore.collection("users")
                    .whereEqualTo("email", auth.getCurrentUser().getEmail())
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            DocumentSnapshot doc = query.getDocuments().get(0);
                            String docId = doc.getId();

                            Map<String, Object> updated = new HashMap<>();
                            updated.put("name", name);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                updated.put("profileImage", imageUrl);
                            }

                            firestore.collection("users").document(docId)
                                    .update(updated)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                        finish();
                                    });
                        }
                    });
        });
    }


}
