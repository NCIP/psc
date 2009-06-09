package org.dynamicjava.osgi.da_launcher.internal.utilities;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XPathUtils {
	
	public static Element[] getElements(XPath xpath, Element element, String expression) {
		try {
			return convertToElements((NodeList)xpath.evaluate(expression, element, XPathConstants.NODESET));
		} catch (XPathExpressionException ex) {
			throw new XPathException(ex);
		}
	}
	
	public static Element getElement(XPath xpath, Element element, String expression) {
		Element[] elements = getElements(xpath, element, expression);
		return elements.length > 0 ? elements[0] : null;
	}
	
	public static Element[] convertToElements(NodeList nodeList) {
		if (nodeList == null || nodeList.getLength() == 0) {
			return new Element[0];
		}
		
		Element[] result = new Element[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++) {
			result[i] = (Element)nodeList.item(i);
		}
		return result;
	}
	
	
	public static class XPathException extends RuntimeException {
		
		public XPathException(XPathExpressionException ex) {
			super(ex.getMessage(), ex);
		}
		
		private static final long serialVersionUID = ("urn:" + XPathException.class.getName()).hashCode();
		
	}
	
	private XPathUtils() { }
	
}
