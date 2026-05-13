package com.centropsicologico.sistema.dto;

public class ReminderDTO {

    private String title;
    private String message;
    private String type;

    public ReminderDTO() {
    }

    public ReminderDTO(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}