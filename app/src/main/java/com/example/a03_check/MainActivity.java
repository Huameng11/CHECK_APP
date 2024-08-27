package com.example.a03_check;

import android.content.ContentValues;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements SuggestionAdapter.OnSuggestionClickListener {

    private List<ParkingInfo> parkingInfoList;
    private EditText inputPlate;
    private TextView resultTextView;
    private RecyclerView suggestionList;
    private SuggestionAdapter suggestionAdapter;
    private List<String> suggestions;
    private ParkingInfo currentParkingInfo;
    private ActivityResultLauncher<Intent> editActivityResultLauncher;
    private ParkingDatabaseHelper dbHelper;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ParkingDatabaseHelper(this);
        parkingInfoList = new ArrayList<>();
        initializeData();

        inputPlate = findViewById(R.id.input_plate);
        resultTextView = findViewById(R.id.result);
        suggestionList = findViewById(R.id.suggestion_list);

        suggestions = new ArrayList<>();
        suggestionAdapter = new SuggestionAdapter(suggestions, this);
        suggestionList.setLayoutManager(new LinearLayoutManager(this));
        suggestionList.setAdapter(suggestionAdapter);

        inputPlate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                updateSuggestions(query);
                currentParkingInfo = findParkingInfoByLicensePlate(query);
                if (currentParkingInfo != null) {
                    resultTextView.setText(
                            "车位号: " + currentParkingInfo.getParkingSpot() + "\n" +
                                    "楼层: " + currentParkingInfo.getFloor() + "\n" +
                                    "姓名: " + currentParkingInfo.getName() + "\n" +
                                    "车牌号: " + currentParkingInfo.getLicensePlate() + "\n" +
                                    "电话: " + currentParkingInfo.getPhone()
                    );
                    speakParkingSpot(currentParkingInfo.getParkingSpot());
                } else {
                    resultTextView.setText("未找到匹配的信息");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        editActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            String updatedFloor = data.getStringExtra("floor");
                            String updatedName = data.getStringExtra("name");
                            String updatedParkingSpot = data.getStringExtra("parkingSpot");
                            String updatedLicensePlate = data.getStringExtra("licensePlate");
                            String updatedPhone = data.getStringExtra("phone");

                            currentParkingInfo.setFloor(updatedFloor);
                            currentParkingInfo.setName(updatedName);
                            currentParkingInfo.setParkingSpot(updatedParkingSpot);
                            currentParkingInfo.setLicensePlate(updatedLicensePlate);
                            currentParkingInfo.setPhone(updatedPhone);

                            resultTextView.setText(
                                    "车位号: " + currentParkingInfo.getParkingSpot() + "\n" +
                                            "楼层: " + currentParkingInfo.getFloor() + "\n" +
                                            "姓名: " + currentParkingInfo.getName() + "\n" +
                                            "车牌号: " + currentParkingInfo.getLicensePlate() + "\n" +
                                            "电话: " + currentParkingInfo.getPhone()
                            );

                            saveParkingInfoToStorage(currentParkingInfo);
                        }
                    }
                }
        );

        // 初始化 TextToSpeech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TextToSpeech", "Language is not supported");
                    }
                } else {
                    Log.e("TextToSpeech", "Initialization failed");
                }
            }
        });

        // 找到按钮并设置点击监听器
        findViewById(R.id.check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddParkingInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void initializeData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(ParkingDatabaseHelper.TABLE_PARKING_INFO, null, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            List<ParkingInfo> defaultData = getDefaultData();
            for (ParkingInfo info : defaultData) {
                ContentValues values = new ContentValues();
                values.put(ParkingDatabaseHelper.COLUMN_PARKING_SPOT, info.getParkingSpot());
                values.put(ParkingDatabaseHelper.COLUMN_FLOOR, info.getFloor());
                values.put(ParkingDatabaseHelper.COLUMN_NAME, info.getName());
                values.put(ParkingDatabaseHelper.COLUMN_LICENSE_PLATE, info.getLicensePlate());
                values.put(ParkingDatabaseHelper.COLUMN_PHONE, info.getPhone());
                db.insert(ParkingDatabaseHelper.TABLE_PARKING_INFO, null, values);
            }
        }
        cursor.close();

        cursor = db.query(ParkingDatabaseHelper.TABLE_PARKING_INFO, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int parkingSpotIndex = cursor.getColumnIndex(ParkingDatabaseHelper.COLUMN_PARKING_SPOT);
                int floorIndex = cursor.getColumnIndex(ParkingDatabaseHelper.COLUMN_FLOOR);
                int nameIndex = cursor.getColumnIndex(ParkingDatabaseHelper.COLUMN_NAME);
                int licensePlateIndex = cursor.getColumnIndex(ParkingDatabaseHelper.COLUMN_LICENSE_PLATE);
                int phoneIndex = cursor.getColumnIndex(ParkingDatabaseHelper.COLUMN_PHONE);

                if (parkingSpotIndex != -1 && floorIndex != -1 && nameIndex != -1 && licensePlateIndex != -1 && phoneIndex != -1) {
                    String parkingSpot = cursor.getString(parkingSpotIndex);
                    String floor = cursor.getString(floorIndex);
                    String name = cursor.getString(nameIndex);
                    String licensePlate = cursor.getString(licensePlateIndex);
                    String phone = cursor.getString(phoneIndex);
                    parkingInfoList.add(new ParkingInfo(parkingSpot, name, floor, licensePlate, phone));
                } else {
                    // Handle the case where one or more columns are not found
                    Log.e("initializeData", "One or more columns not found in the database.");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    private List<ParkingInfo> getDefaultData() {
        List<ParkingInfo> defaultData = new ArrayList<>();
        defaultData.add(new ParkingInfo("E-1608", "白书颜", "1#", "晋DF15999", "15235592555"));
        defaultData.add(new ParkingInfo("E-706", "杨芳区", "2#", "晋DV9867", "15536120555"));
        defaultData.add(new ParkingInfo("E-1705", "孟凤珍", "3#", "晋D0673D", "15535563399"));
        defaultData.add(new ParkingInfo("E-705", "赵周鹏", "4#", "晋D08V06", "13546512872"));
        defaultData.add(new ParkingInfo("E-1305", "赵向阳", "5#", "晋D23L07", "13994603362"));
        defaultData.add(new ParkingInfo("E-1701", "王屹", "6#", "晋EGM259", "17636303615"));
        defaultData.add(new ParkingInfo("E-1508", "郭婷", "7#", "晋D00B36", "15135556789"));
        defaultData.add(new ParkingInfo("E-710", "李晓学", "8#", "晋D04018", "15035599608"));
        defaultData.add(new ParkingInfo("E-602", "申小玲", "9#", "晋D61J50", "15503554445"));
        defaultData.add(new ParkingInfo("E-910", "王欣", "11#", "晋DYY895", "15635559860"));
        defaultData.add(new ParkingInfo("E-608", "陈康", "12#", "晋D7676L", "15534695556"));
        defaultData.add(new ParkingInfo("E-907", "杨凌云", "13#", "晋D11P39", "13634112777"));
        defaultData.add(new ParkingInfo("E-1505", "石峰华", "14#", "晋D8035R", "13223554441"));
        defaultData.add(new ParkingInfo("E-1403", "武丽霞", "16#", "晋DYY281", "18635509355"));
        defaultData.add(new ParkingInfo("E-1811", "李慧萍", "17#", "晋D3188Z", "0U13994625999"));
        defaultData.add(new ParkingInfo("E-1307", "刘亚军", "18#", "晋D5851G", "15535597700"));
        defaultData.add(new ParkingInfo("E-1408", "张静", "19#", "晋D13642", "13223552205"));
        defaultData.add(new ParkingInfo("E-1102", "史康亮", "20#", "晋DDD797", "15835586028"));
        defaultData.add(new ParkingInfo("E-1110", "城进建材", "21#", "晋D1780M", "18649557776"));
        defaultData.add(new ParkingInfo("D-3-1202", "王晶晶", "23#", "晋DK2555", "18635543444"));
        defaultData.add(new ParkingInfo("E-1908", "王树丽", "24#", "晋D717P8", "15534527099"));
        defaultData.add(new ParkingInfo("E-1510", "陈俊义", "25#", "晋D02054", "13834308124"));
        defaultData.add(new ParkingInfo("E-807", "李刚", "26#", "晋DDA3375", "18603428680"));
        defaultData.add(new ParkingInfo("E-702", "王志杰", "27#", "晋D7305F", "13467053356"));
        defaultData.add(new ParkingInfo("C-1-402", "杨彦芳", "28#", "京N6W722", "13301156775"));
        return defaultData;
    }

    private ParkingInfo findParkingInfoByLicensePlate(String licensePlate) {
        for (ParkingInfo parkingInfo : parkingInfoList) {
            if (parkingInfo.getLicensePlate().equals(licensePlate)) {
                return parkingInfo;
            }
        }
        return null;
    }

    private void updateSuggestions(String query) {
        suggestions.clear();
        if (!query.isEmpty()) {
            suggestions.addAll(parkingInfoList.stream()
                    .map(ParkingInfo::getLicensePlate)
                    .filter(licensePlate -> licensePlate.contains(query))
                    .collect(Collectors.toList()));
        }
        suggestionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSuggestionClick(String suggestion) {
        inputPlate.setText(suggestion);
        inputPlate.setSelection(suggestion.length());
    }

    public void onResultClick(View view) {
        if (currentParkingInfo != null) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("floor", currentParkingInfo.getFloor());
            intent.putExtra("name", currentParkingInfo.getName());
            intent.putExtra("parkingSpot", currentParkingInfo.getParkingSpot());
            intent.putExtra("licensePlate", currentParkingInfo.getLicensePlate());
            intent.putExtra("phone", currentParkingInfo.getPhone());
            editActivityResultLauncher.launch(intent);
        }
    }

    private void saveParkingInfoToStorage(ParkingInfo parkingInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ParkingDatabaseHelper.COLUMN_PARKING_SPOT, parkingInfo.getParkingSpot());
        values.put(ParkingDatabaseHelper.COLUMN_FLOOR, parkingInfo.getFloor());
        values.put(ParkingDatabaseHelper.COLUMN_NAME, parkingInfo.getName());
        values.put(ParkingDatabaseHelper.COLUMN_LICENSE_PLATE, parkingInfo.getLicensePlate());
        values.put(ParkingDatabaseHelper.COLUMN_PHONE, parkingInfo.getPhone());

        db.update(ParkingDatabaseHelper.TABLE_PARKING_INFO, values, ParkingDatabaseHelper.COLUMN_LICENSE_PLATE + "=?", new String[]{parkingInfo.getLicensePlate()});
        db.close();
    }

    private void speakParkingSpot(String parkingSpot) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.speak("车位号 " + parkingSpot, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}