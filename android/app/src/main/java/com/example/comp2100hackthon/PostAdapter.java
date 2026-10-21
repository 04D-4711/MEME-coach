package com.example.comp2100hackthon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import dao.model.Post; // 如果报错，导入你们真实的 Post 包名

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final List<Post> posts;
    public PostAdapter(List<Post> posts) { this.posts = posts; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        // 1. 获取帖子里的第一条 Message 作为正文
        String content = "No content";
        try {
            // 尝试用 getAtIndex(0) 拿第一条消息
            dao.model.Message firstMessage = post.messages.getAtIndex(0);
            if (firstMessage != null) {
                content = firstMessage.message();
            }
        } catch (Exception e) {
            // 如果报错，尝试用迭代器拿第一条
            try {
                java.util.Iterator<dao.model.Message> it = post.messages.getAll();
                if (it.hasNext()) {
                    content = it.next().message();
                }
            } catch (Exception ex) {}
        }

        // 2. 展示正文
        holder.tvTopic.setText(content);

        // 3. 截取 Author ID 前8位显示（因为帖子实际存的是 UUID）
        String authorId = post.poster != null ? post.poster.toString().substring(0, 8) : "Unknown";
        holder.tvAuthor.setText("Poster: " + authorId);
    }

    @Override
    public int getItemCount() { return posts.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvAuthor;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvPostTopic);
            tvAuthor = itemView.findViewById(R.id.tvPostAuthor);
        }
    }
}
