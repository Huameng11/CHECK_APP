package com.example.a03_check;


import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {


    private EditText editName;
    private EditText editParkingSpot;
    private EditText editLicensePlate;
    private EditText editPhone;
    private Button saveButton;
    private Button deleteButton;
    private ParkingDatabaseHelper dbHelper;
    private String currentLicensePlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        dbHelper = new ParkingDatabaseHelper(this);

        //editFloor = findViewById(R.id.edit_floor);
        editName = findViewById(R.id.edit_name);
        editParkingSpot = findViewById(R.id.edit_parking_spot);
        editLicensePlate = findViewById(R.id.edit_license_plate);
        editPhone = findViewById(R.id.edit_phone);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        // 获取传递过来的数据
        Intent intent = getIntent();
        String floor = intent.getStringExtra("floor");
        String name = intent.getStringExtra("name");
        String parkingSpot = intent.getStringExtra("parkingSpot");
        String licensePlate = intent.getStringExtra("licensePlate");
        String phone = intent.getStringExtra("phone");

        // 设置初始值
        //editFloor.setText(floor);
        editName.setText(name);
        editParkingSpot.setText(parkingSpot);
        editLicensePlate.setText(licensePlate);
        editPhone.setText(phone);

        currentLicensePlate = licensePlate;

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            // 获取修改后的值
            //String updatedFloor = editFloor.getText().toString();

            String updatedName = editName.getText().toString();
            String updatedParkingSpot = editParkingSpot.getText().toString();
            String updatedLicensePlate = editLicensePlate.getText().toString();
            String updatedPhone = editPhone.getText().toString();
            // 更新数据库中的记录
            updateParkingInfo("floor", updatedName, updatedParkingSpot, updatedLicensePlate, updatedPhone);
            // 将修改后的值传递回MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("floor", "floor");
            resultIntent.putExtra("name", updatedName);
            resultIntent.putExtra("parkingSpot", updatedParkingSpot);
            resultIntent.putExtra("licensePlate", updatedLicensePlate);
            resultIntent.putExtra("phone", updatedPhone);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 删除按钮点击事件
        deleteButton.setOnClickListener(v -> {
            deleteParkingInfo();
        });
    }
    private void updateParkingInfo(String floor, String name, String parkingSpot, String licensePlate, String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ParkingDatabaseHelper.COLUMN_PARKING_SPOT, parkingSpot);
        values.put(ParkingDatabaseHelper.COLUMN_NAME, name);
        values.put(ParkingDatabaseHelper.COLUMN_FLOOR, floor);
        values.put(ParkingDatabaseHelper.COLUMN_LICENSE_PLATE, licensePlate);
        values.put(ParkingDatabaseHelper.COLUMN_PHONE, phone);
        int updatedRows = db.update(ParkingDatabaseHelper.TABLE_PARKING_INFO, values, ParkingDatabaseHelper.COLUMN_LICENSE_PLATE + "=?", new String[]{currentLicensePlate});
        db.close();

        if (updatedRows > 0) {
            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteParkingInfo() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(ParkingDatabaseHelper.TABLE_PARKING_INFO, ParkingDatabaseHelper.COLUMN_LICENSE_PLATE + "=?", new String[]{currentLicensePlate});
        db.close();

        if (deletedRows > 0) {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        } else {
            // Handle error
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }
}