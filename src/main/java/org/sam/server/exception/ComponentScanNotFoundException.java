package org.sam.server.exception;

/**
 * Created by melchor
 * Date: 2020/08/12
 * Time: 11:48 AM
 */
public class ComponentScanNotFoundException extends RuntimeException {
    public ComponentScanNotFoundException() {
        super("ComponentScan not found in package");
    }
}
