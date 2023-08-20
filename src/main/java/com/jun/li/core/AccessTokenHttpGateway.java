package com.jun.li.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenHttpGateway extends AbstractHandlerChain {
    @Autowired
    public void setPre(AccessTokenMysqlGateway pre) {
        this.pre = pre;
    }

    @Override
    public AccessToken query() {
        return new AccessToken();
    }

    @Override
    public void update(AccessToken accessToken) {

    }
}
