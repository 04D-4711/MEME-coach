package com.example.comp2100hackthon;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 启动后直接跳转到答题页
        Intent intent = new Intent(this, QuizActivity.class);
        startActivity(intent);
        finish(); // 关掉当前空白页
    }
}