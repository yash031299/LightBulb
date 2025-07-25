package com.example.controller;

import com.example.model.LightBulb;
import jakarta.validation.Valid;
import com.example.services.LightBulbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bulbs")
public class LightBulbController {

    private final LightBulbService lightBulbService;

    @Autowired
    public LightBulbController(LightBulbService lightBulbService) {
        this.lightBulbService = lightBulbService;
    }

    @PostMapping
    public LightBulb addBulb(@Valid @RequestBody LightBulb lightBulb) {
        return lightBulbService.addBulb(lightBulb);
    }

    @GetMapping
    public List<LightBulb> getAllBulbs() {
        return lightBulbService.getAllBulbs();
    }

    @GetMapping("/{id}")
    public LightBulb getBulbById(@PathVariable Long id) {
       return lightBulbService.getBulbById(id);
    }

    @PutMapping("/{id}")
    public LightBulb updateBulb(@PathVariable Long id, @Valid @RequestBody LightBulb updatedBulb) {
       return lightBulbService.updateBulb(id, updatedBulb);
    }

    @DeleteMapping("/{id}")
    public void deleteBulb(@PathVariable Long id) {
        lightBulbService.deleteBulb(id);
    }
}
