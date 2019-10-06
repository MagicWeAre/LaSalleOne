package com.wearemagic.lasalle.one.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wearemagic.lasalle.one.objects.ParcialGrade;
import com.wearemagic.lasalle.one.R;

import org.apache.commons.text.WordUtils;

import java.util.List;

public class ParcialGradeAdapter extends RecyclerView.Adapter<ParcialGradeAdapter.MyViewHolder> {

    private List<ParcialGrade> parcialsList;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, points, maxPoints, date, percentage;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.nameParcialGrade);
            points = view.findViewById(R.id.pointsParcialGrade);
            maxPoints = view.findViewById(R.id.maxPointsParcialGrade);
            date = view.findViewById(R.id.dateParcialGrade);
            percentage = view.findViewById(R.id.percentageParcialGrade);
        }
    }

    public ParcialGradeAdapter(List<ParcialGrade> parcialsList, Context context) {
        this.parcialsList = parcialsList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parcials_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ParcialGrade parcial = parcialsList.get(position);
        holder.name.setText(parcial.getParcialName());
        holder.points.setText(parcial.getPointsEarned());
        holder.maxPoints.setText(parcial.getMaxPoints());
        holder.date.setText(parcial.getDueDate());
        holder.percentage.setText(parcial.getPercentage());
    }

    @Override
    public int getItemCount() {
        return parcialsList.size();
    }
}