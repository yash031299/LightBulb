package com.example.model;

import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LightBulbValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testInvalidLightBulb() {
        LightBulb bulb = new LightBulb(); // no fields set
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidLightBulb() {
        LightBulb bulb = new LightBulb();
        bulb.setName("Valid");
        bulb.setType("LED");
        bulb.setWattage(10);
        Set<ConstraintViolation<LightBulb>> violations = validator.validate(bulb);
        assertTrue(violations.isEmpty());
    }
}
