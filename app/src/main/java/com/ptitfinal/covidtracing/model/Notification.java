package com.ptitfinal.covidtracing.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Notification {
    String ID;
    String content;
    String title;
    public Notification() {
    }

    public Notification(String content, String title) {
        this.content = content;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

