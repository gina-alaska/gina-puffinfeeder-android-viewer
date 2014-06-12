package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Category;

import java.util.ArrayList;

/**
 * Created by Bobby on 6/10/2014.
 */
public class CategoriesAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Category> categories;

    public CategoriesAdapter(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categories.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categories.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater i = LayoutInflater.from(context);
            convertView = i.inflate(android.R.layout.simple_list_item_1, parent, false);
            convertView.setPadding(30,0,0,0);
        }
        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(categories.get(groupPosition).name);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater i = LayoutInflater.from(context);
            convertView = i.inflate(android.R.layout.simple_list_item_1, parent, false);
            convertView.setPadding(90,0,0,0);
            convertView.setBackgroundColor(Color.parseColor("#DDDDDD"));
        }
        TextView name = (TextView) convertView.findViewById(android.R.id.text1);
        name.setText(categories.get(groupPosition).get(childPosition).title);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
