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
        // 注意：这里如果有报错，说明你们的 Post 类里获取标题的方法不叫 getTopic()
        // 可能是 topic() 或者 getContent()，把光标放上去按 Option+Enter 看看
        holder.tvTopic.setText(post.topic);
        holder.tvAuthor.setText("Author ID: " + post.poster);
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
