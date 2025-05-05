package com.example.login.Config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {

    private final DelegatingPasswordEncoder delegatingPasswordEncoder;
    private final BCryptPasswordEncoder bcryptEncoder;

    public CustomPasswordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        bcryptEncoder = new BCryptPasswordEncoder();
        encoders.put("bcrypt", bcryptEncoder);
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        
        // Default to bcrypt for new passwords
        this.delegatingPasswordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        
        // This is the critical line that fixes the error:
        // Set default password encoder for passwords without prefix
        this.delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegatingPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        
        // Handle existing {noop} passwords
        if (encodedPassword.startsWith("{noop}")) {
            return rawPassword.toString().equals(encodedPassword.substring(6));
        }
        
        // Handle passwords with standard format prefixes using delegating encoder
        if (encodedPassword.startsWith("{")) {
            return delegatingPasswordEncoder.matches(rawPassword, encodedPassword);
        }
        
        // Handle passwords without any prefix - try plain first
        if (rawPassword.toString().equals(encodedPassword)) {
            return true;
        }
        
        // Try bcrypt as a fallback for non-prefixed hashed passwords
        try {
            return bcryptEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            // If bcrypt fails, return false
            return false;
        }
    }
}