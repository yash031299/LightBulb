package com.example.controller;

import com.example.model.LightBulb;
import com.example.repo.LightBulbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bulbs")
public class LightBulbController {

    @Autowired
    private LightBulbRepository repository;

    @PostMapping
    public LightBulb addBulb(@RequestBody LightBulb lightBulb) {
        return repository.save(lightBulb);
    }

    @GetMapping
    public List<LightBulb> getAllBulbs() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public LightBulb getBulbById(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Bulb not found with id: " + id));
    }

    @PutMapping("/{id}")
    public LightBulb updateBulb(@PathVariable Long id, @RequestBody LightBulb updatedBulb) {
        LightBulb existingBulb = repository.findById(id).orElseThrow(() -> new RuntimeException("Bulb not found with id: " + id));
        existingBulb.setName(updatedBulb.getName());
        existingBulb.setType(updatedBulb.getType());
        existingBulb.setWattage(updatedBulb.getWattage());
        return repository.save(existingBulb);
    }

    @DeleteMapping("/{id}")
    public void deleteBulb(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
