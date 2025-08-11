package com.example.controller;

import com.example.model.LightBulb;
import com.example.services.LightBulbService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bulbs")
public class LightBulbController {


    private final LightBulbService lightBulbService;

    @Autowired
    public LightBulbController(LightBulbService lightBulbService) {
        this.lightBulbService = lightBulbService;
    }

    @GetMapping
    public List<LightBulb> getAllBulbs() {
        return lightBulbService.getAllBulbs();
    }

    @GetMapping("/{id}")
    public LightBulb getBulbById(@PathVariable Long id) {
        return lightBulbService.getBulbById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LightBulb> addBulb(@Valid @RequestBody LightBulb lightBulb) {
        LightBulb createdBulb = lightBulbService.addBulb(lightBulb);
        return ResponseEntity.created(URI.create("/bulbs/" + createdBulb.getId())).body(createdBulb);
    }

    @PutMapping("/{id}")
    public LightBulb updateBulb(@PathVariable Long id, @Valid @RequestBody LightBulb updatedBulb) {
        if (updatedBulb.getId() != null && !updatedBulb.getId().equals(id)) {
            throw new IllegalArgumentException("ID in URL does not match ID in request body");
        }
        return lightBulbService.updateBulb(id, updatedBulb);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBulb(@PathVariable Long id) {
        lightBulbService.deleteBulb(id);
    }
}
