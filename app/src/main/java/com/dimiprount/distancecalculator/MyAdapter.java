package com.dimiprount.distancecalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dimiprount on 7/9/2015.
 */
public class MyAdapter extends BaseAdapter {
    Context ctxt;
    ArrayList<Routes> myData;

    // Constructor
    public MyAdapter(Context context, ArrayList<Routes> data){
        this.ctxt = context;
        this.myData = data;
    }

    @Override
    public int getCount() {     // How many items are in the data set represented by this Adapter.
        return myData.size();
    }

    @Override
    public Object getItem(int position) {       // Get the data item associated with the specified position in the data set.
        return myData.get(position);
    }

    @Override
    public long getItemId(int position) {       // Get the row id associated with the specified position in the list.
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView == null){        // If the first cell is empty
            LayoutInflater li = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.list_items, parent, false);       // How it will look like
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder)convertView.getTag();      // Recycle an existing object of holder in order not to create a new one
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


    private class ViewHolder {      // Definition of the holder
        public TextView showcell;

        public ViewHolder(View convertView) {
            showcell = (TextView)convertView.findViewById(R.id.tvListItems);        // Restore the result of the findViewById

        }
    }
}

