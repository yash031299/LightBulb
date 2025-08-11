package com.example.services;

import com.example.model.LightBulb;
import java.util.List;


public interface LightBulbService {

    List<LightBulb> getAllBulbs();

    LightBulb getBulbById(Long id);

    LightBulb addBulb(LightBulb bulb);

    LightBulb updateBulb(Long id, LightBulb updatedBulb);

    void deleteBulb(Long id);
}
