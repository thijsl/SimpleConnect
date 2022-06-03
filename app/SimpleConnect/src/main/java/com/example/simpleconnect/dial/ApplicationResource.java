package com.example.simpleconnect.dial;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

interface DialContent {

    String getContentType();

    byte[] getData();
}

public class ApplicationResource {

    private static final String APPLICATION_DIAL_VERSION_QUERY = "clientDialVersion=2.1";
    private static final String CLIENT_FRIENDLY_NAME_QUERY = "friendlyName";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final DialContent NO_CONTENT = new DialContent() {
        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public byte[] getData() {
            return null;
        }
    };

    private final String clientFriendlyName;
    private final URL rootUrl;
    private boolean sendQueryParameter;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private Device device;

    ApplicationResource(String clientFriendlyName, URL rootUrl, Device device) {

        this.clientFriendlyName = clientFriendlyName;
        this.rootUrl = rootUrl;
        this.device = device;
        this.sendQueryParameter = true;
    }

    public Application getApplication(String applicationName) throws IOException {
        System.out.println("trying to get app");
        URLBuilder applicationUrl = URLBuilder.of(rootUrl).path(applicationName);

        if (sendQueryParameter) {

            applicationUrl.query(APPLICATION_DIAL_VERSION_QUERY);
        }

        HttpURLConnection httpUrlConnection = (HttpURLConnection) applicationUrl.build().openConnection();

        addTimeoutParameter(httpUrlConnection);
        httpUrlConnection.setDoInput(true);

        if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

            return null;
        }
        System.out.println("we here on " + httpUrlConnection.getURL().toString() + " for " + device.friendlyName);
        try (InputStream inputStream = httpUrlConnection.getInputStream()) {
            System.out.println("we are trying here");
            Document serviceDocument = getServiceDocument(inputStream);

            Application application = new Application();
            System.out.println("we are still trying here");

            application.name = getTextFromSub(serviceDocument, "name");
            application.instanceUrl = getInstanceUrl(serviceDocument, application.name);
            application.allowStop = getIsAllowStopFromOption(serviceDocument);
            application.additionalData = extractAdditionalData(serviceDocument);
            extractState(serviceDocument, application);
            System.out.println("App is: " + application.state);
            return application;

        } catch (ParserConfigurationException | SAXException | ApplicationResourceException e) {
            System.out.println("error is " + e.toString());
            return null;
        }
    }

    private Node extractAdditionalData(Document document) {

        NodeList nodes = document.getElementsByTagName("additionalData");

        if (nodes.getLength() >= 1) {

            return nodes.item(0);
        }

        return null;
    }

    private boolean getIsAllowStopFromOption(Document document) {

        NodeList nodes = document.getElementsByTagName("options");

        if (nodes.getLength() < 1) {
            return false;
        }

        NamedNodeMap optionAttributes = nodes.item(0).getAttributes();
        Node allowStop = optionAttributes.getNamedItem("allowStop");

        return allowStop != null && Boolean.parseBoolean(allowStop.getTextContent());
    }

    private URL getInstanceUrl(Document document, String applicationName) throws MalformedURLException, ApplicationResourceException {

        NodeList nodes = document.getElementsByTagName("link");

        if (nodes.getLength() < 1) {
            throw new ApplicationResourceException("Document has no link element");
        }

        NamedNodeMap linkAttributes = nodes.item(0).getAttributes();
        Node href = linkAttributes.getNamedItem("href");
        Node rel = linkAttributes.getNamedItem("rel");

        if (rel == null || href == null || !rel.getTextContent().equals("run")) {

            throw new ApplicationResourceException("Unknown link type on service");
        }

        return URLBuilder.of(rootUrl).path(applicationName).path(href.getTextContent()).build();
    }

    private void extractState(Document document, Application application) throws ApplicationResourceException, MalformedURLException {

        String stateText = getTextFromSub(document, "state");

        State state = mapToState(stateText);
        application.state = state;

        if (state == State.INSTALLABLE) {
            application.installUrl = getInstallUrl(stateText);
        }
    }

    private URL getInstallUrl(String state) throws MalformedURLException {

        String[] stateParts = state.split("=");

        if (stateParts.length < 2) {
            return null;
        }

        return new URL(stateParts[1]);
    }

    private State mapToState(String value) throws ApplicationResourceException {

        if (value == null) {
            throw new ApplicationResourceException("App exists but has no state");
        }

        String lowercaseStatus = value.toLowerCase();
        if (lowercaseStatus.startsWith("installable")) {

            return State.INSTALLABLE;
        }

        switch (lowercaseStatus) {

            case "running":
                return State.RUNNING;
            case "stopped":
                return State.STOPPED;
            case "hidden":
                return State.HIDDEN;
            default:
                throw new ApplicationResourceException("Unknown state: " + value);
        }
    }

    private void addTimeoutParameter(HttpURLConnection httpUrlConnection) {

        if (connectionTimeout != null) {
            httpUrlConnection.setConnectTimeout(connectionTimeout);
        }

        if (readTimeout != null) {
            httpUrlConnection.setReadTimeout(readTimeout);
        }
    }

    private String getTextFromSub(Document element, String tagName) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() >= 1) {
            return elementsByTagName.item(0).getTextContent();
        }
        return "";
    }

    private Document getServiceDocument(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);

        document.getDocumentElement().normalize();

        return document;
    }

}

class ApplicationResourceException extends Exception {

    public ApplicationResourceException(String message) {
        super(message);
    }
}