package org.sam.server.context;

/**
 * Created by melchor
 * Date: 2020/08/10
 * Time: 12:04 AM
 */
public class Bean {

    private final String name;
    private final Object instance;

    Bean(String name, Object instance) {
        this.name = name;
        this.instance = instance;
    }

    String getName() {
        return name;
    }

    Object getInstance() {
        return instance;
    }
}
