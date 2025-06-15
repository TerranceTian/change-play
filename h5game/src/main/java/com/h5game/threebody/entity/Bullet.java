package com.h5game.threebody.entity;

public class Bullet {
    private String shooterId;
    private double x;
    private double y;
    private double angle;
    private static final double SPEED = 300;
    private boolean active;

    public Bullet(String shooterId, double x, double y, double angle) {
        this.shooterId = shooterId;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.active = true;
    }

    public void move(double dt) {
        x += Math.cos(Math.toRadians(angle)) * SPEED * dt;
        y += Math.sin(Math.toRadians(angle)) * SPEED * dt;
    }

    public String getShooterId() { return shooterId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
}
