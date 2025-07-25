package com.example.services;

import com.example.model.LightBulb;
import com.example.repo.LightBulbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class LightBulbServiceImpl implements LightBulbService {

    private final LightBulbRepository repository;

    @Autowired
    public LightBulbServiceImpl(LightBulbRepository repository) {
        this.repository = repository;
    }
    private static final Logger logger = LoggerFactory.getLogger(LightBulbServiceImpl.class);

    @Override
    public LightBulb addBulb(LightBulb bulb) {
        logger.info("Adding new bulb: {}", bulb);
        return repository.save(bulb);
    }

    @Override
    public List<LightBulb> getAllBulbs() {
        logger.info("Getting all Bulbs...");
        return repository.findAll();
    }

    @Override
    public LightBulb getBulbById(Long id) {
        logger.info("Getting the bulb by BulbID = {}",id );
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Bulb not found with id: " + id));
    }

    @Override
    public LightBulb updateBulb(Long id, LightBulb updatedBulb) {
        LightBulb existingBulb = getBulbById(id);
        existingBulb.setName(updatedBulb.getName());
        existingBulb.setType(updatedBulb.getType());
        existingBulb.setWattage(updatedBulb.getWattage());

        logger.info("Bulb Updated with the BulbId = {}, with new entry = {}", id, updatedBulb);
        return repository.save(existingBulb);
    }

    @Override
    public void deleteBulb(Long id) {

        logger.info("Deleting Bulb with the BulbID = {}", id);
        repository.deleteById(id);
    }
}
