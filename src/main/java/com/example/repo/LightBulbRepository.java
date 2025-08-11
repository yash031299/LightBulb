package com.example.repo;

import com.example.model.LightBulb;

import java.util.List;
import java.util.Optional;


public interface LightBulbRepository {
    List<LightBulb> findAll();
    Optional<LightBulb> findById(Long id);
    LightBulb save(LightBulb bulb);
    void deleteById(Long id);
}
