package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.xml.SimpleNamespaceContext;
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
    private APConfig config;
    public UblXmlReader(APConfig config){
        this.config = config;
    }

    public String getElementValue(String xml, String element)throws Exception{
        String value = null;
        Element root = getRootElement(xml);
        String[] props = element.split("\\.");
        for(int i = 0; i < props.length && root != null; i++) {
            String[] prop = props[i].split(":");
            NodeList nodeList = root.getElementsByTagNameNS(config.getNamespaces().get(prop[0]), prop[1]);

            root = (Element) nodeList.item(0);
            if (i == props.length -1 && root != null) {
                value = root.getTextContent();
                break;
            }
        }
        return value;
    }

    public String getElementValue(Element root, String element){
        String value = null;
        String[] props = element.split("\\.");
        for(int i = 0; i < props.length && root != null; i++) {
            String[] prop = props[i].split(":");
            NodeList nodeList = root.getElementsByTagNameNS(config.getNamespaces().get(prop[0]), prop[1]);
            root = (Element) nodeList.item(0);
            if (i == props.length -1 && root != null) {
                value = root.getTextContent();
                break;
            }
        }
        return value;
    }

    public NodeList getNodes(String xml, String elementPath)throws Exception{
        Element root = getRootElement(xml);
        String[] prop = elementPath.split(":");
        NodeList nodeList = root.getElementsByTagNameNS(config.getNamespaces().get(prop[0]), prop[1]);
        return nodeList;
    }

    public String getElementValue(String xml, String element, String defaultValue)throws Exception{
        String value = getElementValue(xml, element);
        return StringUtils.isNotEmpty(value)? value: defaultValue;
    }

    public String getXPathValue(String xml, String elementPath)throws Exception{
        XPath xPath = XPathFactory.newInstance().newXPath();
        SimpleNamespaceContext nsc = new SimpleNamespaceContext();
        config.getNamespaces().forEach((pref, ns) -> nsc.bindNamespaceUri(pref, ns));
        xPath.setNamespaceContext(nsc);
        Element root = getRootElement(xml);
        String value = (String) xPath.compile(elementPath).evaluate(root, XPathConstants.STRING);
        return value;
    }

    public String getXPathValue(Element root, String elementPath)throws Exception{
        XPath xPath = XPathFactory.newInstance().newXPath();
        SimpleNamespaceContext nsc = new SimpleNamespaceContext();
        config.getNamespaces().forEach((pref, ns) -> nsc.bindNamespaceUri(pref, ns));
        xPath.setNamespaceContext(nsc);
        String value = (String) xPath.compile(elementPath).evaluate(root, XPathConstants.STRING);
        return value;
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