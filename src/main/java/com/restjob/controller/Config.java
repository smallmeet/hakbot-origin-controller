/*
 * This file is part of RESTjob Controller.
 *
 * RESTjob Controller is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RESTjob Controller is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RESTjob Controller. If not, see http://www.gnu.org/licenses/.
 */
package com.restjob.controller;

import com.restjob.controller.logging.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * The Config class is responsible for reading the application.properties file
 */
public final class Config {

    private static final Logger logger = Logger.getLogger(Config.class);
    static final String propFile = "application.properties";
    private static Config instance;
    private Properties properties;

    private Config() {
        init();
    }

    /**
     * Returns an instance of the Config object
     * @return a Config object
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /**
     * Initialize the Config object. This method should only be called once.
     */
    private void init() {
        if (properties != null) {
            return;
        }

        logger.info("Initializing Configuration");
        properties = new Properties();
        try {
            properties.load(RestJobControllerServlet.inputStream);
        } catch (IOException e) {
            logger.error("Unable to load " + propFile);
        }
    }

    /**
     * Return the configured value for the specified ConfigItem
     * @param item The ConfigItem to return the configuration for
     * @return a String of the value of the configuration
     */
    public String getProperty(ConfigItem item) {
        return properties.getProperty(item.propertyName);
    }

    public int getPropertyAsInt(ConfigItem item) {
        return Integer.parseInt(getProperty(item));
    }

}