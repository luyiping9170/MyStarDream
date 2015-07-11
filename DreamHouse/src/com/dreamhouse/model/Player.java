package com.dreamhouse.model;

import javax.persistence.*;

/**
 * Created by jason on 2015/7/2.
 */
@Entity(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private long id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "prize_id")
    private int prizeId;

    @Column(name = "prize_state")
    private int prizeState;

    @Column(name = "prize_exchange_id")
    private String prizeExchangeId;

    @Column(name = "description")
    private String description;

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE}, optional = true)
    @JoinColumn(name="game_id")
    private Game game;

    public Player() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(int prizeId) {
        this.prizeId = prizeId;
    }

    public int getPrizeState() {
        return prizeState;
    }

    public void setPrizeState(int prizeState) {
        this.prizeState = prizeState;
    }

    public String getPrizeExchangeId() {
        return prizeExchangeId;
    }

    public void setPrizeExchangeId(String prizeExchangeId) {
        this.prizeExchangeId = prizeExchangeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
