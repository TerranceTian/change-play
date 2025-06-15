package com.h5game.threebody.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.h5game.threebody.entity.Bullet;
import com.h5game.threebody.entity.Car;
import com.h5game.threebody.model.ChatMessage;
import com.h5game.threebody.model.RacingState;

@Component
public class RacingSimulation {
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Car> cars = new ConcurrentHashMap<>();
    private final List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private static final double ACCELERATION = 200.0;
    private static final double MAX_SPEED = 300.0;
    private static final double ROTATION_SPEED = 180.0;
    private static final String[] COLORS = {"#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF"};
    private static final double CAR_WIDTH = 40;
    private static final double CAR_HEIGHT = 20;
    private boolean isInitialized = false;
    private final Random random = new Random();

    public RacingSimulation(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void initialize() {
        if (!isInitialized) {
            isInitialized = true;
        }
    }

    public void addPlayer(String username) {
        if (!cars.containsKey(username)) {
            double x = 100 + random.nextDouble() * 600;
            double y = 100 + random.nextDouble() * 600;
            String color = COLORS[cars.size() % COLORS.length];
            Car car = new Car(username, x, y, color);
            cars.put(username, car);
            
            // 发送玩家加入消息
            messagingTemplate.convertAndSend("/topic/messages", 
                new ChatMessage("系统", username + " 加入了赛车联赛", true));
        }
    }

    public void handleInput(String username, String input) {
        Car car = cars.get(username);
        if (car != null && car.isAlive()) {
            double dt = 0.016; // 假设16ms的帧时间

            switch (input) {
                case "UP":
                case "W":
                    double newSpeed = Math.min(car.getSpeed() + ACCELERATION * dt, MAX_SPEED);
                    // 检查移动后是否会超出边界
                    double nextX = car.getX() + Math.cos(Math.toRadians(car.getAngle())) * newSpeed * dt;
                    double nextY = car.getY() + Math.sin(Math.toRadians(car.getAngle())) * newSpeed * dt;
                    if (isValidPosition(nextX, nextY)) {
                        car.setSpeed(newSpeed);
                    } else {
                        car.setSpeed(0); // 如果会超出边界，停止移动
                    }
                    break;
                case "DOWN":
                case "S":
                    newSpeed = Math.max(car.getSpeed() - ACCELERATION * dt, -MAX_SPEED/2);
                    nextX = car.getX() + Math.cos(Math.toRadians(car.getAngle())) * newSpeed * dt;
                    nextY = car.getY() + Math.sin(Math.toRadians(car.getAngle())) * newSpeed * dt;
                    if (isValidPosition(nextX, nextY)) {
                        car.setSpeed(newSpeed);
                    } else {
                        car.setSpeed(0);
                    }
                    break;
                case "LEFT":
                case "A":
                    car.setAngle(car.getAngle() - ROTATION_SPEED * dt);
                    break;
                case "RIGHT":
                case "D":
                    car.setAngle(car.getAngle() + ROTATION_SPEED * dt);
                    break;
                case "SPACE":
                    if (car.canShoot()) {
                        car.shoot();
                        bullets.add(new Bullet(car.getId(), car.getX(), car.getY(), car.getAngle()));
                    }
                    break;
            }
        }
    }

    private boolean isValidPosition(double x, double y) {
        // 考虑车辆尺寸的边界检查
        return x >= CAR_WIDTH/2 && 
               x <= 800 - CAR_WIDTH/2 && 
               y >= CAR_HEIGHT/2 && 
               y <= 800 - CAR_HEIGHT/2;
    }

    @Scheduled(fixedRate = 16)
    public void update() {
        if (!isInitialized) return;

        double dt = 0.016;

        // 更新车辆位置
        for (Car car : cars.values()) {
            if (car.isAlive()) {
                // 计算下一个位置
                double nextX = car.getX() + Math.cos(Math.toRadians(car.getAngle())) * car.getSpeed() * dt;
                double nextY = car.getY() + Math.sin(Math.toRadians(car.getAngle())) * car.getSpeed() * dt;

                // 只有在有效位置时才更新
                if (isValidPosition(nextX, nextY)) {
                    car.setX(nextX);
                    car.setY(nextY);
                } else {
                    // 如果位置无效，停止移动
                    car.setSpeed(0);
                }

                // 摩擦力减速
                car.setSpeed(car.getSpeed() * 0.99);
            }
        }        // 更新子弹位置和碰撞检测
        List<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            // 预先计算下一个位置
            double nextX = bullet.getX() + Math.cos(Math.toRadians(bullet.getAngle())) * 300 * dt;
            double nextY = bullet.getY() + Math.sin(Math.toRadians(bullet.getAngle())) * 300 * dt;

            // 检查子弹是否超出边界
            if (nextX < 0 || nextX > 800 || nextY < 0 || nextY > 800) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // 更新子弹位置
            bullet.move(dt);

            // 碰撞检测
            boolean hitSomething = false;
            for (Car car : cars.values()) {
                if (car.isAlive() && !car.getId().equals(bullet.getShooterId())) {
                    double dx = car.getX() - bullet.getX();
                    double dy = car.getY() - bullet.getY();
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 30) { // 碰撞半径
                        car.damage();
                        bulletsToRemove.add(bullet);
                        hitSomething = true;

                        // 发送击中消息
                        String message = car.getUsername() + " 被击中! 剩余生命值: " + car.getHealth();
                        messagingTemplate.convertAndSend("/topic/messages", 
                            new ChatMessage("系统", message, true));

                        if (!car.isAlive()) {
                            messagingTemplate.convertAndSend("/topic/messages", 
                                new ChatMessage("系统", car.getUsername() + " 挑战失败!", true));
                        }
                        break;
                    }
                }
            }

            if (hitSomething) {
                break;
            }
        }

        // 安全地移除失效的子弹
        bullets.removeAll(bulletsToRemove);

        // 广播游戏状态
        RacingState state = new RacingState(cars, bullets, System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/racing", state);
    }
}
