package org.sam.server.context;

/**
 * Created by melchor
 * Date: 2020/08/10
 * Time: 12:04 AM
 */
public class Bean {

    private String name;
    private Object instance;

    public Bean(String name, Object instance) {
        this.name = name;
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public Object getInstance() {
        return instance;
    }
}
