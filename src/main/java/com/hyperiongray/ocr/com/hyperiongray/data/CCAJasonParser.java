package com.hyperiongray.ocr.com.hyperiongray.data;

import com.google.gson.Gson;

public class CCAJasonParser {
    String [] image_urls;

    public String[] getImageUrls(String jsonInput) {
        Gson gson = new Gson();
        CCAJasonParser instance = gson.fromJson(jsonInput, CCAJasonParser.class);
        return instance.image_urls;
    }
}
