package com.chatt.demo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chatt.demo.R;
import com.chatt.demo.model.ChatUser;

import java.util.List;

/**
 * Created by guilhermeboroni on 01/10/17.
 */

public class CustomAdapter extends ArrayAdapter<ChatUser> {

    private Context ctx;

    public CustomAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapter(Context context, int resource, List<ChatUser> items) {
        super(context, resource, items);
    }

    public void setCtx(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView text1 = (TextView) v;
        text1.setText(getItem(position).getUsername());

        return v;
    }

}