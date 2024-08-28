package com.example.a03_check;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ParkingDatabaseHelper extends SQLiteOpenHelper {
    // 数据库名称
    private static final String DATABASE_NAME = "parking.db";
    // 数据库版本
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_PARKING_INFO = "parking_info";
    // 列名
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PARKING_SPOT = "parking_spot";
    public static final String COLUMN_FLOOR = "floor";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LICENSE_PLATE = "license_plate";
    public static final String COLUMN_PHONE = "phone";

    // 创建表的SQL语句
    private static final String DATABASE_CREATE = "create table "
            + TABLE_PARKING_INFO + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PARKING_SPOT + " text not null, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_FLOOR + " text not null, "
            + COLUMN_LICENSE_PLATE + " text not null, "
            + COLUMN_PHONE + " text not null);";

    // 构造函数
    public ParkingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 创建数据库时调用
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // 升级数据库时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_INFO);
        onCreate(db);
    }

    // 获取所有停车信息
    public List<ParkingInfo> getAllParkingInfo() {
        List<ParkingInfo> parkingInfoList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PARKING_INFO, null, null, null, null, null, null);
        loadParkingInfoFromCursor(cursor, parkingInfoList);
        cursor.close();
        db.close();
        return parkingInfoList;
    }

    // 插入停车信息
    public void insertParkingInfo(ParkingInfo parkingInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARKING_SPOT, parkingInfo.getParkingSpot());
        values.put(COLUMN_FLOOR, parkingInfo.getFloor());
        values.put(COLUMN_NAME, parkingInfo.getName());
        values.put(COLUMN_LICENSE_PLATE, parkingInfo.getLicensePlate());
        values.put(COLUMN_PHONE, parkingInfo.getPhone());
        db.insert(TABLE_PARKING_INFO, null, values);
        db.close();
    }

    // 更新停车信息
    public void updateParkingInfo(ParkingInfo parkingInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARKING_SPOT, parkingInfo.getParkingSpot());
        values.put(COLUMN_FLOOR, parkingInfo.getFloor());
        values.put(COLUMN_NAME, parkingInfo.getName());
        values.put(COLUMN_LICENSE_PLATE, parkingInfo.getLicensePlate());
        values.put(COLUMN_PHONE, parkingInfo.getPhone());
        db.update(TABLE_PARKING_INFO, values, COLUMN_LICENSE_PLATE + "=?", new String[]{parkingInfo.getLicensePlate()});
        db.close();
    }

    // 删除停车信息
    public void deleteParkingInfo(String licensePlate) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PARKING_INFO, COLUMN_LICENSE_PLATE + "=?", new String[]{licensePlate});
        db.close();
    }

    // 从Cursor加载停车信息
    private void loadParkingInfoFromCursor(Cursor cursor, List<ParkingInfo> parkingInfoList) {
        if (cursor.moveToFirst()) {
            do {
                ParkingInfo info = createParkingInfoFromCursor(cursor);
                if (info != null) {
                    parkingInfoList.add(info);
                }
            } while (cursor.moveToNext());
        }
    }

    // 从Cursor创建ParkingInfo对象
    private ParkingInfo createParkingInfoFromCursor(Cursor cursor) {
        int parkingSpotIndex = cursor.getColumnIndex(COLUMN_PARKING_SPOT);
        int floorIndex = cursor.getColumnIndex(COLUMN_FLOOR);
        int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
        int licensePlateIndex = cursor.getColumnIndex(COLUMN_LICENSE_PLATE);
        int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);

        if (parkingSpotIndex != -1 && floorIndex != -1 && nameIndex != -1 && licensePlateIndex != -1 && phoneIndex != -1) {
            String parkingSpot = cursor.getString(parkingSpotIndex);
            String floor = cursor.getString(floorIndex);
            String name = cursor.getString(nameIndex);
            String licensePlate = cursor.getString(licensePlateIndex);
            String phone = cursor.getString(phoneIndex);
            return new ParkingInfo(floor, name, parkingSpot, licensePlate, phone);
        } else {
            Log.e("initializeData", "One or more columns not found in the database.");
            return null;
        }
    }

    // 初始化默认数据
    public void initializeDefaultData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PARKING_INFO, null, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            insertDefaultData(db);
        }
        cursor.close();
        db.close();
    }

    // 插入默认数据
    private void insertDefaultData(SQLiteDatabase db) {
        List<ParkingInfo> defaultData = getDefaultData();
        for (ParkingInfo info : defaultData) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PARKING_SPOT, info.getParkingSpot());
            values.put(COLUMN_FLOOR, info.getFloor());
            values.put(COLUMN_NAME, info.getName());
            values.put(COLUMN_LICENSE_PLATE, info.getLicensePlate());
            values.put(COLUMN_PHONE, info.getPhone());
            db.insert(TABLE_PARKING_INFO, null, values);
        }
    }

    // 获取默认数据
    private List<ParkingInfo> getDefaultData() {
        List<ParkingInfo> defaultData = new ArrayList<>();
        defaultData.add(new ParkingInfo("1#", "白书颜", "1#", "晋DF15999", "15235592555"));
        defaultData.add(new ParkingInfo("2#", "杨芳区", "2#", "晋DV9867", "15536120555"));
        defaultData.add(new ParkingInfo("3#", "孟凤珍", "3#", "晋D0673D", "15535563399"));
        defaultData.add(new ParkingInfo("4#", "赵周鹏", "4#", "晋D08V06", "13546512872"));
        defaultData.add(new ParkingInfo("5#", "赵向阳", "5#", "晋D23L07", "13994603362"));
        defaultData.add(new ParkingInfo("6#", "王屹", "6#", "晋EGM259", "17636303615"));
        defaultData.add(new ParkingInfo("7#", "郭婷", "7#", "晋D00B36", "15135556789"));
        defaultData.add(new ParkingInfo("8#", "李晓学", "8#", "晋D04018", "15035599608"));
        defaultData.add(new ParkingInfo("9#", "申小玲", "9#", "晋D61J50", "15503554445"));
        defaultData.add(new ParkingInfo("11#", "王欣", "11#", "晋DYY895", "15635559860"));
        defaultData.add(new ParkingInfo("12#", "陈康", "12#", "晋D7676L", "15534695556"));
        defaultData.add(new ParkingInfo("13#", "杨凌云", "13#", "晋D11P39", "13634112777"));
        defaultData.add(new ParkingInfo("14#", "石峰华", "14#", "晋D8035R", "13223554441"));
        defaultData.add(new ParkingInfo("16#", "武丽霞", "16#", "晋DYY281", "18635509355"));
        defaultData.add(new ParkingInfo("17#", "李慧萍", "17#", "晋D3188Z", "0U13994625999"));
        defaultData.add(new ParkingInfo("18#", "刘亚军", "18#", "晋D5851G", "15535597700"));
        defaultData.add(new ParkingInfo("19#", "张静", "19#", "晋D13642", "13223552205"));
        defaultData.add(new ParkingInfo("20#", "史康亮", "20#", "晋DDD797", "15835586028"));
        defaultData.add(new ParkingInfo("21#", "城进建材", "21#", "晋D1780M", "18649557776"));
        defaultData.add(new ParkingInfo("23#", "王晶晶", "23#", "晋DK2555", "18635543444"));
        defaultData.add(new ParkingInfo("24#", "王树丽", "24#", "晋D717P8", "15534527099"));
        defaultData.add(new ParkingInfo("25#", "陈俊义", "25#", "晋D02054", "13834308124"));
        defaultData.add(new ParkingInfo("26#", "李刚", "26#", "晋DDA3375", "18603428680"));
        defaultData.add(new ParkingInfo("27#", "王志杰", "27#", "晋D7305F", "13467053356"));
        defaultData.add(new ParkingInfo("28#", "杨彦芳", "28#", "京N6W722", "13301156775"));
        return defaultData;
    }
}