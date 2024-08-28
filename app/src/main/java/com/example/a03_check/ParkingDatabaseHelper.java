package com.example.a03_check;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ParkingDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "parking.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PARKING_INFO = "parking_info";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PARKING_SPOT = "parking_spot";
    public static final String COLUMN_FLOOR = "floor";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LICENSE_PLATE = "license_plate";
    public static final String COLUMN_PHONE = "phone";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_PARKING_INFO + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PARKING_SPOT + " text not null, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_FLOOR + " text not null, "
            + COLUMN_LICENSE_PLATE + " text not null, "
            + COLUMN_PHONE + " text not null);";

    public ParkingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_INFO);
        onCreate(db);
    }
}