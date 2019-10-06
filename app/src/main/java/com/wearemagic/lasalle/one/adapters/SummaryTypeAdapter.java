package com.wearemagic.lasalle.one.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.objects.SummaryType;

import java.util.List;

public class SummaryTypeAdapter extends RecyclerView.Adapter<SummaryTypeAdapter.MyViewHolder> {
    Context context;

    private List<SummaryType> summaryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView desc, amount, date;

        public MyViewHolder(View view) {
            super(view);
            desc = view.findViewById(R.id.descSummaryType);
            amount = view.findViewById(R.id.amountSummaryType);
        }
    }


    public SummaryTypeAdapter(List<SummaryType> summaryList, Context context) {
        this.summaryList = summaryList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.summary_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SummaryType movement = summaryList.get(position);
        holder.desc.setText(movement.getType());
        holder.amount.setText(movement.getAmount());
        if (movement.getAmount() != null){
            if (movement.getAmount().startsWith("+")){
                holder.amount.setTextColor(ContextCompat.getColor(this.context, R.color.colorPositiveGreen));
            } else {
                holder.amount.setTextColor(ContextCompat.getColor(this.context, R.color.colorText));
            }
        }
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }
}