package com.wearemagic.lasalle.one.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.objects.ScheduleSubject;

import java.util.List;

public class ScheduleSubjectAdapter extends RecyclerView.Adapter<ScheduleSubjectAdapter.MyViewHolder> {

    Context context;
    private List<ScheduleSubject> scheduleSubjectList;
    private OnGSListener mOnGSListener;

    public interface OnGSListener {
        void onSubjectClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, section, instructor, credits, code;
        public ImageView instructorIcon, theoreticalIcon, practicalIcon;
        public View separator;
        ScheduleSubjectAdapter.OnGSListener onGSListener;

        public MyViewHolder(View view, ScheduleSubjectAdapter.OnGSListener onGSListener) {
            super(view);
            name = view.findViewById(R.id.nameScheduleSubject);
            section = view.findViewById(R.id.sectionScheduleSubject);
            instructor = view.findViewById(R.id.instructorScheduleSubject);
            credits = view.findViewById(R.id.creditsScheduleSubject);
            code = view.findViewById(R.id.codeScheduleSubject);

            instructorIcon = view.findViewById(R.id.instructorIconScheduleSubject);
            theoreticalIcon = view.findViewById(R.id.theoreticalIconScheduleSubject);
            practicalIcon = view.findViewById(R.id.practicalIconScheduleSubject);

            separator = view.findViewById(R.id.scheduleSubjectSeparator);

            this.onGSListener = onGSListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onGSListener.onSubjectClick(getAdapterPosition());
        }
    }

    public ScheduleSubjectAdapter(List<ScheduleSubject> subjectsList, Context context, OnGSListener onGSListener) {
        this.scheduleSubjectList = subjectsList;
        this.context = context;
        this.mOnGSListener = onGSListener;
    }

    @Override
    public ScheduleSubjectAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subjects_row, parent, false);

        return new ScheduleSubjectAdapter.MyViewHolder(itemView, mOnGSListener);
    }

    @Override
    public void onBindViewHolder(ScheduleSubjectAdapter.MyViewHolder holder, int position) {
        ScheduleSubject scheduleSubject = scheduleSubjectList.get(position);

        holder.name.setText(scheduleSubject.getSubjectName());
        holder.section.setText(scheduleSubject.getSectionCode());
        if (scheduleSubject.getInstructorName().isEmpty()){
            holder.instructorIcon.setVisibility(View.GONE);
            holder.instructor.setVisibility(View.GONE);
        } else {
            holder.instructorIcon.setVisibility(View.VISIBLE);
            holder.instructor.setVisibility(View.VISIBLE);
        }
        holder.instructor.setText(scheduleSubject.getInstructorName());
        holder.code.setText(scheduleSubject.getCourseCode());
        holder.credits.setText(context.getString(R.string.credits_placeholder_grades).concat(" ").concat(scheduleSubject.getCredits()));

        if (scheduleSubject.isTeorico()) {
            holder.theoreticalIcon.setVisibility(View.VISIBLE);
        } else {
            holder.theoreticalIcon.setVisibility(View.GONE);
        }

        if (scheduleSubject.isPractico()) {
            holder.practicalIcon.setVisibility(View.VISIBLE);
        } else {
            holder.practicalIcon.setVisibility(View.GONE);
        }

        int generatedColor = getConsistentColor(scheduleSubject.getSubjectName());
        ColorDrawable generatedDrawable = new ColorDrawable(generatedColor);
        holder.separator.setBackground(generatedDrawable);

    }

    @Override
    public int getItemCount() {
        return scheduleSubjectList.size();
    }

    public int getConsistentColor(String subjectName) {
        String opacity = "#99"; //opacity between 00-ff
        String hexColor = String.format(
                opacity + "%06X", (0xFFFFFF & subjectName.hashCode()));
        return Color.parseColor(hexColor);
    }

}
