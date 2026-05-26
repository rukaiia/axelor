package com.example.currency.service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.example.currency.db.CurrencyRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class NbkrXmlParser {

    private static final Logger log = LoggerFactory.getLogger(NbkrXmlParser.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static List<CurrencyRate> parse(String xml) {
        List<CurrencyRate> result = new ArrayList<>();
        Document document = buildDocument(xml);
        Element root = document.getDocumentElement();

        LocalDate rateDate = LocalDate.parse(root.getAttribute("Date").trim(), DATE_FORMAT);

        NodeList currencyNodes = document.getElementsByTagName("Currency");
        log.info("Курсы за {}, найдено валют: {}", rateDate, currencyNodes.getLength());
        for (int i = 0; i < currencyNodes.getLength(); i++) {
            CurrencyRate rate = parseElement((Element) currencyNodes.item(i), rateDate);
            if (rate != null) {
                result.add(rate);
            }
        }
        log.info("Успешно разобрано: {}", result.size());
        return result;
    }

    private static CurrencyRate parseElement(Element element, LocalDate rateDate) {
        try {
            String code = element.getAttribute("ISOCode").trim();
            if (code.isEmpty()) {
                return null;
            }
            String nominalText = getChildText(element, "Nominal");
            String valueText = getChildText(element, "Value").replace(",", ".");

            if (nominalText.isEmpty() || valueText.isEmpty()) {
                return null;
            }
            CurrencyRate currencyRate = new CurrencyRate();
            currencyRate.setCode(code);
            currencyRate.setName(code);
            currencyRate.setNominal(Integer.parseInt(nominalText.trim()));
            currencyRate.setRate(new BigDecimal(valueText.trim()));
            currencyRate.setRateDate(rateDate);
            return currencyRate;

        } catch (Exception e) {
            log.warn("Ошибка при разборе элемента: {}", e.getMessage());
            return null;
        }
    }

    private static Document buildDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось разобрать XML: " + e.getMessage(), e);
        }
    }

    private static String getChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }
}