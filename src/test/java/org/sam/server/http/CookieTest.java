package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class CookieTest {

    @Test
    void test() {
        Date expiredDate = new Date();
        ZonedDateTime gmt = ZonedDateTime.now(ZoneId.of("GMT"));
        System.out.println(gmt);
        expiredDate.setTime(expiredDate.getTime());
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String format1 = df.format(expiredDate);
        System.out.println(format1);
    }

}