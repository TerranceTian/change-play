package com.h5game.threebody.entity;

public class Body {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private double mass;
    private String color;
    private double radius;

    public Body(double x, double y, double mass, String color) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.mass = mass;
        this.color = color;
        this.radius = 10;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getMass() { return mass; }
    public String getColor() { return color; }
    public double getRadius() { return radius; }

    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }

    public double getSpeed() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    public String getPosition() {
        return String.format("(%.0f, %.0f)", x, y);
    }
}
