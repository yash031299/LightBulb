package com.example.services;

import com.example.model.LightBulb;
import java.util.List;

public interface LightBulbService {
    LightBulb addBulb(LightBulb bulb);
    List<LightBulb> getAllBulbs();
    LightBulb getBulbById(Long id);
    LightBulb updateBulb(Long id, LightBulb updatedBulb);
    void deleteBulb(Long id);
}
