package com.situ.elder.tools;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTools {

    @Tool(description = "获取当前日期时间，用户提问关于日期时间的时候调用，格式为 yyyy-MM-dd HH:mm:ss，中文描述。如果用户只问日期，就只回答日期。")
    public String getTime() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return now.format(formatter);
    }
}
