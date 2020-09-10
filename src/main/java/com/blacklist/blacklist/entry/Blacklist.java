package com.blacklist.blacklist.entry;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
public class Blacklist {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int id;

    private String ip;

    private Date creatTime;

    public Blacklist() {
        super();
    }

    public Blacklist(String ip, Date creatTime) {
        this.ip = ip;
        this.creatTime = creatTime;
    }
}
