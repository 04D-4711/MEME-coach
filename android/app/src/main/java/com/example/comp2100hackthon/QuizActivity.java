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
    private DemoQuizCatalog.Question currentQuestion;
    private int currentQuizIndex = new java.util.Random().nextInt(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 初始化下拉菜单
        spinner = findViewById(R.id.spinnerPersona);
        String[] personas = {"Supportive", "Penguin", "Teacher"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 初始化 Manager (注入真实解释)
        coachManager = new MemeCoachManager(
                new TemplateFeedbackService(new Random()),
                new DemoExplanationProvider(DemoQuizCatalog.explanations()),
                Executors.newSingleThreadExecutor(),
                ContextCompat.getMainExecutor(this)
        );

        // 加载第一道题
        loadQuiz(currentQuizIndex);

        RadioGroup radioGroup = findViewById(R.id.radioGroupOptions);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnNextQuiz = findViewById(R.id.btnNextQuiz);

        // 换题按钮
        btnNextQuiz.setOnClickListener(v -> {
            currentQuizIndex++;
            loadQuiz(currentQuizIndex);
        });

        // 提交按钮
        btnSubmit.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = radioGroup.indexOfChild(findViewById(selectedId));
            String personaId = spinner.getSelectedItem().toString().toLowerCase();
            User currentUser = new User(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"), User.Role.Member, "Player1", "1234");

            // 构造真实的 Quiz
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
                        intent.putExtra("correctAnswer", currentQuestion.options()[currentQuestion.correctIndex()]);
                        intent.putExtra("imageKey", "supportive");
                        intent.putExtra("personaId", personaId);
                        intent.putExtra("userAnswer", currentQuestion.options()[selectedIndex]); // 真实答案
                        intent.putExtra("questionText", currentQuestion.text());
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
                        intent.putExtra("userAnswer", currentQuestion.options()[selectedIndex]); // 真实答案
                        intent.putExtra("questionText", currentQuestion.text());
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

    // 动态加载题目
    private void loadQuiz(int index) {
        // 使用 % count() 实现三道题循环
        currentQuestion = DemoQuizCatalog.questionAt(index % DemoQuizCatalog.count());

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

        // 清空选中状态
        RadioGroup radioGroup = findViewById(R.id.radioGroupOptions);
        radioGroup.clearCheck();
    }
}