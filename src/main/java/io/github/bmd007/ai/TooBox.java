package io.github.bmd007.ai;


import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TooBox {
    @Tool(description = "Get the current date and time in the user's timezone, provided in ISO-8601 format")
    public String getCurrentDateTime() {
        var d = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Current time is: " + d);
        return d;
    }

    @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
    public void setAlarm(String time) {
        var alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }

}

