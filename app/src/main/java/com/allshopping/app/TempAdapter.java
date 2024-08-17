package com.allshopping.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allshopping.app.models.TempModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class TempAdapter extends FirebaseRecyclerAdapter<TempModel, TempAdapter.Viewholder> {

    private static final String TAG = "TempAdapter";
    Context context;

    public TempAdapter(@NonNull FirebaseRecyclerOptions<TempModel> options, Context context) {
        super(options);
        this.context = context;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public TempAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.home_items_temp, parent, false);
        return new TempAdapter.Viewholder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull final TempAdapter.Viewholder holder, int position, @NonNull TempModel tempModel) {
        Log.d(TAG, "onBindViewHolder: position = " + position);

        holder.label.setText(tempModel.getName());

        FirebaseRecyclerOptions<TempChildModel> options = new FirebaseRecyclerOptions.Builder<TempChildModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference("HomeItems")
                        .child(getRef(position).getKey()).child("items").limitToFirst(8), TempChildModel.class)
                .build();

        TempChildAdapter tempChildAdapter = new TempChildAdapter(options, context);
        holder.recyclerView.setAdapter(tempChildAdapter);
        tempChildAdapter.startListening();

        holder.sellAllLebel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("CurrentPosition", getRef(adapterPosition).getKey());
                    intent.putExtra("itemName", holder.label.getText().toString().trim());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    class Viewholder extends RecyclerView.ViewHolder {

        TextView label, sellAllLebel;
        RecyclerView recyclerView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            label = itemView.findViewById(R.id.label);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            sellAllLebel = itemView.findViewById(R.id.sellAllLebel);

            recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        }
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        notifyDataSetChanged();
    }
}
