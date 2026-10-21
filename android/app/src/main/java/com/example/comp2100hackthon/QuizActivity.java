
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

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;

import attachments.Quiz;
import dao.model.User;
import memecoach.DemoExplanationProvider;
import memecoach.FeedbackCallback;
import memecoach.FeedbackService;
import memecoach.HttpLLMClient;
import memecoach.LLMClient;
import memecoach.LLMFeedbackService;
import memecoach.MemeCoachManager;
import memecoach.QuizFeedback;
import memecoach.TemplateFeedbackService;

public class QuizActivity extends AppCompatActivity {

    private MemeCoachManager coachManager;
    private Spinner spinner;
    private DemoQuizCatalog.Question currentQuestion;
    // 全局静态变量，确保每次打开界面都会换下一题
    private static int globalQuizIndex = 0;
    private int currentQuizIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 初始化人物设定下拉菜单
        spinner = findViewById(R.id.spinnerPersona);

        String[] personas = {"Supportive", "Penguin", "Teacher"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                personas
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(adapter);

        // 初始化反馈服务：优先使用 AI，失败时回退到本地模板
        FeedbackService fallback =
                new TemplateFeedbackService(new Random());

        FeedbackService feedbackService = fallback;

        String endpoint = BuildConfig.LLM_API_URL;
        String token = BuildConfig.DEMO_API_TOKEN;

        if (endpoint != null && !endpoint.trim().isEmpty()
                && token != null && !token.trim().isEmpty()) {
            try {
                LLMClient client = new HttpLLMClient(endpoint, token);
                feedbackService = new LLMFeedbackService(client, fallback);
            } catch (IllegalArgumentException e) {
                // 配置不合法时继续使用本地模板反馈
            }
        }

        coachManager = new MemeCoachManager(
                feedbackService,
                new DemoExplanationProvider(
                        DemoQuizCatalog.explanations()
                ),
                Executors.newSingleThreadExecutor(),
                ContextCompat.getMainExecutor(this)
        );

        // 从全局静态变量取当前题号，并让全局变量加1，为下一次打开做准备
        currentQuizIndex = globalQuizIndex;
        globalQuizIndex = (globalQuizIndex + 1) % DemoQuizCatalog.count();
        loadQuiz(currentQuizIndex);

        RadioGroup radioGroup = findViewById(R.id.radioGroupOptions);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnNextQuiz = findViewById(R.id.btnNextQuiz);

        // 换题按钮
        btnNextQuiz.setOnClickListener(v -> {
            currentQuizIndex = globalQuizIndex;
            globalQuizIndex = (globalQuizIndex + 1) % DemoQuizCatalog.count();
            loadQuiz(currentQuizIndex);
        });

        // 提交按钮
        btnSubmit.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(
                        this,
                        "Please select an option!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int selectedIndex = radioGroup.indexOfChild(
                    findViewById(selectedId)
            );

            String personaId = spinner.getSelectedItem()
                    .toString()
                    .toLowerCase(java.util.Locale.ROOT);

            User currentUser = new User(
                    java.util.UUID.fromString(
                            "11111111-1111-1111-1111-111111111111"
                    ),
                    User.Role.Member,
                    "Player1",
                    "1234"
            );

            // 将当前题目转换为 MemeCoachManager 使用的 Quiz
            Quiz realQuiz = new Quiz(
                    currentQuestion.text(),
                    currentQuestion.options(),
                    currentQuestion.correctIndex()
            );

            // 提交答案；错误答案由反馈服务生成解释
            coachManager.submitAnswer(
                    realQuiz,
                    currentUser,
                    selectedIndex,
                    personaId,
                    new FeedbackCallback() {

                        @Override
                        public void onCorrect() {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(
                                        QuizActivity.this,
                                        FeedbackActivity.class
                                );

                                intent.putExtra("isCorrect", true);
                                intent.putExtra(
                                        "headline",
                                        "Amazing job! You got it right!"
                                );
                                intent.putExtra(
                                        "explanation",
                                        "You are truly a master of this topic."
                                );
                                intent.putExtra(
                                        "correctAnswer",
                                        currentQuestion.options()[
                                                currentQuestion.correctIndex()
                                        ]
                                );
                                intent.putExtra("imageKey", "supportive");
                                intent.putExtra("personaId", personaId);
                                intent.putExtra(
                                        "userAnswer",
                                        currentQuestion.options()[selectedIndex]
                                );
                                intent.putExtra(
                                        "questionText",
                                        currentQuestion.text()
                                );

                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onFeedback(QuizFeedback feedback) {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(
                                        QuizActivity.this,
                                        FeedbackActivity.class
                                );

                                intent.putExtra("isCorrect", false);
                                intent.putExtra(
                                        "headline",
                                        feedback.headline()
                                );
                                intent.putExtra(
                                        "explanation",
                                        feedback.explanation()
                                );
                                intent.putExtra(
                                        "correctAnswer",
                                        feedback.correctAnswer()
                                );
                                intent.putExtra(
                                        "imageKey",
                                        feedback.imageKey()
                                );
                                intent.putExtra("personaId", personaId);
                                intent.putExtra(
                                        "userAnswer",
                                        currentQuestion.options()[selectedIndex]
                                );
                                intent.putExtra(
                                        "questionText",
                                        currentQuestion.text()
                                );

                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            QuizActivity.this,
                                            "Error: " + message,
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }
                    }
            );
        });
    }

    // 动态加载题目
    private void loadQuiz(int index) {
        int questionCount = DemoQuizCatalog.count();

        if (questionCount <= 0) {
            Toast.makeText(
                    this,
                    "No quiz questions available.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        currentQuestion = DemoQuizCatalog.questionAt(
                index % questionCount
        );

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

        // 清空上一题的选择状态
        RadioGroup radioGroup = findViewById(R.id.radioGroupOptions);
        radioGroup.clearCheck();
    }
}
