package org.sam.server.context;

/**
 * ComponentScan 어노테이션을 찾을 수 없을 때 발생한다.
 *
 * @author hypernova1
 * */
public class ComponentScanNotFoundException extends RuntimeException {
    public ComponentScanNotFoundException() {
        super("ComponentScan not found in package");
    }
}
