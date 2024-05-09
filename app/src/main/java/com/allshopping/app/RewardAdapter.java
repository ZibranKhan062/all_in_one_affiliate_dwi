package com.allshopping.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.allshopping.app.models.Reward;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
    private List<Reward> rewardList;

    public RewardAdapter(List<Reward> rewardList) {
        this.rewardList = rewardList;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_item, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        holder.nameTextView.setText("Name : " + reward.getName());
        holder.emailTextView.setText("Email : " + reward.getEmail());
        holder.pointsTextView.setText("Points : " + String.valueOf(reward.getRewardPoints()));
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView pointsTextView;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.rewardName);
            emailTextView = itemView.findViewById(R.id.rewardEmail);
            pointsTextView = itemView.findViewById(R.id.rewardPoints);
        }
    }
}
