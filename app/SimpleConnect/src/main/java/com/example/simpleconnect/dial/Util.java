package com.example.simpleconnect.dial;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Util {

    public static String getTextFromSub(Document element, String tagName) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() >= 1) {
            return elementsByTagName.item(0).getTextContent();
        }
        return "";
    }

}
