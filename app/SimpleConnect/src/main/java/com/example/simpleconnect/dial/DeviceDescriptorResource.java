package com.example.simpleconnect.dial;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceDescriptorResource {

    private static final String APPLICATION_URL_HEADER = "Application-URL";

    public DeviceDescriptor getDescriptor(URL deviceDescriptorLocation) throws IOException {
        if (deviceDescriptorLocation == null) {

            throw new IllegalArgumentException("Device descriptor can't be null");
        }

        if (!deviceDescriptorLocation.getProtocol().equals("http")) {
            return null;
        }
        HttpURLConnection connection = (HttpURLConnection) deviceDescriptorLocation.openConnection();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }

        String applicationUrl = connection.getHeaderField(APPLICATION_URL_HEADER);

        if (applicationUrl == null) {
            return null;
        }

        DeviceDescriptor deviceDescriptor = new DeviceDescriptor();
        deviceDescriptor.applicationResourceUrl = new URL(applicationUrl);

        readInfoFromBody(connection, deviceDescriptor);
        return deviceDescriptor;
    }

    private void readInfoFromBody(HttpURLConnection connection, DeviceDescriptor deviceDescriptor) throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document bodyDocument = documentBuilder.parse(inputStream);

            bodyDocument.getDocumentElement().normalize();

            deviceDescriptor.friendlyName = getTextFromSub(bodyDocument, "friendlyName");
            deviceDescriptor.modelName = getTextFromSub(bodyDocument, "modelName");
            deviceDescriptor.modelNumber = getTextFromSub(bodyDocument, "modelNumber");
            deviceDescriptor.manufacturer = getTextFromSub(bodyDocument, "manufacturer");
        } catch (ParserConfigurationException | SAXException e) {
        }
    }

    private String getTextFromSub(Document element, String tagName) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() >= 1) {
            return elementsByTagName.item(0).getTextContent();
        }
        return "";
    }
}