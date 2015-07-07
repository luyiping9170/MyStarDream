package com.dreamhouse.model;

import javax.persistence.*;

/**
 * Created by jason on 2015/7/3.
 */
@Entity(name = "arduino")
public class Arduino {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private long id;

    @Column(name = "name")
    private String name;

    public Arduino() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
