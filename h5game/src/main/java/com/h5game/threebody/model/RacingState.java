package com.h5game.threebody.model;

import java.util.List;
import java.util.Map;

import com.h5game.threebody.entity.Bullet;
import com.h5game.threebody.entity.Car;

public class RacingState {
    private final Map<String, Car> cars;
    private final List<Bullet> bullets;
    private final long timestamp;

    public RacingState(Map<String, Car> cars, List<Bullet> bullets, long timestamp) {
        this.cars = cars;
        this.bullets = bullets;
        this.timestamp = timestamp;
    }

    public Map<String, Car> getCars() { return cars; }
    public List<Bullet> getBullets() { return bullets; }
    public long getTimestamp() { return timestamp; }
}
