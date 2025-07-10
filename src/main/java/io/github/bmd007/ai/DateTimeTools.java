package io.github.bmd007.ai;


import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone, provided in ISO-8601 format")
    String getCurrentDateTime() {
        var d = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).toString();
        System.out.println("Current time is: " + d);
        return d;
    }

    @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
    void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
