package com.example.a03_check;

import android.widget.TextView;

public class UIHelper {

    public static void updateResultTextView(TextView resultTextView, ParkingInfo parkingInfo) {
        if (parkingInfo != null) {
            resultTextView.setText(
                    "车位号: " + parkingInfo.getParkingSpot() + "\n" +
                            "姓名: " + parkingInfo.getName() + "\n" +
                            "车牌号: " + parkingInfo.getLicensePlate() + "\n" +
                            "电话: " + parkingInfo.getPhone()
            );
        } else {
            resultTextView.setText("未找到匹配的信息");
        }
    }
}