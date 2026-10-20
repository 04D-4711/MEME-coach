package com.example.comp2100hackthon;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// 引入真实的 QuizFeedback 类
import memecoach.QuizFeedback;

public class FeedbackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // 1. 找到控件
        ImageView imgMeme = findViewById(R.id.imgMeme);
        TextView tvQuote = findViewById(R.id.tvQuote);
        TextView tvCorrectAnswer = findViewById(R.id.tvCorrectAnswer);
        TextView tvExplanation = findViewById(R.id.tvExplanation);
        Button btnShareToPost = findViewById(R.id.btnShareToPost);
        Button btnBackToQuiz = findViewById(R.id.btnBackToQuiz);

        // 2. 接收从 QuizActivity 传过来的数据
        // 注意：这里我们假设你在 QuizActivity 里传的是 QuizFeedback 对象
        // (如果 Intent 传对象报错，我们需要让 QuizFeedback 实现 Serializable，或者直接传 String)
        // 为了最稳妥，我直接用 String 传值。

        String headline = getIntent().getStringExtra("headline");
        String explanation = getIntent().getStringExtra("explanation");
        String correctAnswer = getIntent().getStringExtra("correctAnswer");
        String imageKey = getIntent().getStringExtra("imageKey");

        // 3. 填装数据
        tvQuote.setText(headline != null ? headline : "Keep going!");
        tvExplanation.setText(explanation != null ? explanation : "Explanation missing.");
        tvCorrectAnswer.setText("Correct Answer: " + (correctAnswer != null ? correctAnswer : "Unknown"));

        // 4. 根据 imageKey 切换图片
        if (imageKey != null && imageKey.toLowerCase().contains("penguin")) {
            imgMeme.setImageResource(R.drawable.meme_penguin);
        } else if (imageKey != null && imageKey.toLowerCase().contains("teacher")) {
            imgMeme.setImageResource(R.drawable.meme_teacher);
        } else {
            imgMeme.setImageResource(R.drawable.meme_supportive);
        }

        // 5. 按钮逻辑
        btnBackToQuiz.setOnClickListener(v -> finish());

        btnShareToPost.setOnClickListener(v -> {
            // 这里等你问刘要了保存帖子的方法后，直接调。
            // 现在先弹个提示框假装发帖成功。
            Toast.makeText(this, "Feedback saved to post! (Mock)", Toast.LENGTH_SHORT).show();
        });
    }
}