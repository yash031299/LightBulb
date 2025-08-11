package com.example.services;

import com.example.exception.ResourceNotFoundException;
import com.example.model.LightBulb;
import com.example.repo.LightBulbRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightBulbServiceImpl implements LightBulbService {

    private static final Logger logger = LoggerFactory.getLogger(LightBulbServiceImpl.class);

    private final LightBulbRepository lightBulbRepository;

    @Autowired
    public LightBulbServiceImpl(LightBulbRepository lightBulbRepository) {
        this.lightBulbRepository = lightBulbRepository;
    }

    @Override
    public LightBulb addBulb(LightBulb bulb) {
        if (bulb == null) {
            logger.error("Attempted to add a null bulb");
            throw new IllegalArgumentException("LightBulb cannot be null");
        }
        
        logger.info("Adding new bulb: {}", bulb);
        try {
            LightBulb savedBulb = lightBulbRepository.save(bulb);
            logger.info("Successfully added bulb with ID: {}", savedBulb.getId());
            return savedBulb;
        } catch (Exception e) {
            logger.error("Failed to add bulb: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add bulb: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LightBulb> getAllBulbs() {
        logger.info("Fetching all bulbs...");
        try {
            List<LightBulb> bulbs = lightBulbRepository.findAll();
            logger.info("Successfully retrieved {} bulbs", bulbs.size());
            return bulbs;
        } catch (Exception e) {
            logger.error("Failed to retrieve bulbs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve bulbs: " + e.getMessage(), e);
        }
    }

    @Override
    public LightBulb getBulbById(Long id) {
        if (id == null) {
            logger.error("Attempted to get bulb with null ID");
            throw new IllegalArgumentException("Bulb ID cannot be null");
        }
        
        logger.info("Fetching bulb with ID: {}", id);
        try {
            return lightBulbRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("LightBulb", "id", id));
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw ResourceNotFoundException as is
        } catch (Exception e) {
            logger.error("Failed to fetch bulb with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch bulb: " + e.getMessage(), e);
        }
    }

    @Override
    public LightBulb updateBulb(Long id, LightBulb updatedBulb) {
        if (id == null) {
            logger.error("Attempted to update bulb with null ID");
            throw new IllegalArgumentException("Bulb ID cannot be null");
        }
        if (updatedBulb == null) {
            logger.error("Attempted to update with null bulb data");
            throw new IllegalArgumentException("Bulb data cannot be null");
        }
        
        logger.info("Updating bulb with ID: {}", id);
        try {
            return lightBulbRepository.findById(id).map(existingBulb -> {
                logger.debug("Found existing bulb: {}", existingBulb);
                existingBulb.setName(updatedBulb.getName());
                existingBulb.setType(updatedBulb.getType());
                existingBulb.setWattage(updatedBulb.getWattage());
                
                LightBulb updated = lightBulbRepository.save(existingBulb);
                logger.info("Successfully updated bulb with ID: {}", id);
                return updated;
            }).orElseThrow(() -> new ResourceNotFoundException("LightBulb", "id", id));
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw ResourceNotFoundException as is
        } catch (Exception e) {
            logger.error("Failed to update bulb with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update bulb: " + e.getMessage(), e);
        }
    }


    @Override
    public void deleteBulb(Long id) {
        if (id == null) {
            logger.error("Attempted to delete bulb with null ID");
            throw new IllegalArgumentException("Bulb ID cannot be null");
        }
        
        logger.info("Deleting bulb with ID: {}", id);
        try {
            if (lightBulbRepository.findById(id).isEmpty()) {
                throw new ResourceNotFoundException("LightBulb", "id", id);
            }
            lightBulbRepository.deleteById(id);
            logger.info("Successfully deleted bulb with ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw ResourceNotFoundException as is
        } catch (Exception e) {
            logger.error("Failed to delete bulb with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete bulb: " + e.getMessage(), e);
        }
    }
}
