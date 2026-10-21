package com.example.comp2100hackthon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import dao.PostDAO;
import dao.model.Post;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 RecyclerView
        recycler = findViewById(R.id.recyclerPosts);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // 去答题的按钮
        Button btnGoToQuiz = findViewById(R.id.btnGoToQuiz);
        btnGoToQuiz.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, QuizActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 🔥 每次回到主界面，都重新加载一遍数据并刷新列表
        loadFeed();
    }

    private void loadFeed() {
        // 1. 触发持久化读盘
        try {
            persistentdata.DataManager.getInstance(getApplicationContext()).readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. 从 PostDAO 拿最新的数据
        List<Post> postList = new ArrayList<>();
        Iterator<Post> it = PostDAO.getInstance().getAll();
        while (it != null && it.hasNext()) {
            postList.add(it.next());
        }

        // 3. 更新 Adapter
        adapter = new PostAdapter(postList);
        recycler.setAdapter(adapter);
    }
}