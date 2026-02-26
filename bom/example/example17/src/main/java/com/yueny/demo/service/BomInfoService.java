package com.yueny.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

@Service
@Slf4j
public class BomInfoService {

    public Map<String, Object> getBomInfo(Set<String> bomFiles) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File pomFile = null;

            for (String filePath : bomFiles) {
                // 读取 BOM pom.xml 文件
                pomFile = new File(filePath);
                if (pomFile != null && pomFile.exists()) {
                    break;
                }
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();
            
            // 获取基本信息
            result.put("groupId", getTextContent(doc, "groupId"));
            result.put("artifactId", getTextContent(doc, "artifactId"));
            result.put("version", getTextContent(doc, "version"));
            result.put("name", getTextContent(doc, "name"));
            result.put("description", getTextContent(doc, "description"));
            
            // 获取 properties
            Map<String, String> properties = new HashMap<>();
            NodeList propertiesNodes = doc.getElementsByTagName("properties");
            if (propertiesNodes.getLength() > 0) {
                Element propertiesElement = (Element) propertiesNodes.item(0);
                NodeList propertyNodes = propertiesElement.getChildNodes();
                for (int i = 0; i < propertyNodes.getLength(); i++) {
                    Node node = propertyNodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        properties.put(node.getNodeName(), node.getTextContent());
                    }
                }
            }
            result.put("properties", properties);
            
            // 获取 dependencyManagement 中的依赖
            List<Map<String, String>> dependencies = new ArrayList<>();
            NodeList dependencyManagementNodes = doc.getElementsByTagName("dependencyManagement");
            if (dependencyManagementNodes.getLength() > 0) {
                Element dependencyManagement = (Element) dependencyManagementNodes.item(0);
                NodeList dependenciesNodes = dependencyManagement.getElementsByTagName("dependencies");
                if (dependenciesNodes.getLength() > 0) {
                    Element dependenciesElement = (Element) dependenciesNodes.item(0);
                    NodeList dependencyNodes = dependenciesElement.getElementsByTagName("dependency");
                    
                    for (int i = 0; i < dependencyNodes.getLength(); i++) {
                        Element dependency = (Element) dependencyNodes.item(i);
                        Map<String, String> depInfo = new HashMap<>();
                        
                        String groupId = getElementTextContent(dependency, "groupId");
                        String artifactId = getElementTextContent(dependency, "artifactId");
                        String version = getElementTextContent(dependency, "version");
                        String type = getElementTextContent(dependency, "type");
                        String scope = getElementTextContent(dependency, "scope");
                        
                        if (groupId != null) depInfo.put("groupId", groupId);
                        if (artifactId != null) depInfo.put("artifactId", artifactId);
                        if (version != null) depInfo.put("version", version);
                        if (type != null) depInfo.put("type", type);
                        if (scope != null) depInfo.put("scope", scope);
                        
                        dependencies.add(depInfo);
                    }
                }
            }
            result.put("dependencies", dependencies);
            
        } catch (Exception e) {
            log.error("读取 BOM 信息失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private String getTextContent(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
    
    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}
