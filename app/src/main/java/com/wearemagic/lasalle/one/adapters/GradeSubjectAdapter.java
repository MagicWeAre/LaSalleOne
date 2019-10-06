package com.wearemagic.lasalle.one.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wearemagic.lasalle.one.objects.GradeSubject;
import com.wearemagic.lasalle.one.R;

import java.util.List;

public class GradeSubjectAdapter extends RecyclerView.Adapter<GradeSubjectAdapter.MyViewHolder> {

    Context context;
    private List<GradeSubject> gradeSubjectList;
    private OnGSListener mOnGSListener;

    public interface OnGSListener {
        void onSubjectClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, partial, code;
        OnGSListener onGSListener;

        public MyViewHolder(View view, OnGSListener onGSListener) {
            super(view);
            name = view.findViewById(R.id.nameGradeSubject);
            partial = view.findViewById(R.id.partialGradeSubject);
            code = view.findViewById(R.id.codeGradeSubject);

            this.onGSListener = onGSListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onGSListener.onSubjectClick(getAdapterPosition());
        }
    }

    public GradeSubjectAdapter(List<GradeSubject> gradesList, Context context, OnGSListener onGSListener) {
        this.gradeSubjectList = gradesList;
        this.context = context;
        this.mOnGSListener = onGSListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grades_row, parent, false);

        return new MyViewHolder(itemView, mOnGSListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GradeSubject gradeSubject = gradeSubjectList.get(position);
        holder.name.setText(gradeSubject.getSubjectName());
        holder.partial.setText(gradeSubject.getCurrentGrade());
        holder.code.setText(gradeSubject.getSectionCode());

        if(gradeSubject.getCurrentGrade() != null){
            int gradeColor = R.color.colorPrimary;

            switch (gradeSubject.gradeState()) {
                case 0: {
                    gradeColor = R.color.colorPrimaryText;
                    break;
                }

                case 1: {
                    gradeColor = R.color.colorText;
                    break;
                }

                case 2: {
                    gradeColor = R.color.colorAccent;
                    break;
                }

                case 3: {
                    gradeColor = R.color.colorGrey;
                    break;
                }

            }

            holder.partial.setTextColor(ContextCompat.getColor(this.context, gradeColor));
        }
    }

    @Override
    public int getItemCount() {
        return gradeSubjectList.size();
    }
}

