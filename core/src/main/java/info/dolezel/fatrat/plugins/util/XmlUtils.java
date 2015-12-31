/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2011 Lubos Dolezel <lubos a dolezel.info>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package info.dolezel.fatrat.plugins.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author lubos
 */
public class XmlUtils {
    static final XPathFactory factory = XPathFactory.newInstance();
    static final DocumentBuilderFactory dbf;

    static {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setValidating(false);
    }

    public static XPathExpression expr(String xpathStr) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        return xpath.compile(xpathStr);
    }

    public static String xpathString(Node node, String xpath) throws XPathExpressionException {
        return (String) expr(xpath).evaluate(node, XPathConstants.STRING);
    }

    public static Node xpathNode(Node node, String xpath) throws XPathExpressionException {
        return (Node) expr(xpath).evaluate(node, XPathConstants.NODE);
    }

    public static NodeList xpathNodeList(Node node, String xpath) throws XPathExpressionException {
        return (NodeList) expr(xpath).evaluate(node, XPathConstants.NODESET);
    }

    public static Document loadDocument(ByteBuffer buf) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //byte[] b = Charset.forName("UTF-8").decode(buf).toString().getBytes("UTF-8");
        //System.out.println("New byte[] len: "+b.length);
        //return builder.parse(new ByteArrayInputStream(b));
        return builder.parse(new ByteBufferInputStream(buf));
    }
}
