package de.lehmannet.om.mapper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lehmannet.om.IEquipment;
import de.lehmannet.om.IFilter;
import de.lehmannet.om.ISchemaElement;
import de.lehmannet.om.util.SchemaException;

public class FilterMapper {


    public static String getOptionalVendorName(Element filterElement) throws SchemaException {
        NodeList children;
        Element child;
        // Get optional vendor name
        child = null;
        children = filterElement.getElementsByTagName(IFilter.XML_ELEMENT_VENDOR);
        StringBuilder vendor = new StringBuilder();
        if (children != null) {
            if (children.getLength() == 1) {
                child = (Element) children.item(0);
                if (child != null) {
                    NodeList textElements = child.getChildNodes();
                    if ((textElements != null) && (textElements.getLength() > 0)) {
                        for (int te = 0; te < textElements.getLength(); te++) {
                            vendor.append(textElements.item(te).getNodeValue());
                        }
                        return vendor.toString();
                    }
                } else {
                    throw new SchemaException("Problem while retrieving vendor name from filter. ");
                }
            } else if (children.getLength() > 1) {
                throw new SchemaException("Filter can have only one vendor name. ");
            }
        }
        return null;
    }
    
    public static String getOptionalSchottValue(Element filterElement) throws SchemaException {
        NodeList children;
        Element child;
        // Get optional schott value
        child = null;
        children = filterElement.getElementsByTagName(IFilter.XML_ELEMENT_SCHOTT);
        StringBuilder schott = new StringBuilder();
        if (children != null) {
            if (children.getLength() == 1) {
                child = (Element) children.item(0);
                if (child != null) {
                    NodeList textElements = child.getChildNodes();
                    if ((textElements != null) && (textElements.getLength() > 0)) {
                        for (int te = 0; te < textElements.getLength(); te++) {
                            schott.append(textElements.item(te).getNodeValue());
                        }
                        return schott.toString();
                    }
                    /*
                     * schott = child.getFirstChild().getNodeValue(); if( schott != null ) { this.setSchott(schott); }
                     */
                } else {
                    throw new SchemaException("Problem while retrieving schott value from filter. ");
                }
            } else if (children.getLength() > 1) {
                throw new SchemaException("Filter can have only one schott value. ");
            }
        }
        return null;
    }
    
    public static String getOptionalWrattenValue(Element filterElement) throws SchemaException {
        NodeList children;
        Element child;
        // Get optional wratten value
        child = null;
        children = filterElement.getElementsByTagName(IFilter.XML_ELEMENT_WRATTEN);
        StringBuilder wratten = new StringBuilder();
        if (children != null) {
            if (children.getLength() == 1) {
                child = (Element) children.item(0);
                if (child != null) {
                    NodeList textElements = child.getChildNodes();
                    if ((textElements != null) && (textElements.getLength() > 0)) {
                        for (int te = 0; te < textElements.getLength(); te++) {
                            wratten.append(textElements.item(te).getNodeValue());
                        }
                        return wratten.toString();
                    }
                    /*
                     * wratten = child.getFirstChild().getNodeValue(); if( wratten != null ) { this.setWratten(wratten);
                     * }
                     */
                } else {
                    throw new SchemaException("Problem while retrieving wratten value from filter. ");
                }
            } else if (children.getLength() > 1) {
                throw new SchemaException("Filter can have only one wratten value. ");
            }
        }
        return null;
    }
    
    public static String getOptionalColor(Element filterElement) throws SchemaException {
        NodeList children;
        Element child;
        // Get optional color
        child = null;
        children = filterElement.getElementsByTagName(IFilter.XML_ELEMENT_COLOR);
        StringBuilder color = new StringBuilder();
        if (children != null) {
            if (children.getLength() == 1) {
                child = (Element) children.item(0);
                if (child != null) {
                    NodeList textElements = child.getChildNodes();
                    if ((textElements != null) && (textElements.getLength() > 0)) {
                        for (int te = 0; te < textElements.getLength(); te++) {
                            color.append(textElements.item(te).getNodeValue());
                        }
                        return color.toString();
                    }
                    /*
                     * color = child.getFirstChild().getNodeValue(); if( color != null ) { this.setColor(color); }
                     */
                } else {
                    throw new SchemaException("Problem while retrieving color from filter. ");
                }
            } else if (children.getLength() > 1) {
                throw new SchemaException("Filter can have only one color. ");
            }
        }
        return null;
    }
    
    public static boolean getOptionalAvailability(Element filterElement) {
        // Search for optional availability comment within nodes
        NodeList list = filterElement.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node c = list.item(i);
            if (c.getNodeType() == Node.COMMENT_NODE) {
                if (IEquipment.XML_COMMENT_ELEMENT_NOLONGERAVAILABLE.equals(c.getNodeValue())) {
                   return false;
                }
            }
        }
        return false;
    }
    
    public static String getMandatoryType(Element filterElement) throws SchemaException {
        NodeList children;
        Element child;
        // Get mandatory type
        children = filterElement.getElementsByTagName(IFilter.XML_ELEMENT_TYPE);
        if ((children == null) || (children.getLength() != 1)) {
            throw new SchemaException("Filter must have exact one type. ");
        }
        child = (Element) children.item(0);
        StringBuilder type = new StringBuilder();
        if (child == null) {
            throw new SchemaException("Filter must have a type. ");
        } else {
            if (child.getFirstChild() != null) {
                NodeList textElements = child.getChildNodes();
                if ((textElements != null) && (textElements.getLength() > 0)) {
                    for (int te = 0; te < textElements.getLength(); te++) {
                        type.append(textElements.item(te).getNodeValue());
                    }
                    return type.toString();
                   
                }
                // type = child.getFirstChild().getNodeValue();
            } else {
                throw new SchemaException("Filter cannot have an empty type. ");
            }
        }
        return null;
    }
    
    public static String getMadatoryModel(Element filterElement) throws SchemaException {
        NodeList children;
        Element child;
        // Get mandatory model
        children = filterElement.getElementsByTagName(IFilter.XML_ELEMENT_MODEL);
        if ((children == null) || (children.getLength() != 1)) {
            throw new SchemaException("Filter must have exact one model name. ");
        }
        child = (Element) children.item(0);
        StringBuilder model = new StringBuilder();
        if (child == null) {
            throw new SchemaException("Filter must have a model name. ");
        } else {
            if (child.getFirstChild() != null) {
                NodeList textElements = child.getChildNodes();
                if ((textElements != null) && (textElements.getLength() > 0)) {
                    for (int te = 0; te < textElements.getLength(); te++) {
                        model.append(textElements.item(te).getNodeValue());
                    }
                   return model.toString();
                }
                // model = child.getFirstChild().getNodeValue();
            } else {
                throw new SchemaException("Filter cannot have an empty model name. ");
            }
        }
        return null;
    }
    
    public static String getID(Element filterElement) throws SchemaException {
        // Get ID from element
        String ID = filterElement.getAttribute(ISchemaElement.XML_ELEMENT_ATTRIBUTE_ID);
        if ((ID != null) && ("".equals(ID.trim()))) {
            throw new SchemaException("Filter must have a ID. ");
        }
        return ID;
    }
}