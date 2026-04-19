package com.skillonnet.automation.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class MentalHealthApplication extends ResourceConfig {

    public MentalHealthApplication() {
        packages("com.skillonnet.automation.api");
        register(JacksonFeature.class);
    }
}
