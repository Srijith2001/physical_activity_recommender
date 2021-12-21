package com.recommender.physicalActivity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserDetails extends AppCompatActivity {

    AppCompatButton detailsSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        detailsSubmit = findViewById(R.id.userDetailsbtn);

        detailsSubmit.setOnClickListener(view -> {
            String height = ((EditText)findViewById(R.id.height)).getText().toString();
            String weight = ((EditText)findViewById(R.id.weight)).getText().toString();
            String gender = ((EditText)findViewById(R.id.gender)).getText().toString();
            String goal = ((EditText)findViewById(R.id.dailyGoal)).getText().toString();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> user = new HashMap<>();
            user.put("height", height);
            user.put("weight", weight);
            user.put("gender", gender);
            user.put("goal", goal);

            // Add a new document with a generated ID
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            Intent intent = new Intent(UserDetails.this, RecommenderActivity.class);
                            intent.putExtra("height", height);
                            intent.putExtra("weight", weight);
                            intent.putExtra("gender", gender);
                            intent.putExtra("goal",goal);
                            intent.putExtra("id", documentReference.getId());
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        });
    }
}