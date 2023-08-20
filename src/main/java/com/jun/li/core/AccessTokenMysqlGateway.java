package com.jun.li.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenMysqlGateway extends AbstractHandlerChain {
    @Autowired
    public void setPre(AccessTokenRedisGateway pre) {
        this.pre = pre;
    }

    @Autowired
    public void setNext(AccessTokenHttpGateway next) {
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
