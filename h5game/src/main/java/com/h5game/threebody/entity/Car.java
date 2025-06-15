package com.h5game.threebody.entity;

import java.util.UUID;

public class Car {
    private String id;
    private String username;
    private double x;
    private double y;
    private double angle;  // 角度，0度表示向右
    private double speed;
    private String color;
    private int health;
    private long lastShotTime;

    public Car(String username, double x, double y, String color) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.speed = 0;
        this.color = color;
        this.health = 20;
        this.lastShotTime = 0;
    }

    public void move(double dt) {
        x += Math.cos(Math.toRadians(angle)) * speed * dt;
        y += Math.sin(Math.toRadians(angle)) * speed * dt;
    }

    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime > 500; // 0.5秒冷却时间
    }

    public void shoot() {
        lastShotTime = System.currentTimeMillis();
    }

    public void damage() {
        health--;
    }

    public boolean isAlive() {
        return health > 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public double getSpeed() { return speed; }
    public String getColor() { return color; }
    public int getHealth() { return health; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setAngle(double angle) { this.angle = angle; }
    public void setSpeed(double speed) { this.speed = speed; }
}
