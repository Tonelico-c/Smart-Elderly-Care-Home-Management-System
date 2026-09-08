package com.situ.elder.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
    @Bean
    public DefaultKaptcha kaptchaProducer() {
        Properties properties = new Properties();
        // 图片尺寸
        properties.setProperty("kaptcha.image.width", "120");
        properties.setProperty("kaptcha.image.height", "40");
        // 验证码字符集（去掉易混淆的 0/O、1/I 等）和长度
        properties.setProperty("kaptcha.textproducer.char.string", "23456789ABCDEFGHJKLMNPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // 干扰线和背景
        properties.setProperty("kaptcha.noise.color", "gray");
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        // 无边框
        properties.setProperty("kaptcha.border", "no");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(new Config(properties));
        return kaptcha;
    }
}
