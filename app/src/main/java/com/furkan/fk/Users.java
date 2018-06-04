package com.furkan.fk;

/**
 * Created by furkan on 25.05.2018.
 */

public class Users {

    private String display_name;
    private String image;
    private String status;
    private String thumb_image;

    public Users(String name, String image, String status, String thumb_image) {
        this.display_name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public Users(String name, String image, String status) {
        this.display_name = name;
        this.image = image;
        this.status = status;
    }

    public Users() {
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
