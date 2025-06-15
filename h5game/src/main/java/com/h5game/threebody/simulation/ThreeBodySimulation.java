package com.h5game.threebody.simulation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.h5game.threebody.entity.Body;
import com.h5game.threebody.model.SimulationState;

@Component
@EnableScheduling
public class ThreeBodySimulation {
    private final SimpMessagingTemplate messagingTemplate;
    private static final double G = 50;  // Gravitational constant
    private static final double dt = 0.05;  // Time step
    private static final double DAMPING = 0.995;  // Damping factor
    private final long startTime;
    private final List<Body> bodies;
    private boolean isInitialized = false;

    public ThreeBodySimulation(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.startTime = System.currentTimeMillis();
        this.bodies = new ArrayList<>();
    }

    public void initialize() {
        if (!isInitialized) {
            bodies.clear();
            bodies.add(new Body(200, 400, 1000, "red"));
            bodies.add(new Body(600, 400, 1000, "yellow"));
            bodies.add(new Body(400, 200, 1000, "green"));
            isInitialized = true;
        }
    }

    @Scheduled(fixedRate = 16)  // 约60fps的更新率
    public void updateAndBroadcast() {
        if (!isInitialized) {
            return;
        }

        updatePositions();
        SimulationState state = new SimulationState(
            bodies,
            System.currentTimeMillis() - startTime,
            G,
            dt,
            DAMPING
        );
        messagingTemplate.convertAndSend("/topic/simulation", state);
    }

    private void updatePositions() {
        // 计算引力和更新速度
        for (int i = 0; i < bodies.size(); i++) {
            Body body1 = bodies.get(i);
            double totalAx = 0;
            double totalAy = 0;

            for (int j = 0; j < bodies.size(); j++) {
                if (i != j) {
                    Body body2 = bodies.get(j);
                    double dx = body2.getX() - body1.getX();
                    double dy = body2.getY() - body1.getY();
                    double r = Math.sqrt(dx * dx + dy * dy);
                    double F = (G * body1.getMass() * body2.getMass()) / (r * r);
                    totalAx += (F * dx) / (r * body1.getMass());
                    totalAy += (F * dy) / (r * body1.getMass());
                }
            }

            body1.setVx(body1.getVx() + totalAx * dt);
            body1.setVy(body1.getVy() + totalAy * dt);

            // 应用阻尼
            body1.setVx(body1.getVx() * DAMPING);
            body1.setVy(body1.getVy() * DAMPING);

            // 限制最大速度
            double maxVelocity = 300;
            double currentVelocity = Math.sqrt(body1.getVx() * body1.getVx() + body1.getVy() * body1.getVy());
            if (currentVelocity > maxVelocity) {
                double scale = maxVelocity / currentVelocity;
                body1.setVx(body1.getVx() * scale);
                body1.setVy(body1.getVy() * scale);
            }
        }

        // 更新位置并处理边界
        for (Body body : bodies) {
            body.setX(body.getX() + body.getVx() * dt);
            body.setY(body.getY() + body.getVy() * dt);

            // 边界检查
            if (body.getX() < body.getRadius()) {
                body.setX(body.getRadius());
                body.setVx(body.getVx() * -0.8);
            } else if (body.getX() > 800 - body.getRadius()) {
                body.setX(800 - body.getRadius());
                body.setVx(body.getVx() * -0.8);
            }

            if (body.getY() < body.getRadius()) {
                body.setY(body.getRadius());
                body.setVy(body.getVy() * -0.8);
            } else if (body.getY() > 800 - body.getRadius()) {
                body.setY(800 - body.getRadius());
                body.setVy(body.getVy() * -0.8);
            }
        }
    }
}
