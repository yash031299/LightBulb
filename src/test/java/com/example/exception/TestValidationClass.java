package com.example.exception;

import jakarta.validation.constraints.NotNull;

public class TestValidationClass {
    
    @NotNull(message = "Name cannot be null")
    private String name;
    
    // Getter and setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
