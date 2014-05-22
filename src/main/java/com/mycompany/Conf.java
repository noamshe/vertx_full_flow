package com.mycompany;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

/**
 * Created by shahar.b on 5/22/2014.
 */
public class Conf {
    private static final CompositeConfiguration config = new CompositeConfiguration();
    static {
        config.addConfiguration(new SystemConfiguration());
        try {
            config.addConfiguration(new PropertiesConfiguration("application.properties"));
        } catch (ConfigurationException e) {

        }
    }

    static public CompositeConfiguration getConfig()
    {
        return config;
    }
}
