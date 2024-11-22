package com.example.agrilens;

public class UserProfile {
    private String name;
    private String password;
    private String profileImageUrl;

    public UserProfile(String name, String password, String profileImageUrl) {
        this.name = name;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}

