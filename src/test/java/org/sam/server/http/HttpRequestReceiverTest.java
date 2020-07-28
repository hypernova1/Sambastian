package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 3:47 PM
 */
class HttpRequestReceiverTest {

    @Test
    void test() {
        String handlerPath = "/";
        String requestPath = "/path";

        assertEquals(true, requestPath.startsWith(handlerPath));
    }

    @Test
    void test2() {
        String formData = "------WebKitFormBoundarybN8S4aB20v24VBLR\n" +
                "Content-Disposition: form-data; name=\"name\"\n" +
                "\n" +
                "sam\n" +
                "------WebKitFormBoundarybN8S4aB20v24VBLR\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"component.html\"\n" +
                "Content-Type: text/html\n" +
                "\n" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"kr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "    <title>Document</title>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/vue/dist/vue.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <todo-item></todo-item>\n" +
                "<script>\n" +
                "    Vue.component('todo-item', {\n" +
                "        template: '<ol>확인</ol>'\n" +
                "    })    \n" +
                "</script>\n" +
                "</body>\n" +
                "</html>\n" +
                "------WebKitFormBoundarybN8S4aB20v24VBLR--";

        String[] split = formData.replace("/\\s/g", "").split("------WebKitFormBoundarybN8S4aB20v24VBLR");
        List<String> multipartList = Arrays.asList(split);
        multipartList = multipartList.subList(1, multipartList.size() - 1);

        List<Object> collect = multipartList.stream().map(multipart -> {
            List<String> lines = Arrays.asList(multipart.split("\\n"));
            lines = lines.stream().filter(line -> !line.isEmpty()).collect(Collectors.toList());
            return lines;
        }).collect(Collectors.toList());

        System.out.println(collect.get(0));
        System.out.println("--------------------");
        System.out.println(collect.get(1));
    }

}