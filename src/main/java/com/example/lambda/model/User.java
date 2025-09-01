package com.example.lambda.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * User entity model
 */
public class User {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("score")
    private Integer score;
    
    @JsonProperty("wallet_address")
    private String walletAddress;
    
    @JsonProperty("wallet_type")
    private String walletType;
    
    @JsonProperty("bind_time")
    private LocalDateTime bindTime;
    


    // Default constructor
    public User() {}

    // Constructor with all fields
    public User(Integer id, String username, String email, String password, 
                Integer score, String walletAddress, String walletType, 
                LocalDateTime bindTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.score = score;
        this.walletAddress = walletAddress;
        this.walletType = walletType;
        this.bindTime = bindTime;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public LocalDateTime getBindTime() {
        return bindTime;
    }

    public void setBindTime(LocalDateTime bindTime) {
        this.bindTime = bindTime;
    }



    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", score=" + score +
                ", walletAddress='" + walletAddress + '\'' +
                ", walletType='" + walletType + '\'' +
                ", bindTime=" + bindTime +
                '}';
    }
}
