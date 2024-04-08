package com.microservice.ms.common.util.jwt;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

interface ITokenBlacklist {
    void addToBlacklist(String token);

    boolean isBlacklisted(String token);
}

@Service
public class TokenBlacklist implements ITokenBlacklist {
    private Set<String> blacklist = new HashSet<>();

    @Override
    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }

}
