package com.example.talks.modal;

public class Contacts {

    public String name,status,images;

    public Contacts(){

    }

    public Contacts(String name, String status, String images) {
        this.name = name;
        this.status = status;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return images;
    }

    public void setImage(String images) {
        this.images = images;
    }
}
