package com.chat.application.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userName;
    private String email;
    private String password;
    private String phoneNumber;
}
