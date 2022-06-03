package com.example.simpleconnect.dial;

import org.w3c.dom.Node;

import java.io.Serializable;
import java.net.URL;

public class Application {

    public static final String NETFLIX = "Netflix";
    public static final String YOUTUBE = "YouTube";
    public static final String AMAZON_INSTANT_VIDEO = "AmazonInstantVideo";

    // The name of the application
    public String name;

    // The state of the application
    public State state;

    // True if the client is allowed to stop the app
    public boolean allowStop;

    // The installUrl can be used to issue an installation of the app.
    public URL installUrl;

    /*
     * The url of a running instance.
     * The installUrl is null when no instance is running.
     */
    public URL instanceUrl;

    // Additional data defined by the app author.
    public Node additionalData;

}

enum State {

    RUNNING,
    STOPPED,
    INSTALLABLE,
    HIDDEN
}