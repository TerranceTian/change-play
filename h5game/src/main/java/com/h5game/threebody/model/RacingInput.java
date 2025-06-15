package com.h5game.threebody.model;

public class RacingInput {
    private String username;
    private String input;

    public RacingInput() {}

    public RacingInput(String username, String input) {
        this.username = username;
        this.input = input;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}
