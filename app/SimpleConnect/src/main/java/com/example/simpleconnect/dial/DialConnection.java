package com.example.simpleconnect.dial;

import java.io.IOException;

public class DialConnection {

    private ApplicationResource applicationResource;

    DialConnection(ApplicationResource applicationResource) {
        this.applicationResource = applicationResource;
    }

    /**
     * Tests if the server supports the application.
     *
     * @param applicationName The name of the application.
     * @return True if the server supports the application.
     */
    public boolean supportsApplication(String applicationName) {

        return getApplication(applicationName) != null;
    }

    /**
     * Returns an Application instance if the app is supported.
     *
     * @param applicationName The name of the application
     * @return An instance of the Application
     */
    public Application getApplication(String applicationName) {

        try {

            return applicationResource.getApplication(applicationName);
        } catch (IOException e) {

            return null;
        }
    }

    public void launch(String applicationName) {

    }

    public void install(String applicationName) {

    }

    public void stop(String applicationName) {

    }
}
