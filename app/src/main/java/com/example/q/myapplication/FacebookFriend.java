package com.example.q.myapplication;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by q on 2017-01-01.
 */

public class FacebookFriend {
    String facebookId;
    String name;
    String pictureUrl;
    boolean available;
    boolean isValid;

    public enum Type {AVAILABLE};

    public FacebookFriend(JSONObject jsonObject, Type type) {
        //
        //Parse the Facebook Data from the JSON object.
        //
        try {
            //parse /me/friends
            this.facebookId = jsonObject.getString("id");
            this.name = jsonObject.getString("name");
            this.available = true;
            this.pictureUrl = "";
            isValid = true;
        } catch (JSONException e) {
            Log.w("#", "Warnings - unable to process FB JSON: " + e.getLocalizedMessage());
        }
    }
}
