package com.jun.li.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenRedisGateway extends AbstractHandlerChain {
    @Autowired
    public void setNext(AccessTokenMysqlGateway next) {
        this.next = next;
    }

    @Override
    public AccessToken query() {
        return null;
    }

    @Override
    public void update(AccessToken accessToken) {

    }
}
