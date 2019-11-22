package com.huatu.ztk.scm.util;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
/**
 * 基于dom4j的xml工具包
 * @author shaojieyue
 * @date 2013-08-21 10:21:35
 */
public class XMLUtil {
	
	/**
	 * 从文件读取XML，输入文件名，返回XML文档
	 * @param fileName
	 * @return
	 * @throws MalformedURLException
	 * @throws DocumentException
	 */
    public static Document read(String fileName) throws MalformedURLException, DocumentException {
       SAXReader reader = new SAXReader();
       Document document = reader.read(new File(fileName));
       return document;
    }
    
    public static XPath createXPath(Element e,String xpath,String namespacePrefix){
    	XPath x=e.createXPath(xpath);
    	Namespace namespace=e.getNamespace();
    	String prefix = null;
    	//此 Element 不包含namespace
    	if(namespace.getURI()!=null&&"".equals(namespace.getURI())){
    		return x;
    	}
    	
    	if(namespacePrefix!=null){
    		prefix = namespacePrefix;
    	}else{
    		prefix = namespace.getPrefix();
    	}
    	Map map = new HashMap();
    	//添加命名空间
		map.put(prefix,namespace.getURI());
		x.setNamespaceURIs(map);
		return x;
    }
}
