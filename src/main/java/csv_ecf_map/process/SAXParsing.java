package csv_ecf_map.process;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by c-sergluca on 10/18/2018.
 */
public class SAXParsing {
    /**
     * @param path
     * @return xpaths into LinkedHashMap format - useful for xpath asserts from ECF
     */
    public static LinkedHashMap<String, String> parse(String path) {
        LinkedHashMap<String, String> xmlElements = new LinkedHashMap<>();
        try {
            File inputFile = new File(path);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            ECFHandler userhandler = new ECFHandler(xmlElements);
            saxParser.parse(inputFile, userhandler);
        } catch (Exception e) { e.printStackTrace(); }
        return xmlElements;
    }
}

class ECFHandler extends DefaultHandler {
    boolean started = true;
    StringBuilder parent = new StringBuilder();
    LinkedHashMap<String, String> xmlElements;
    Map<String, Integer> indexesMap = new HashMap<>();

    public ECFHandler(LinkedHashMap<String, String> xmlElements) {
        this.xmlElements = xmlElements;
    }

    public static String checkDate(String value) {
        if (value.matches("([0-9]{4}-[0-9]{2}-[0-9]{2})")) return value.replace("-", "");
        return value;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("echcf:Claim")) {
            started = true;
            qName = "/" + formatECFNode(qName, true);
            parent.append(qName);
            return;
        }
        if (started) {
        //if (started && !qName.contains(":Property")) {
            qName = formatECFNode(qName,true);

            parent.append("/").append(qName);
            if (attributes.getIndex("value") >= 0) {
                xmlElements.put(parent.toString()+"/@value", checkDate(attributes.getValue(attributes.getIndex("value"))));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("echcf:Claim")) return;
        if (started) {
        //if (started && !qName.contains(":Property")) {
            qName = formatECFNode(qName,false);
            parent.setLength(parent.length() - qName.length() - 1);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
    }

    public String formatECFNode(String str, boolean start) {
        if (str.contains(":")) str = str.replace(":", "\\:");
        String temp = start ? parent.toString() : parent.substring(0,parent.lastIndexOf("/"));
        temp+= "/" + str;
        if(start)
        if (!indexesMap.containsKey(temp)) {
            indexesMap.put(temp, 1);
        } else indexesMap.put(temp, indexesMap.get(temp) + 1);
        str = str + "[" + indexesMap.get(temp) + "]";

        return str;
    }
}

