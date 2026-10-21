package com.example.comp2100hackthon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;

import dao.model.User;
import memecoach.MemeCoachManager;
import memecoach.QuizFeedback;
import memecoach.FeedbackCallback;
import memecoach.TemplateFeedbackService;
import memecoach.DemoExplanationProvider;

// 如果这里报错找不到 Quiz 和 User，按 Option+Enter 自动导入一下
import attachments.Quiz;
import dao.UserDAO;

public class QuizActivity extends AppCompatActivity {

    private MemeCoachManager coachManager;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 1. 初始化下拉菜单
        spinner = findViewById(R.id.spinnerPersona);
        String[] personas = {"Supportive", "Penguin", "Teacher"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 2. 初始化 MemeCoachManager (修正了参数)
        coachManager = new MemeCoachManager(
                new TemplateFeedbackService(new Random()),
                new DemoExplanationProvider(new HashMap<>()),
                Executors.newSingleThreadExecutor(),
                ContextCompat.getMainExecutor(this)
        );

        RadioGroup radioGroup = findViewById(R.id.radioGroupOptions);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            // 1. 拿到用户选的下拉框和选项
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedIndex = radioGroup.indexOfChild(findViewById(selectedId));
            String personaId = spinner.getSelectedItem().toString().toLowerCase();

            // 2. 获取真实的 User (先尝试登录，登录失败就注册一个)
            User currentUser = UserDAO.getInstance().login("TestUser123", "password123");
            if (currentUser == null) {
                currentUser = UserDAO.getInstance().register("TestUser123", "password123");
            }

            if (currentUser == null) {
                Toast.makeText(this, "Failed to get User", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. 获取 Quiz (手写一道真题目)
            String questionText = "What is the capital of Australia?";
            String[] options = {"Sydney", "Canberra", "Melbourne", "Perth"};
            Quiz currentQuiz = new Quiz(questionText, options, 1); // 1 是正确答案的索引

            // 4. 调用后端
            final User finalUser = currentUser;
            coachManager.submitAnswer(currentQuiz, finalUser, selectedIndex, personaId, new FeedbackCallback() {
                @Override
                public void onCorrect() {
                    runOnUiThread(() -> {
                        // 答对也跳转到反馈界面！
                        Intent intent = new Intent(QuizActivity.this, FeedbackActivity.class);
                        intent.putExtra("isCorrect", true); // 加个标记告诉下个页面是答对的
                        intent.putExtra("headline", "Amazing job! You got it right!");
                        intent.putExtra("explanation", "You are truly a master of this topic.");
                        intent.putExtra("correctAnswer", currentQuiz.getOption(currentQuiz.getCorrectAnswer()));
                        intent.putExtra("imageKey", "supportive"); // 答对给个鼓励的图
                        intent.putExtra("personaId", personaId);
                        startActivity(intent);
                    });
                }

                @Override
                public void onFeedback(QuizFeedback feedback) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(QuizActivity.this, FeedbackActivity.class);
                        intent.putExtra("headline", feedback.headline());
                        intent.putExtra("explanation", feedback.explanation());
                        intent.putExtra("correctAnswer", feedback.correctAnswer());
                        intent.putExtra("imageKey", feedback.imageKey());
                        intent.putExtra("personaId", personaId);
                        startActivity(intent);
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(QuizActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}