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
    private TextToSpeechHelper textToSpeechHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ParkingDatabaseHelper(this);
        textToSpeechHelper = new TextToSpeechHelper(this);

        dbHelper.initializeDefaultData();
        parkingInfoList = dbHelper.getAllParkingInfo();

        inputPlate = findViewById(R.id.input_plate);
        resultTextView = findViewById(R.id.result);
        suggestionList = findViewById(R.id.suggestion_list);

        suggestions = new ArrayList<>();
        suggestionAdapter = new SuggestionAdapter(suggestions, this);
        suggestionList.setLayoutManager(new LinearLayoutManager(this));
        suggestionList.setAdapter(suggestionAdapter);

        setupTextWatcher();
        setupEditActivityResultLauncher();
        setupCheckButton();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeechHelper != null) {
            textToSpeechHelper.shutdown();
        }
        super.onDestroy();
    }

    private void setupTextWatcher() {
        inputPlate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                updateSuggestions(query);
                currentParkingInfo = findParkingInfoByLicensePlate(query);
                UIHelper.updateResultTextView(resultTextView, currentParkingInfo);
                if (currentParkingInfo != null) {
                    textToSpeechHelper.speak("车位号 " + currentParkingInfo.getParkingSpot());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setupEditActivityResultLauncher() {
        editActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            updateCurrentParkingInfoFromIntent(data);
                            UIHelper.updateResultTextView(resultTextView, currentParkingInfo);
                            dbHelper.updateParkingInfo(currentParkingInfo);
                        }
                    }
                }
        );
    }

    private void setupCheckButton() {
        findViewById(R.id.check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddParkingInfoActivity.class);
                startActivity(intent);
            }
        });
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