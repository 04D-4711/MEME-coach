package com.example.comp2100hackthon;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定刚才画的反馈卡界面
        setContentView(R.layout.activity_feedback);

        Button btnBack = findViewById(R.id.btnBackToQuiz);
        // 点击 Continue 按钮，返回上一页
        btnBack.setOnClickListener(v -> finish());
    }
}