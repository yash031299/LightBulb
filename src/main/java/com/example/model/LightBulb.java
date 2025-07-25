package com.example.model;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class LightBulb {

    private Long id;

    @NotBlank(message = "Name ids Required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    @Min(value = 1, message = "Wattage must be at least 1")
    private int wattage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWattage() {
        return wattage;
    }

    public void setWattage(int wattage) {
        this.wattage = wattage;
    }
}
