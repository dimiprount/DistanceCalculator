package com.dimiprount.distancecalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by dimiprount on 7/9/2015.
 */

public class DbView extends FragmentActivity {

    MyAdapter ma;
    ListView lv;
    ArrayList<Routes> routes = new ArrayList<Routes>();
    DbDatabase info;
    final Context myContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbview);

        lv = (ListView)findViewById(R.id.listView);
        info = new DbDatabase(this);
        try {
            info.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        routes = info.getData();
        info.close();

        ma = new MyAdapter(this, routes);
        lv.setAdapter(ma);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle("" + lv.getCheckedItemCount());       // How many items are selected

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater mi = mode.getMenuInflater();
                mi.inflate(R.menu.multidelete,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                final SparseBooleanArray selected = lv.getCheckedItemPositions();
                switch (item.getItemId()){
                    case R.id.multidelete:
                        AlertDialog.Builder adb = new AlertDialog.Builder(myContext);
                        adb.setMessage("Delete?")
                                .setCancelable(true)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for(int i = selected.size() -1;  i >= 0; i--){
                                            if(selected.valueAt(i)){
                                                Routes selectedItem = (Routes) ma.getItem(selected.keyAt(i));
                                                info.delete(selectedItem);
                                                ma.remove(selectedItem);

                                            }
                                            mode.finish();
                                        }
                                    }
                                });
                        AlertDialog alertDialog = adb.create();
                        alertDialog.show();
                        return true;
                    default:
                        return false;
                }


            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MapsActivity.class));
    }



}
