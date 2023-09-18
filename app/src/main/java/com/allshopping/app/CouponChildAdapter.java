package com.allshopping.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.allshopping.app.CouponPackage.CouponDetails;
import com.allshopping.app.models.TempModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.List;

public class CouponChildAdapter extends FirebaseRecyclerAdapter<CouponChildModel, CouponChildAdapter.Viewholder> {


    List<TempModel> tempModelList;
    Context context;

    public CouponChildAdapter(@NonNull FirebaseRecyclerOptions<CouponChildModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public CouponChildAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.coupon_items_temp_child, parent, false);
        return new CouponChildAdapter.Viewholder(v);
    }


    @Override
    protected void onBindViewHolder(@NonNull final CouponChildAdapter.Viewholder holder, final int position, @NonNull CouponChildModel couponChildModel) {


        holder.coupon_title.setText(couponChildModel.getCouponTitle());
        holder.coupon_desc.setText(couponChildModel.getCouponDescription());

        holder.deal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CouponDetails.class);
                intent.putExtra("Title", couponChildModel.getCouponTitle());
                intent.putExtra("Description", couponChildModel.getCouponDescription());
                intent.putExtra("websiteLink", couponChildModel.getWebLink());
                intent.putExtra("coupon", couponChildModel.getCoupon());
                intent.putExtra("expiryDate", couponChildModel.getExpiryDate());
                context.startActivity(intent);

            }
        });

    }


    class Viewholder extends RecyclerView.ViewHolder {


        TextView coupon_title, coupon_desc;
        Button deal_button;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            coupon_title = itemView.findViewById(R.id.coupon_title);
            coupon_desc = itemView.findViewById(R.id.coupon_desc);
            deal_button = itemView.findViewById(R.id.deal_button);

        }
    }
}
