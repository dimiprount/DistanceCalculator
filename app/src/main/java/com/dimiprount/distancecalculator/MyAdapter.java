package com.dimiprount.distancecalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    Context ctxt;
    ArrayList<Routes> myData;

    public MyAdapter(Context context, ArrayList<Routes> data){
        this.ctxt = context;
        this.myData = data;
    }

    @Override
    public int getCount() { 
        return myData.size();
    }

    @Override
    public Object getItem(int position) {
        return myData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView == null){
            LayoutInflater li = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.list_items, parent, false); 
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder)convertView.getTag();
        }

        Routes routes = (Routes) getItem(position);
        vh.showcell.setText("From: "  + routes.getsOrigin() + "\n" + "To: " + routes.getsDestination() + "\n" + "\n" + "The distance is: " + routes.getsDisDur());
        notifyDataSetChanged();
        return convertView;
    }

    public void remove(Routes selectedItem) {
        myData.remove(selectedItem);
        notifyDataSetChanged();
    }


    private class ViewHolder {
        public TextView showcell;

        public ViewHolder(View convertView) {
            showcell = (TextView)convertView.findViewById(R.id.tvListItems);

        }
    }
}

