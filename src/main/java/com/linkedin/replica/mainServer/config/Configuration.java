package com.linkedin.replica.mainServer.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static final Properties webServConfigProp = new Properties();
    private static final Properties commandConfigProp = new Properties();
    private static final Properties appConfigProp = new Properties();

    private static Configuration instance;

    private Configuration (String webServConfigPath, String commandConfigPath, String appConfigPath) throws IOException{
        populateWithConfig(webServConfigPath, webServConfigProp);
        populateWithConfig(commandConfigPath, commandConfigProp);
        populateWithConfig(appConfigPath, appConfigProp);
    }

    public static Configuration getInstance() {
        return instance;
    }

    public static void init(String webServConfigPath, String commandConfigPath, String appConfigPath) throws IOException {
        instance = new Configuration(webServConfigPath, commandConfigPath, appConfigPath);
    }

    private static void populateWithConfig(String configFilePath,Properties properties) throws IOException {
        FileInputStream inputStream = new FileInputStream(configFilePath);
        properties.load(inputStream);
        inputStream.close();
    }

    public String getWebServConfigProp(String key) {
        return webServConfigProp.getProperty(key);
    }

    public String getCommandConfigProp(String key) {
        return commandConfigProp.getProperty(key);
    }

    public String getAppConfigProp(String key){
        return appConfigProp.getProperty(key);
    }
}
