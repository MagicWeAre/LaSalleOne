package com.wearemagic.lasalle.one.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wearemagic.lasalle.one.objects.ChargeCredit;
import com.wearemagic.lasalle.one.R;

import org.apache.commons.text.WordUtils;

import java.util.List;

public class ChargeCreditAdapter extends RecyclerView.Adapter<ChargeCreditAdapter.MyViewHolder> {
    Context context;

    private List<ChargeCredit> movementsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView desc, amount, date;

        public MyViewHolder(View view) {
            super(view);
            desc = view.findViewById(R.id.descChargeCredit);
            amount = view.findViewById(R.id.amountChargeCredit);
            date = view.findViewById(R.id.dateChargeCredit);
        }
    }

    public ChargeCreditAdapter(List<ChargeCredit> movementsList, Context context) {
        this.movementsList = movementsList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.charge_credit_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ChargeCredit movement = movementsList.get(position);
        holder.desc.setText(WordUtils.capitalizeFully(movement.getDesc()));
        holder.amount.setText(movement.getAmount());
        holder.date.setText(movement.getDate());

        if(movement.getAmount() != null){
            if (movement.getAmount().startsWith("+")){
                holder.amount.setTextColor(ContextCompat.getColor(this.context, R.color.colorPositiveGreen));
            } else {
                holder.amount.setTextColor(ContextCompat.getColor(this.context, R.color.colorText));
            }
        }
    }

    @Override
    public int getItemCount() {
        return movementsList.size();
    }
}