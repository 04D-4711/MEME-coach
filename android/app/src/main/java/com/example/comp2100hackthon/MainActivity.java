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
import dao.PostDAO; // 导入真实的 PostDAO 包
import dao.model.Post; // 导入真实的 Post 包

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGoToQuiz = findViewById(R.id.btnGoToQuiz);
        btnGoToQuiz.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, QuizActivity.class));
        });

        RecyclerView recycler = findViewById(R.id.recyclerPosts);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // 从 PostDAO 拿数据
        List<Post> postList = new ArrayList<>();
        Iterator<Post> it = PostDAO.getInstance().getAll();
        while (it != null && it.hasNext()) {
            postList.add(it.next());
        }

        PostAdapter adapter = new PostAdapter(postList);
        recycler.setAdapter(adapter);
    }
}