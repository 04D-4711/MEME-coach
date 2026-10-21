package com.example.comp2100hackthon;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import dao.UserDAO;
import dao.model.User;
import memecoach.QuizFeedback;
import java.util.Random;
import java.util.HashMap;
import java.util.concurrent.Executors;
import androidx.core.content.ContextCompat;
import memecoach.MemeCoachManager;
import memecoach.TemplateFeedbackService;
import memecoach.DemoExplanationProvider;

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

        String tempPersonaId = getIntent().getStringExtra("personaId");
// 新建一个 final 变量，专门给下面用
        final String finalPersonaId = (tempPersonaId != null) ? tempPersonaId : "supportive";
        boolean isCorrect = getIntent().getBooleanExtra("isCorrect", false);

        // 3. 填装数据
        tvQuote.setText(headline != null ? headline : "Keep going!");
        tvExplanation.setText(explanation != null ? explanation : "Explanation missing.");

        // 直接在这里判断，不用在外面先 setText 一次
        if (isCorrect) {
            // 答对了：隐藏正确答案
            tvCorrectAnswer.setVisibility(android.view.View.GONE);
        } else {
            // 答错了：显示正确答案（加个判空保护）
            tvCorrectAnswer.setVisibility(android.view.View.VISIBLE);
            tvCorrectAnswer.setText("Correct Answer: " + (correctAnswer != null ? correctAnswer : "Unknown"));
        }

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
            // 1. 拿 User
            // 替换成这个
            User currentUser = new User(java.util.UUID.randomUUID(), User.Role.Member, "Player1", "1234");

            // 把这里的 personaId 换成 finalPersonaId
            QuizFeedback feedback = new QuizFeedback(
                    finalPersonaId, finalPersonaId + " Persona",
                    headline, explanation, imageKey,
                    "What is the capital of Australia?", "Sydney", correctAnswer
            );

            // 3. 初始化 Manager
            MemeCoachManager manager = new MemeCoachManager(
                    new TemplateFeedbackService(new Random()),
                    new DemoExplanationProvider(new HashMap<>()),
                    Executors.newSingleThreadExecutor(),
                    ContextCompat.getMainExecutor(this)
            );

            // 4. 执行分享（存入 PostDAO）
            manager.shareFeedback(currentUser, feedback);

            // 5. 🔥 触发持久化：把内存里的数据存到手机文件里
            try {
                // 如果这里报错标红，按 Option + Enter 导入 DataManager
                // 刘同学的 DataManager 通常长这样：
                persistentdata.DataManager.getInstance(getApplicationContext()).writeAll();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 6. 提示成功
            android.widget.Toast.makeText(this, "Shared to Post successfully!", android.widget.Toast.LENGTH_SHORT).show();

            // 7. 🔥 跳转回主界面，并清空中间的所有页面栈
            android.content.Intent intent = new android.content.Intent(FeedbackActivity.this, MainActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // 关闭当前的反馈页
        });
    }
}