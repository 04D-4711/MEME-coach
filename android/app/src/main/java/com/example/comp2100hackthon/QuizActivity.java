package com.example.comp2100hackthon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Random;
import java.util.concurrent.Executors;

import memecoach.MemeCoachManager;
import memecoach.QuizFeedback;
import memecoach.FeedbackCallback;
import memecoach.TemplateFeedbackService;
import memecoach.DemoExplanationProvider;
import attachments.Quiz;
import dao.model.User;

public class QuizActivity extends AppCompatActivity {

    private MemeCoachManager coachManager;
    private Spinner spinner;
    private DemoQuizCatalog.Question currentQuestion; // 存当前题目

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // ==========================================
        // 1. 获取真实题目并显示到 UI 上
        // ==========================================
        currentQuestion = DemoQuizCatalog.questionAt(0);

        TextView tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestion.setText(currentQuestion.text());

        RadioButton opt1 = findViewById(R.id.option1);
        RadioButton opt2 = findViewById(R.id.option2);
        RadioButton opt3 = findViewById(R.id.option3);
        RadioButton opt4 = findViewById(R.id.option4);
        String[] options = currentQuestion.options();
        opt1.setText(options[0]);
        opt2.setText(options[1]);
        opt3.setText(options[2]);
        opt4.setText(options[3]);

        // ==========================================
        // 2. 初始化下拉菜单
        // ==========================================
        spinner = findViewById(R.id.spinnerPersona);
        String[] personas = {"Supportive", "Penguin", "Teacher"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // ==========================================
        // 3. 初始化 Manager (注入真实解释)
        // ==========================================
        coachManager = new MemeCoachManager(
                new TemplateFeedbackService(new Random()),
                new DemoExplanationProvider(DemoQuizCatalog.explanations()), // 👈 真实解释
                Executors.newSingleThreadExecutor(),
                ContextCompat.getMainExecutor(this)
        );

        // ==========================================
        // 4. 按钮点击逻辑
        // ==========================================
        RadioGroup radioGroup = findViewById(R.id.radioGroupOptions);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = radioGroup.indexOfChild(findViewById(selectedId));
            String personaId = spinner.getSelectedItem().toString().toLowerCase();
            User currentUser = new User(java.util.UUID.randomUUID(), User.Role.Member, "Player1", "1234");

            // 用真实的题目构造 Quiz 对象
            Quiz realQuiz = new Quiz(currentQuestion.text(), currentQuestion.options(), currentQuestion.correctIndex());

            // 调用后端
            coachManager.submitAnswer(realQuiz, currentUser, selectedIndex, personaId, new FeedbackCallback() {
                @Override
                public void onCorrect() {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(QuizActivity.this, FeedbackActivity.class);
                        intent.putExtra("isCorrect", true);
                        intent.putExtra("headline", "Amazing job! You got it right!");
                        intent.putExtra("explanation", "You are truly a master of this topic.");
                        // 获取正确选项的文字
                        intent.putExtra("correctAnswer", currentQuestion.options()[currentQuestion.correctIndex()]);
                        intent.putExtra("imageKey", "supportive");
                        intent.putExtra("personaId", personaId);
                        startActivity(intent);
                    });
                }

                @Override
                public void onFeedback(QuizFeedback feedback) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(QuizActivity.this, FeedbackActivity.class);
                        intent.putExtra("isCorrect", false);
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