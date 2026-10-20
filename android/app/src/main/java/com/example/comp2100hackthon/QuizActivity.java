package com.example.comp2100hackthon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定刚才画的答题界面
        setContentView(R.layout.activity_quiz);

        Button btnSubmit = findViewById(R.id.btnSubmit);

        // 点击提交按钮，跳转到反馈卡页面
        btnSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }
}
