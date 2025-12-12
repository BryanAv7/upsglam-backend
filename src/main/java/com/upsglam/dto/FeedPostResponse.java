package com.upsglam.dto;
//test
import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedPostResponse {

    private String id;

    @JsonProperty("user_uid")
    private String userUid;

    private String caption;

    @JsonProperty("public_url")
    private String publicUrl;

    @JsonProperty("created_at")
    private String createdAt;

    // NO VIENE DE SUPABASE, LO AGREGAMOS NOSOTROS
    private String profileImageUrl;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
