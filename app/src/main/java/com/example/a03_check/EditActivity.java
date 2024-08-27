package com.example.a03_check;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {

    private EditText editFloor;
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

        editFloor = findViewById(R.id.edit_floor);
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
        editFloor.setText(floor);
        editName.setText(name);
        editParkingSpot.setText(parkingSpot);
        editLicensePlate.setText(licensePlate);
        editPhone.setText(phone);

        currentLicensePlate = licensePlate;

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            // 获取修改后的值
            String updatedFloor = editFloor.getText().toString();
            String updatedName = editName.getText().toString();
            String updatedParkingSpot = editParkingSpot.getText().toString();
            String updatedLicensePlate = editLicensePlate.getText().toString();
            String updatedPhone = editPhone.getText().toString();

            // 将修改后的值传递回MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("floor", updatedFloor);
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

    private void deleteParkingInfo() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(ParkingDatabaseHelper.TABLE_PARKING_INFO, ParkingDatabaseHelper.COLUMN_LICENSE_PLATE + "=?", new String[]{currentLicensePlate});
        db.close();

        if (deletedRows > 0) {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Handle error
        }
    }
}