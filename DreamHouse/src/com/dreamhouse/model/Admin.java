package com.dreamhouse.model;

import javax.persistence.*;

/**
 * Created by jason on 2015/7/2.
 */
@Entity(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private String id;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "authority")
    private String authority;

    public Admin() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
