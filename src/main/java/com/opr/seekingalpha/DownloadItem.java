package com.opr.seekingalpha;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mark on 12/17/14.
 */
public class DownloadItem {
    private String url;
    private String ticker;
    private Date date;
    private String transcriptHtml;

    SimpleDateFormat format = new SimpleDateFormat("yyyy");

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTranscriptHtml() {
        return transcriptHtml;
    }

    public void setTranscriptHtml(String transcriptHtml) {
        this.transcriptHtml = transcriptHtml;
    }

    public String getYear() {
        if (date == null) {
            return "";
        }
        return format.format(date);
    }
}
