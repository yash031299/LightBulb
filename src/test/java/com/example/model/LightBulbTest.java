package com.example.model;

import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LightBulbValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_whenLightBulbIsValid_shouldPassValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Valid");
        bulb.setType("LED");
        bulb.setWattage(10);
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_whenLightBulbIsInvalid_shouldFailValidation() {
        // Arrange
        LightBulb bulb = new LightBulb(); // no fields set
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_whenNameIsBlank_shouldFailValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setType("LED");
        bulb.setWattage(10);
        bulb.setName("");
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void validate_whenTypeIsBlank_shouldFailValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb");
        bulb.setWattage(10);
        bulb.setType("");
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("type")));
    }

    @Test
    void validate_whenWattageIsZero_shouldFailValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb");
        bulb.setType("LED");
        bulb.setWattage(0);
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("wattage")));
    }

    @Test
    void validate_whenWattageIsNegative_shouldFailValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb");
        bulb.setType("LED");
        bulb.setWattage(-5);
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("wattage")));
    }

    @Test
    void validate_whenWattageIsOne_shouldPassValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb");
        bulb.setType("LED");
        bulb.setWattage(1);
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_whenWattageIsVeryLarge_shouldPassValidation() {
        // Arrange
        LightBulb bulb = new LightBulb();
        bulb.setName("Bulb");
        bulb.setType("LED");
        bulb.setWattage(Integer.MAX_VALUE);
        // Act
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        // Assert
        assertTrue(violations.isEmpty());
    }
}
