package com.kjh.seoulapp;

import android.app.Application;
import com.tsengvn.typekit.Typekit;

public class ApplicationFont extends Application {
    @Override public void onCreate() {
        super.onCreate();
        // 폰트 정의
        Typekit.getInstance().addNormal(Typekit.createFromAsset(this, "NanumSquareR.ttf")).addBold(Typekit.createFromAsset(this, "NanumSquareB.ttf"));
    }
}