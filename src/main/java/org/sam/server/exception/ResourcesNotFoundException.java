package org.sam.server.exception;

/**
 * Created by melchor
 * Date: 2020/08/14
 * Time: 10:12 AM
 */
public class ResourcesNotFoundException extends RuntimeException {
    public ResourcesNotFoundException(String filename) {
        super(filename + " not found.");
    }
}
