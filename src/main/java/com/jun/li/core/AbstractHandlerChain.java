package com.jun.li.core;

import java.util.Objects;

public abstract class AbstractHandlerChain {
    protected AbstractHandlerChain pre;
    protected AbstractHandlerChain next;

    public abstract AccessToken query();

    public abstract void update(AccessToken accessToken);

    public AccessToken queryChain() {
        AccessToken accessToken = query();
        if (!isValid(accessToken)) {
            if (Objects.nonNull(next)) {
                AccessToken token = next.queryChain();
                if (isValid(token)) {
                    update(token);
                    return token;
                }
            }
        } else {
            return accessToken;
        }
        return null;
    }

    public AccessToken updateChain(AccessToken accessToken){
        if(Objects.isNull(accessToken)){
            accessToken = query();
        }
        if(isValid(accessToken)){
            update(accessToken);
            if(Objects.nonNull(pre)){
                pre.updateChain(accessToken);
            }
            return accessToken;
        }
        return null;
    }

     public boolean isValid(AccessToken accessToken){
        return Objects.nonNull(accessToken);
     }
}
