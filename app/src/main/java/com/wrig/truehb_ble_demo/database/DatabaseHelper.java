package com.wrig.truehb_ble_demo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wrig.truehb_ble_demo.modal.TestDetailsModal;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static  String DATABASE_NAME = "test_db";

    public static final String TABLE_NAME = "test_details";
    public static final String COL_DEVICE_ID= "Device_Id";
    public static final String COL_HB_RESULT = "HB";
    public static final String COL_DATE = "DATE";
    public static final String COL_TIME = "TIME";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +TABLE_NAME+ "("+
                COL_DEVICE_ID+" TEXT, " +
                COL_HB_RESULT+ " TEXT, "+
                COL_DATE + " TEXT, " +
                COL_TIME + " TEXT  )"+"");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    public boolean insertData( TestDetailsModal detailsModal) throws Exception {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DEVICE_ID,detailsModal.getDeviceid());
        contentValues.put(COL_HB_RESULT,detailsModal.getHbresult());
        contentValues.put(COL_DATE,detailsModal.getDate());
        contentValues.put(COL_TIME,detailsModal.getTime());

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;

    }
    public Cursor getAllData( ){
        SQLiteDatabase db=this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME ,null);
    }

}
