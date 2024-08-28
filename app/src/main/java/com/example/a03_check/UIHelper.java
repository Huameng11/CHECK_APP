package com.example.a03_check;

import android.widget.TextView;
import android.view.View;
public class UIHelper {

    public static void updateResultTextView(TextView resultTextView,TextView phoneTextView, ParkingInfo parkingInfo) {
        if (parkingInfo != null) {
            resultTextView.setText(
                    "车位号: " + parkingInfo.getParkingSpot() + "\n" +
                            "姓名: " + parkingInfo.getName() + "\n" +
                            "车牌号: " + parkingInfo.getLicensePlate()
            );
            phoneTextView.setText("电话: " + parkingInfo.getPhone());
            phoneTextView.setVisibility(View.VISIBLE);
        } else {
            resultTextView.setText("未找到匹配的信息");
            phoneTextView.setVisibility(View.GONE);
        }
    }
}