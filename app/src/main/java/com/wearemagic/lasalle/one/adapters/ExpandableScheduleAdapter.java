package com.wearemagic.lasalle.one.adapters;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.wearemagic.lasalle.one.R;
import com.wearemagic.lasalle.one.objects.SchedulePiece;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpandableScheduleAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> scheduleDayList;
    private HashMap<String, ArrayList<SchedulePiece>> schedulePieceList;
    private Context context;

    public ExpandableScheduleAdapter(Context c, ArrayList<String> parentList, HashMap<String, ArrayList<SchedulePiece>> childHashMap) {
        this.context = c;
        this.scheduleDayList = parentList;
        this.schedulePieceList = childHashMap;
    }

    @Override
    public int getGroupCount() {
        return scheduleDayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return schedulePieceList.get(scheduleDayList.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return scheduleDayList.get(groupPosition);
    }

    @Override
    public SchedulePiece getChild(int groupPosition, int childPosition) {
        return schedulePieceList.get(scheduleDayList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {


        String dayString = getGroup(groupPosition);

        if (convertView == null){
            LayoutInflater parentInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = parentInflater.inflate(R.layout.day_row, null);
        }

        ConstraintLayout groupConstraintLayout = convertView.findViewById(R.id.dayRowCL);
        TextView dayNameTextView = convertView.findViewById(R.id.nameScheduleDay);
        dayNameTextView.setText(dayString);

        groupConstraintLayout.setVisibility(View.VISIBLE);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SchedulePiece childSchedulePiece = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater childInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = childInflater.inflate(R.layout.schedule_row, null);
        }

        ConstraintLayout childConstraintLayout = convertView.findViewById(R.id.scheduleRowCL);
        TextView hourRangeTextView = convertView.findViewById(R.id.nameScheduleHourRange);
        TextView buildingTextView = convertView.findViewById(R.id.buildingScheduleRow);
        TextView roomTextView = convertView.findViewById(R.id.roomScheduleRow);

        hourRangeTextView.setText(childSchedulePiece.getHourRange());
        buildingTextView.setText(childSchedulePiece.getBuilding());
        roomTextView.setText(childSchedulePiece.getRoom());

        childConstraintLayout.setVisibility(View.VISIBLE);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
