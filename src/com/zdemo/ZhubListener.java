package com.zdemo;

import org.redkale.boot.Application;
import org.redkale.boot.ApplicationListener;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.RedkaleClassLoader;
import org.redkale.util.ResourceFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 服务监听
 *
 * @author: liangxy.
 */
public class ZhubListener implements ApplicationListener {

    @Override
    public void preStart(Application application) {

        CompletableFuture.runAsync(() -> {
            ResourceFactory resourceFactory = application.getResourceFactory();
            RedkaleClassLoader classLoader = application.getClassLoader();

            AnyValue appConfig = application.getAppConfig();
            AnyValue zhubs = appConfig.getAnyValue("zhubs");
            AnyValue[] values = zhubs.getAnyValues("zhub");
            for (AnyValue zhub : values) {
                String clazz = zhub.getValue("value", "com.zdemo.zhub.ZHubClient");
                try {
                    Class<?> aClass = classLoader.loadClass(clazz);
                    Service obj = (Service) aClass.newInstance();
                    obj.init(zhub);
                    resourceFactory.register(zhub.get("name"), aClass, obj);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void preShutdown(Application application) {

    }
}
