package org.sam.server.exception;

public class ComponentScanNotFoundException extends RuntimeException {
    public ComponentScanNotFoundException() {
        super("ComponentScan not found in package");
    }
}
