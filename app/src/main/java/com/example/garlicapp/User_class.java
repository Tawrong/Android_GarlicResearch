package com.example.garlicapp;

public class User_class {
    private String Email;
    private String Filename;
    private String ImageSource;

    // Required default constructor for Firebase
    public User_class() {
    }

    public User_class(String email, String filename, String imageSource) {
        this.Email = email;
        this.Filename = filename;
        this.ImageSource = imageSource;
    }



    // Getters and setters (optional, but often useful)
    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFilename() {
        return Filename;
    }

    public void setFilename(String filename) {
        Filename = filename;
    }

    public String getImageSource() {
        return ImageSource;
    }

    public void setImageSource(String imageSource) {
        ImageSource = imageSource;
    }
}