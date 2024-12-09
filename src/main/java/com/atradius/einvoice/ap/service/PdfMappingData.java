package com.atradius.einvoice.ap.service;

import com.atradius.einvoice.ap.config.APConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PdfMappingData {
    private UblXmlReader ublXmlReader;
    private APConfig config;

    public PdfMappingData(UblXmlReader ublXmlReader, APConfig config){
        this.ublXmlReader  = ublXmlReader;
        this.config = config;
    }

    public List<String> getSupplierData(String xml, String rootElement)throws Exception{
        return getXPathData(xml, config.getSupplierMappings(), rootElement);
    }

    public List<String> getCustomerData(String xml, String rootElement)throws Exception{
        return getXPathData(xml, config.getCustomerMappings(), rootElement);
    }

    public List<String> getInvoiceData(String xml, String rootElement)throws Exception{
        return getXPathData(xml, config.getInvoiceMappings(), rootElement);
    }

    public List<String> getBankData(String xml, String rootElement)throws Exception{
        return getXPathData(xml, config.getBankMappings(), rootElement);
    }

    public List<List<String>> getPaymentsData(String xml, String rootElement)throws Exception{
        List<List<String>> result = new ArrayList<>();
        NodeList nodes = ublXmlReader.getNodes(xml, config.getPaymentTag());
        for(int i = 0; i < nodes.getLength(); i++){
            List<String> row = new ArrayList<>();
            Element node = (Element) nodes.item(i);
            config.getPaymentMappings().stream().forEach(element -> {
                    row.add(ublXmlReader.getElementValue(node, element));
            });
            result.add(row);
        }
        return result;
    }

    public List<String> getXPathData(String xml, List<String> mappings, String rootElement)throws Exception{
        List<String> data = new ArrayList<>();
        for(String mapping: mappings){
            String value = getContent(xml, mapping, rootElement);
            if(StringUtils.isNotEmpty(value)) {
                data.add(value);
            }
        }
        return data;
    }
    private String getContent(String xml, String listItem, String rootElement)throws Exception{
        boolean hasLabel = listItem.indexOf("::") != -1;
        String[] element = listItem.split("::");
        String label = element[0];
        String contentValue = "";
        if(element.length == 2 || !hasLabel) {
            String contentPath = element[element.length == 2 ? 1 : 0];
            label = element.length == 2 ? element[0] + ": " : "";
            contentValue = StringUtils.isNotEmpty(contentPath) ? ublXmlReader.getXPathValue(xml, rootElement + contentPath) : "";
        }
        return hasLabel ? String.join("", label, contentValue) : contentValue;
    }

    public List<String> getXPathData(Element root, List<String> mappings)throws Exception{
        List<String> data = new ArrayList<>();
        for(String mapping: mappings){
            String value = ublXmlReader.getXPathValue(root, mapping);
            if(StringUtils.isNotEmpty(value)) {
                data.add(value);
            }
        }
        return data;
    }
}
