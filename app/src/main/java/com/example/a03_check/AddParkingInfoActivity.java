package com.example.a03_check;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddParkingInfoActivity extends AppCompatActivity {

    private EditText parkingSpotEditText;
    private EditText floorEditText;
    private EditText nameEditText;
    private EditText licensePlateEditText;
    private EditText phoneEditText;
    private Button saveButton;
    private ParkingDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        dbHelper = new ParkingDatabaseHelper(this);

        parkingSpotEditText = findViewById(R.id.edit_parking_spot);
        floorEditText = findViewById(R.id.edit_floor);
        nameEditText = findViewById(R.id.edit_name);
        licensePlateEditText = findViewById(R.id.edit_license_plate);
        phoneEditText = findViewById(R.id.edit_phone);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveParkingInfo();
            }
        });
    }

    private void saveParkingInfo() {
        String parkingSpot = parkingSpotEditText.getText().toString();
        String floor = floorEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String licensePlate = licensePlateEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ParkingDatabaseHelper.COLUMN_PARKING_SPOT, parkingSpot);
        values.put(ParkingDatabaseHelper.COLUMN_FLOOR, floor);
        values.put(ParkingDatabaseHelper.COLUMN_NAME, name);
        values.put(ParkingDatabaseHelper.COLUMN_LICENSE_PLATE, licensePlate);
        values.put(ParkingDatabaseHelper.COLUMN_PHONE, phone);

        long newRowId = db.insert(ParkingDatabaseHelper.TABLE_PARKING_INFO, null, values);
        db.close();

        if (newRowId != -1) {
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Handle error
        }
    }
}