package com.h5game.threebody.model;

import java.util.List;

import com.h5game.threebody.entity.Body;

public class SimulationState {
    private final List<Body> bodies;
    private final long elapsedTime;
    private final double gravityConstant;
    private final double timeStep;
    private final double dampingFactor;

    public SimulationState(List<Body> bodies, long elapsedTime, double gravityConstant, 
                          double timeStep, double dampingFactor) {
        this.bodies = bodies;
        this.elapsedTime = elapsedTime;
        this.gravityConstant = gravityConstant;
        this.timeStep = timeStep;
        this.dampingFactor = dampingFactor;
    }

    public List<Body> getBodies() { return bodies; }
    public long getElapsedTime() { return elapsedTime; }
    public double getGravityConstant() { return gravityConstant; }
    public double getTimeStep() { return timeStep; }
    public double getDampingFactor() { return dampingFactor; }
}
