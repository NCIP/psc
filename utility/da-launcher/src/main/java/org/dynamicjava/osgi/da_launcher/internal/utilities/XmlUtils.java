package org.dynamicjava.osgi.da_launcher.internal.utilities;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlUtils {
	
	public static Document parseDocument(InputStream documentIn) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(documentIn);
		} catch (Exception ex) {
			throw new LauncherException(String.format("Failed to parse XML Document: %s",
					ex.getMessage()), ex);
		}
	}
	
	public static Attr findAttribute(Element element, String attributeName) {
		for (int i = 0; i < element.getAttributes().getLength(); i++) {
			Attr attribute = (Attr)element.getAttributes().item(i);
			if (attributeName.equals(attribute.getName())) {
				return attribute;
			}
		}
		return null;
	}
	
	
	private XmlUtils() { }
	
}
