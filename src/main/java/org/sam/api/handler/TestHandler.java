package org.sam.api.handler;

import org.sam.api.domain.Person;
import org.sam.server.annotation.handle.GetHandle;
import org.sam.server.annotation.handle.Handler;
import org.sam.server.annotation.handle.RestApi;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 2:33 PM
 */
@Handler("/path")
public class TestHandler {

    @GetHandle("/test")
    @RestApi
    public Person testMethod(String a, int b) {
        System.out.println("Test Method Handler");
        Person person = new Person();
        person.setName(a);

        return person;
    }
}
