package com.blacklist.blacklist.dao;

import com.blacklist.blacklist.Mappr.BlacklistMapper;
import com.blacklist.blacklist.entry.Blacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor(onConstructor_={@Autowired})
@Repository
public class BlacklistDao{

    private final BlacklistMapper blacklistMapper;

    public Blacklist getBlacklistByIp(String ip) {
        Blacklist blacklist = new Blacklist();
        blacklist.setIp(ip);
        return this.blacklistMapper.selectOne(blacklist);
    }

    public void insert(Blacklist blacklist) {
        this.blacklistMapper.insert(blacklist);
    }
}
