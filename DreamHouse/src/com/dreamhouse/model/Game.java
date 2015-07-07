package com.dreamhouse.model;

import javax.persistence.*;

/**
 * 表示一局游戏的数据结构。
 * Created by jason on 2015/7/2.
 */
@Entity(name = "game")
public class Game {

    public static final int STATE_WAITING = 1;// 等待中
    public static final int STATE_PLAYING = 2;// 进行中
    public static final int STATE_FINISH = 3;// 终结
    public static final int STATE_ABANDON = 4;// 抛弃

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private long id;
    @Column(name = "state",nullable = false)
    private int state;
    @Column(name = "name")
    private String name;
    @Column(name = "num")
    private int num;
    @Column(name = "phone")
    private String phone;
    @Column(name = "description")
    private String description;

    @Column(name = "game_start_time")
    private long gameStartTime;
    @Column(name = "game_end_time")
    private long gameEndTime;
    @Column(name = "game_result")
    private String gameResult;

    @Column(name = "secret_end_time")
    private long secretEndTime;
    @Column(name = "secret_result")
    private String secretResult;

    @Column(name = "car_end_time")
    private long carEndTime;
    @Column(name = "car_result")
    private String carResult;

    @Column(name = "minigame_end_time")
    private long minigameEndTime;
    @Column(name = "minigame_result")
    private String minigameResult;

    public Game() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public void setGameStartTime(long gameStartTime) {
        this.gameStartTime = gameStartTime;
    }

    public long getGameEndTime() {
        return gameEndTime;
    }

    public void setGameEndTime(long gameEndTime) {
        this.gameEndTime = gameEndTime;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    public long getSecretEndTime() {
        return secretEndTime;
    }

    public void setSecretEndTime(long secretEndTime) {
        this.secretEndTime = secretEndTime;
    }

    public String getSecretResult() {
        return secretResult;
    }

    public void setSecretResult(String secretResult) {
        this.secretResult = secretResult;
    }

    public long getCarEndTime() {
        return carEndTime;
    }

    public void setCarEndTime(long carEndTime) {
        this.carEndTime = carEndTime;
    }

    public String getCarResult() {
        return carResult;
    }

    public void setCarResult(String carResult) {
        this.carResult = carResult;
    }

    public long getMinigameEndTime() {
        return minigameEndTime;
    }

    public void setMinigameEndTime(long minigameEndTime) {
        this.minigameEndTime = minigameEndTime;
    }

    public String getMinigameResult() {
        return minigameResult;
    }

    public void setMinigameResult(String minigameResult) {
        this.minigameResult = minigameResult;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
