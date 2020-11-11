package org.sam.server.exception;

public class ResourcesNotFoundException extends RuntimeException {
    public ResourcesNotFoundException(String filename) {
        super(filename + " not found.");
    }
}
