package com.bcttg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.bcttg.module.media.MediaProperties;
import com.bcttg.module.settings.SettingsOperationsProperties;
import com.bcttg.module.settings.SystemSettingsDefaultsProperties;
import com.bcttg.security.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, MediaProperties.class, SystemSettingsDefaultsProperties.class, SettingsOperationsProperties.class})
public class BcttgApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcttgApplication.class, args);
    }

}
