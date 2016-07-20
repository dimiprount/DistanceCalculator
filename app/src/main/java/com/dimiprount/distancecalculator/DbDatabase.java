package com.dimiprount.distancecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseBooleanArray;

import java.sql.SQLException;
import java.util.ArrayList;

public class DbDatabase {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_LOCA = "locA";
    public static final String KEY_LOCB = "locB";
    public static final String KEY_DEST_DUR = "destDur";

    public static final String DATABASE_NAME = "MapsDatabase";
    public static final String DATABASE_TABLE = "MyTable";
    public static final int DATABASE_VERSION = 1;

    private Context myContext;
    private DbHelper myHelper;
    private SQLiteDatabase myDatabase;

    public static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_LOCA + " TEXT NOT NULL, " + KEY_LOCB + " TEXT NOT NULL, " + KEY_DEST_DUR + " TEXT NOT NULL);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public DbDatabase(Context c) {
        myContext = c;
        myHelper = new DbHelper(c);
    }

    public DbDatabase open() throws SQLException {
        myHelper = new DbHelper(myContext);
        myDatabase = myHelper.getWritableDatabase();
        return this;
    }

    public long createentry(String metLocA, String metLocB, String mCDD) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_LOCA, metLocA);
        cv.put(KEY_LOCB, metLocB);
        cv.put(KEY_DEST_DUR, mCDD);
        return myDatabase.insert(DATABASE_TABLE, null, cv);
    }

    public void close() {
        myHelper.close();
    }

    public ArrayList<Routes> getData() {
        ArrayList<Routes> routes = new ArrayList<Routes>();
        String  selectQuery = "SELECT * FROM MyTable";

        SQLiteDatabase db = myHelper.getWritableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Routes myRoutes = new Routes();
                myRoutes.setId(Integer.parseInt(c.getString(0)));
                myRoutes.setsOrigin(c.getString(1));
                myRoutes.setsDestination(c.getString(2));
                myRoutes.setsDisDur(c.getString(3));

                routes.add(myRoutes);
            }while (c.moveToNext());
        }
        db.close();
        return routes;
    }

    public void delete(Routes selectedItem) {
        SQLiteDatabase db = myHelper.getWritableDatabase();
        db.delete(DATABASE_TABLE, KEY_ROWID + " = ? ", new String[] { String.valueOf(selectedItem.getId()) });
        db.close();

    }
}
