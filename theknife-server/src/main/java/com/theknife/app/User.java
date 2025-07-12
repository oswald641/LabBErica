package com.theknife.app;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class User {
    //https://dzone.com/articles/secure-password-hashing-in-java
    public static String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    public static boolean verifyPassword(String inputPassword, String storedHash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(inputPassword, storedHash);
    }
}
