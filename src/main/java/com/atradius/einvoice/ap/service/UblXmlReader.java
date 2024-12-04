package com.atradius.einvoice.ap.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Slf4j
@Service
public class UblXmlReader {
    public static final String CBC_NAMESPACE = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    public static final String CAC_NAMESPACE = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";

    public String getElementValue(String xml, String element, String namespace)throws Exception{
        String value = null;
        Element root = getRootElement(xml);
        String[] props = element.split("\\.");
        for(int i = 0; i < props.length && root != null; i++) {
            String prop= props[i];
            NodeList nodeList = root.getElementsByTagNameNS((i == props.length -1) ? CBC_NAMESPACE : namespace, prop);
            root = (Element) nodeList.item(0);
            if (i == props.length -1 && root != null) {
                value = root.getTextContent();
                break;
            }
        }
        return value;
    }

    public String getElementValue(String xml, String element, String namespace, String defaultValue)throws Exception{
        String value = getElementValue(xml, element, namespace);
        return StringUtils.isNotEmpty(value)? value: defaultValue;
    }

    public String retrieveCDATA(String xml)throws Exception{
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element root = getRootElement(xml);
        String xmlString = (String) xPath.compile("/OES_EMAIL_OUT/PAYLOAD/text()").evaluate(root, XPathConstants.STRING);
        return xmlString;
    }

    private Element getRootElement(String xml) throws Exception{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement();
    }
}