package com.example.simpleconnect.dial;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Device implements Serializable {

    // The friendly name is only set if the device exposes it via upnp device descriptor
    public String friendlyName;

    public String modelName;

    public String modelNumber;

    public String manufacturer;

    // The url to the application rest resource
    public URL applicationResourceUrl;

    // A unique identifier of the device
    public String uniqueServiceName;

    // The url to the upnp device descriptor
    public URL deviceDescriptorUrl;

    // Set if the server supports wol
    public boolean wakeOnLanSupport;

    // The MAC address to wake up the device
    public String wakeOnLanMAC;

    // The wake on lan timeout.
    public String wakeOnLanTimeout;

    // A technical description string of the server
    public String serverDescription;

    @Override
    public String toString() {
        String result = "";
        result = "{friendlyName: '"+friendlyName+"'";
        if (applicationResourceUrl != null) {
            result = result + ", applicationResourceUrl: '"+applicationResourceUrl+"'";
        }
        if (deviceDescriptorUrl != null) {
            result = result + ", deviceDescriptorUrl: '"+deviceDescriptorUrl+"'";
        }
        if (manufacturer != null) {
            result = result + ", manufacturer: '"+manufacturer+"'";
        }
        if (modelNumber != null) {
            result = result + ", modelNumber: '"+modelNumber+"'";
        }
        if (modelName != null) {
            result = result + ", modelNumber: '"+modelName+"'}";
        }
        return result;
    }

    public void launch(Application application, String[] parameters, ApplicationLaunchListener listener) {
        this.applicationLaunchListener = listener;
        launchTask(application, parameters);
    }

    private void launchTask(Application application, String[] parameters) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    URL appUrl = new URL(applicationResourceUrl.toString() + "/" + application.id);
                    if (appUrl == null) {
                        throw new IllegalArgumentException("This device doesn't have an applicationResourceUrl.");
                    }

                    if (!appUrl.getProtocol().equals("http")) {
                        return false;
                    }
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) appUrl.openConnection();
                        connection.setDoOutput(true);
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            return false;
                        }
                        try (InputStream inputStream = connection.getInputStream()) {
                            String location = connection.getHeaderField("location");
                            application.state = State.RUNNING;
                            application.instanceUrl = new URL(location);
                            return true;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            protected void onPostExecute(Object o) {
                boolean success = (boolean) o;
                super.onPostExecute(o);
                if (applicationLaunchListener != null) {
                    applicationLaunchListener.onDataLoaded(success);
                }
            }
        };
        task.execute();
    }

    public void get(String applicationId, ApplicationRequestListener listener) {
        this.applicationRequestListener = listener;
        getTask(applicationId);
    }

    private void getTask(String applicationId) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Application doInBackground(Object[] objects) {
                try {
                    URL appUrl = new URL(applicationResourceUrl.toString() + "/" + applicationId);
                    if (appUrl == null) {
                        throw new IllegalArgumentException("This device doesn't have an applicationResourceUrl.");
                    }

                    if (!appUrl.getProtocol().equals("http")) {
                        return null;
                    }
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) appUrl.openConnection();
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            return null;
                        }

                        try (InputStream inputStream = connection.getInputStream()) {
                            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                            Document bodyDocument = documentBuilder.parse(inputStream);

                            bodyDocument.getDocumentElement().normalize();
                            String name = Util.getTextFromSub(bodyDocument, "name");
                            String state = Util.getTextFromSub(bodyDocument, "state");
                            Application application = new Application();
                            application.name = name;
                            switch (state) {
                                case "running":
                                    application.state = State.RUNNING;
                                    break;
                                case "stopped":
                                    application.state = State.STOPPED;
                                    break;
                                case "hidden":
                                    application.state = State.HIDDEN;
                                    break;
                                case "installable":
                                    application.state = State.INSTALLABLE;
                                    break;
                            }
                            application.id = applicationId;
                            return application;
                        } catch (ParserConfigurationException | SAXException e) {
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                Application application = (Application) o;
                super.onPostExecute(o);
                if (applicationRequestListener != null) {
                    applicationRequestListener.onDataLoaded(application);
                }
            }
        };
        task.execute();
    }

    public interface ApplicationRequestListener {
        public void onDataLoaded(Application application);
    }

    // Member variable was defined earlier
    private ApplicationRequestListener applicationRequestListener;

    public interface ApplicationLaunchListener {
        public void onDataLoaded(boolean success);
    }

    // Member variable was defined earlier
    private ApplicationLaunchListener applicationLaunchListener;

    /**
     * Stop any app that is playing
     *
     * @return
     */
    public boolean stopActiveApplication() {
        return false;
    }

    public boolean install(String id) {
        return false;
    }

}