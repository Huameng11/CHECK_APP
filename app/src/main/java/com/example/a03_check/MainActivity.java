package com.example.a03_check;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                } else {
                    resultTextView.setText("未找到匹配的信息");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // 注册 Activity Result Launcher
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

                            // 更新当前ParkingInfo对象
                            currentParkingInfo.setFloor(updatedFloor);
                            currentParkingInfo.setName(updatedName);
                            currentParkingInfo.setParkingSpot(updatedParkingSpot);
                            currentParkingInfo.setLicensePlate(updatedLicensePlate);
                            currentParkingInfo.setPhone(updatedPhone);

                            // 更新显示结果
                            resultTextView.setText(
                                    "车位号: " + currentParkingInfo.getParkingSpot() + "\n" +
                                    "楼层: " + currentParkingInfo.getFloor() + "\n" +
                                    "姓名: " + currentParkingInfo.getName() + "\n" +
                                    "车牌号: " + currentParkingInfo.getLicensePlate() + "\n" +
                                    "电话: " + currentParkingInfo.getPhone()
                            );

                            // 保存更新后的数据到本地存储（如SharedPreferences或SQLite数据库）
                            saveParkingInfoToStorage(currentParkingInfo);
                        }
                    }
                }
        );
    }

    // 初始化数据
    private void initializeData() {
        parkingInfoList.add(new ParkingInfo("E-1608", "白书颜", "1#", "晋DF15999", "15235592555"));
        parkingInfoList.add(new ParkingInfo("E-706", "杨芳区", "2#", "晋DV9867", "15536120555"));
        parkingInfoList.add(new ParkingInfo("E-1705", "孟凤珍", "3#", "晋D0673D", "15535563399"));
        parkingInfoList.add(new ParkingInfo("E-705", "赵周鹏", "4#", "晋D08V06", "13546512872"));
        parkingInfoList.add(new ParkingInfo("E-1305", "赵向阳", "5#", "晋D23L07", "13994603362"));
        parkingInfoList.add(new ParkingInfo("E-1701", "王屹", "6#", "晋EGM259", "17636303615"));
        parkingInfoList.add(new ParkingInfo("E-1508", "郭婷", "7#", "晋D00B36", "15135556789"));
        parkingInfoList.add(new ParkingInfo("E-710", "李晓学", "8#", "晋D04018", "15035599608"));
        parkingInfoList.add(new ParkingInfo("E-602", "申小玲", "9#", "晋D61J50", "15503554445"));
        parkingInfoList.add(new ParkingInfo("E-910", "王欣", "11#", "晋DYY895", "15635559860"));
        parkingInfoList.add(new ParkingInfo("E-608", "陈康", "12#", "晋D7676L", "15534695556"));
        parkingInfoList.add(new ParkingInfo("E-907", "杨凌云", "13#", "晋D11P39", "13634112777"));
        parkingInfoList.add(new ParkingInfo("E-1505", "石峰华", "14#", "晋D8035R", "13223554441"));
        // 15# 没有数据，跳过
        parkingInfoList.add(new ParkingInfo("E-1403", "武丽霞", "16#", "晋DYY281", "18635509355"));
        parkingInfoList.add(new ParkingInfo("E-1811", "李慧萍", "17#", "晋D3188Z", "0U13994625999"));
        parkingInfoList.add(new ParkingInfo("E-1307", "刘亚军", "18#", "晋D5851G", "15535597700"));
        parkingInfoList.add(new ParkingInfo("E-1408", "张静", "19#", "晋D13642", "13223552205"));
        parkingInfoList.add(new ParkingInfo("E-1102", "史康亮", "20#", "晋DDD797", "15835586028"));
        parkingInfoList.add(new ParkingInfo("E-1110", "城进建材", "21#", "晋D1780M", "18649557776"));
        parkingInfoList.add(new ParkingInfo("D-3-1202", "王晶晶", "23#", "晋DK2555", "18635543444"));
        parkingInfoList.add(new ParkingInfo("E-1908", "王树丽", "24#", "晋D717P8", "15534527099"));
        parkingInfoList.add(new ParkingInfo("E-1510", "陈俊义", "25#", "晋D02054", "13834308124"));
        parkingInfoList.add(new ParkingInfo("E-807", "李刚", "26#", "晋DDA3375", "18603428680"));
        parkingInfoList.add(new ParkingInfo("E-702", "王志杰", "27#", "晋D7305F", "13467053356"));
        parkingInfoList.add(new ParkingInfo("C-1-402", "杨彦芳", "28#", "京N6W722", "13301156775"));
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
        // 这里可以使用SharedPreferences或SQLite数据库来保存数据
        // 示例使用SharedPreferences
        getSharedPreferences("parking_info", MODE_PRIVATE)
                .edit()
                .putString(parkingInfo.getLicensePlate() + "_floor", parkingInfo.getFloor())
                .putString(parkingInfo.getLicensePlate() + "_name", parkingInfo.getName())
                .putString(parkingInfo.getLicensePlate() + "_parkingSpot", parkingInfo.getParkingSpot())
                .putString(parkingInfo.getLicensePlate() + "_phone", parkingInfo.getPhone())
                .apply();
    }
}