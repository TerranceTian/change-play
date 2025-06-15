package com.h5game.threebody.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.h5game.threebody.model.RacingInput;
import com.h5game.threebody.simulation.RacingSimulation;
import com.h5game.threebody.simulation.ThreeBodySimulation;

@Controller
public class ThreeBodyController {
    
    private final ThreeBodySimulation simulation;
    private final RacingSimulation racingSimulation;
    
    public ThreeBodyController(ThreeBodySimulation simulation, RacingSimulation racingSimulation) {
        this.simulation = simulation;
        this.racingSimulation = racingSimulation;
    }
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/simulation")
    public String simulation(@RequestParam String username, Model model) {
        if (username == null || username.trim().isEmpty()) {
            return "redirect:/";
        }
        // 确保模拟已初始化
        simulation.initialize();
        model.addAttribute("username", username);
        return "simulation";
    }

    @GetMapping("/racing")
    public String racing(@RequestParam String username, Model model) {
        if (username == null || username.trim().isEmpty()) {
            return "redirect:/";
        }
        racingSimulation.initialize();
        racingSimulation.addPlayer(username);
        model.addAttribute("username", username);
        return "racing";
    }

    @MessageMapping("/racing/input")
    public void handleRacingInput(RacingInput input) {
        racingSimulation.handleInput(input.getUsername(), input.getInput());
    }
}
