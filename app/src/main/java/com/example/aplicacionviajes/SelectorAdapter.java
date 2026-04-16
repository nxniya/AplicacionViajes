package com.example.aplicacionviajes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SelectorAdapter extends BaseAdapter {

    public static class Item {
        final String title;
        final String subtitle;
        final int iconResId;
        final int iconBgResId;

        public Item(String title, String subtitle, int iconResId, int iconBgResId) {
            this.title = title;
            this.subtitle = subtitle;
            this.iconResId = iconResId;
            this.iconBgResId = iconBgResId;
        }
    }

    private final Context context;
    private final List<Item> items;

    public SelectorAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.selector_item, parent, false);
        }
        Item item = items.get(position);
        TextView tvTitle = convertView.findViewById(R.id.tvMenuTitle);
        TextView tvSubtitle = convertView.findViewById(R.id.tvMenuSubtitle);
        ImageView ivIcon = convertView.findViewById(R.id.ivCardIcon);

        tvTitle.setText(item.title);
        tvSubtitle.setText(item.subtitle);
        ivIcon.setImageResource(item.iconResId);
        ivIcon.setBackgroundResource(item.iconBgResId);

        return convertView;
    }
}
