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
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import android.net.Uri;
public class MainActivity extends AppCompatActivity implements SuggestionAdapter.OnSuggestionClickListener {

    // 存储停车信息的列表
    private List<ParkingInfo> parkingInfoList;
    // 输入车牌号的编辑框
    private EditText inputPlate;
    // 显示联系电话的文本视图
    private TextView phoneTextView;
    // 显示结果的文本视图
    private TextView resultTextView;
    // 显示建议列表的RecyclerView
    private RecyclerView suggestionList;
    // 建议列表的适配器
    private SuggestionAdapter suggestionAdapter;
    // 存储建议的列表
    private List<String> suggestions;
    // 当前选中的停车信息
    private ParkingInfo currentParkingInfo;
    // 用于启动编辑活动的ActivityResultLauncher
    private ActivityResultLauncher<Intent> editActivityResultLauncher;
    // 数据库帮助类
    private ParkingDatabaseHelper dbHelper;
    // 文本转语音帮助类
    private TextToSpeechHelper textToSpeechHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据库帮助类
        dbHelper = new ParkingDatabaseHelper(this);
        // 初始化文本转语音帮助类
        textToSpeechHelper = new TextToSpeechHelper(this);

        // 初始化默认数据
        dbHelper.initializeDefaultData();
        // 从数据库获取所有停车信息
        parkingInfoList = dbHelper.getAllParkingInfo();

        // 初始化UI组件
        inputPlate = findViewById(R.id.input_plate);
        resultTextView = findViewById(R.id.result);
        phoneTextView = findViewById(R.id.phone);
        suggestionList = findViewById(R.id.suggestion_list);

        // 初始化建议列表
        suggestions = new ArrayList<>();
        // 初始化建议列表的适配器
        suggestionAdapter = new SuggestionAdapter(suggestions, this);
        // 设置RecyclerView的布局管理器
        suggestionList.setLayoutManager(new LinearLayoutManager(this));
        // 设置RecyclerView的适配器
        suggestionList.setAdapter(suggestionAdapter);

        // 设置文本变化监听器
        setupTextWatcher();
        // 设置编辑活动结果启动器
        setupEditActivityResultLauncher();
        // 设置检查按钮的点击监听器
        setupCheckButton();
        // 设置电话号码点击事件监听器
        setupPhoneClickListener();
    }

    @Override
    protected void onDestroy() {
        // 关闭文本转语音
        if (textToSpeechHelper != null) {
            textToSpeechHelper.shutdown();
        }
        super.onDestroy();
    }

    // 设置文本变化监听器
    private void setupTextWatcher() {
        inputPlate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 获取查询字符串
                String query = charSequence.toString();
                // 更新建议列表
                updateSuggestions(query);
                // 根据查询字符串查找停车信息
                currentParkingInfo = findParkingInfoByLicensePlate(query);
                // 更新结果文本视图
                UIHelper.updateResultTextView(resultTextView, phoneTextView,currentParkingInfo);
                // 如果找到停车信息，则使用文本转语音读出车位号
                if (currentParkingInfo != null) {
                    textToSpeechHelper.speak("车位号 " + currentParkingInfo.getParkingSpot());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // 设置编辑活动结果启动器
    private void setupEditActivityResultLauncher() {
        editActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // 如果结果码为RESULT_OK且数据不为空
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            // 从意图中更新当前停车信息
                            updateCurrentParkingInfoFromIntent(data);
                            // 更新结果文本视图
                            UIHelper.updateResultTextView(resultTextView,phoneTextView, currentParkingInfo);
                            // 更新数据库中的停车信息
                            dbHelper.updateParkingInfo(currentParkingInfo);
                        }
                    }
                }
        );
    }

    // 设置检查按钮的点击监听器
    private void setupCheckButton() {
        findViewById(R.id.check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动添加停车信息活动
                Intent intent = new Intent(MainActivity.this, AddParkingInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    // 根据车牌号查找停车信息
    private ParkingInfo findParkingInfoByLicensePlate(String licensePlate) {
        for (ParkingInfo parkingInfo : parkingInfoList) {
            if (parkingInfo.getLicensePlate().equals(licensePlate)) {
                return parkingInfo;
            }
        }
        return null;
    }

    // 更新建议列表
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

    // 处理建议点击事件
    @Override
    public void onSuggestionClick(String suggestion) {
        inputPlate.setText(suggestion);
        inputPlate.setSelection(suggestion.length());
    }
    private void setupPhoneClickListener() {
        // 为 phoneTextView 设置点击事件监听器
        phoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查当前的停车信息是否为空
                if (currentParkingInfo != null) {
                    // 获取当前停车信息的电话号码
                    String phoneNumber = currentParkingInfo.getPhone();

                    // 创建一个拨号意图 (Intent)
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);

                    // 设置意图的数据为电话号码
                    dialIntent.setData(Uri.parse("tel:" + phoneNumber));

                    // 启动意图，打开系统的拨号界面
                    startActivity(dialIntent);
                }
            }
        });
    }
    // 处理结果点击事件
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

    // 从意图中更新当前停车信息
    private void updateCurrentParkingInfoFromIntent(Intent data) {
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
    }
}