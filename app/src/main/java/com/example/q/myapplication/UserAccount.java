package com.example.q.myapplication;

import android.app.Application;

/**
 * Created by q on 2017-01-02.
 */

public class UserAccount extends Application {
      private String mUserName;

    public String getGlobalVarValue() {
        return mUserName;
    }

    public void setGlobalVarValue(String str) {
        mUserName = str;
    }
}
