package com.example.tir.service;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class XmlParser {

    public static Document parse(String xml) {
        try {
            if (xml == null || xml.isEmpty()) {
                throw new RuntimeException("XML не может быть пустым");
            }
            String cleanXml = xml.trim();
            if (cleanXml.startsWith("\uFEFF")) {
                cleanXml = cleanXml.substring(1);
            }
            if (!cleanXml.startsWith("<")) {
                throw new RuntimeException("Некорректный формат XML");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(cleanXml)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось распарсить XML: " + e.getMessage(), e);
        }
    }
    public static String getTagValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }

    public static String getRootTag(String xml) {
        return parse(xml).getDocumentElement().getTagName();
    }



    public static String buildSoapFault(String faultCode, String faultString) {
        return "<soap:Fault>" +
                "<faultcode>" + faultCode + "</faultcode>" +
                "<faultstring>" + faultString + "</faultstring>" +
                "</soap:Fault>";
    }


}

