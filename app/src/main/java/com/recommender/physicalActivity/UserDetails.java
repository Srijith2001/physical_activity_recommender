package com.recommender.physicalActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

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
            Intent intent = new Intent(getApplicationContext(), RecommenderActivity.class);
            intent.putExtra("height", height);
            intent.putExtra("weight", weight);
            intent.putExtra("gender", gender);
            intent.putExtra("goal",goal);
            startActivity(intent);
            finish();
        });
    }
}