/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkalex.cache.redis.redission;

import org.redkale.annotation.Priority;
import org.redkale.source.CacheSource;
import org.redkale.source.CacheSourceProvider;
import org.redkale.util.AnyValue;

/**
 *
 * @author zhangjx
 */
@Priority(-300)
public class RedissionCacheSourceProvider implements CacheSourceProvider {

    @Override
    public boolean acceptsConf(AnyValue config) {
        try {
            Object.class.isAssignableFrom(org.redisson.config.Config.class); //试图加载Redission相关类
            return new RedissionCacheSource().acceptsConf(config);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public CacheSource createInstance() {
        return new RedissionCacheSource();
    }

}
