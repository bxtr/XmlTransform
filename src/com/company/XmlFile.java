package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by bxtr on 03.12.2016.
 */

public class XmlFile {

    private static final String CLIENT_DOCUMENT_TAG = "Document";

    private Document downloadedXML;
    private List<Rules> rulesList;
    private XmlValidationLog validationLog;

    public XmlFile(File file) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document downloadedXML = documentBuilder.parse(file);
        downloadedXML.getDocumentElement().normalize();

        this.downloadedXML = downloadedXML;
        rulesList = new ArrayList<>();
        validationLog = new XmlValidationLog();
    }


    public void applyChanges() {
        if (rulesList.size() == 0) {
            return;
        }
        for(Rules rule : rulesList) {
            if(rule instanceof TransformRule) {
                TransformRule transformRule = (TransformRule) rule;
                NodeList applyingNodes = downloadedXML.getElementsByTagName(transformRule.getApplyingTag().getTagName());
                if (applyingNodes.getLength() > 0) {
                    for (int index = applyingNodes.getLength() - 1; index >= 0; index--) {
                        transformRule.apply(applyingNodes.item(index));
                    }
                }
            }
            if (rule instanceof ValidationRule) {
                ValidationRule validationRule = (ValidationRule) rule;
                NodeList applyingNodes = downloadedXML.getElementsByTagName(validationRule.getTargetTag().getTagName());
                if (applyingNodes.getLength() > 0) {
                    for (int index = applyingNodes.getLength() - 1; index >= 0; index--) {
                        validationRule.apply(applyingNodes.item(index));
                    }
                }
            }
        }
        rulesList = new ArrayList<>();
    }

    //меняет название тега
    public XmlFile changeTag(Tag oldTag, Tag newTag) {
        rulesList.add(new ChangeTagNameRule(oldTag, newTag));
        return this;
    }

    //добавляет пустой тег
    public XmlFile addNewTag(Tag rootTag, Tag newTag) {
        rulesList.add(new AddNewTagRule(rootTag, newTag));
        return this;
    }

    //Меняет рутовый тег. Нормально работает если рутовый элемент один. Так же работает если количество элементов
    // и количество рутовых элементов одинаковое, но в этом случае, его следует использовать только если не возможно достичь
    // желаемого результата другими методами, тк он сложный, и скорее всего долгий. Если количество элементов и рутовых
    // элементов разное - результат не известен.
    public XmlFile changeRootElement(Tag nodeTag, Tag parentTag) {
        rulesList.add(new ChangeRootTagRule(nodeTag, parentTag));
        return this;
    }

    //Добавляет вокруг текста новый тег
    public XmlFile addTextNode(Tag targetTag, Tag newTag) {
        rulesList.add(new AddNewTextNodeRule(targetTag, newTag));
        return this;
    }

    public XmlFile addTypeValidation(Tag targetTag, String pattern) {
        rulesList.add(new TypeValidationRule(targetTag, pattern));
        return this;
    }


    public void write() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(downloadedXML);
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);
    }

    public String getValidationLog() {
        return validationLog.getValidationLog();
    }

    public boolean isValid() {
        return validationLog.isValid();
    }

    enum Tag {
        DOCUMENT("Document"), FORM("form"), PERIOD_ID("periodId"), POWER_FACILITIES_VID("powerFacilitiesVid"),
        ESTIMATED_START("estimatedStart"), ESTIMATED_END("estimatedEnd"), REPAIR_KINDS_ID("repairKindsid"),
        NOTE("note"), POE_TYPE_CODE("poeTypeCode"), POE_EQUIP("poeEquip"), CODE("code"), ITEM("item"),
        SUBDOCUMENTS("subdocuments"), TEST_TAG("test_tag"), NEW_TEST("new_test"), BEAN_LIST("bean_list"),
        ALL_TAGS("*");

        private String tagName;

        Tag(String tagName) {
            this.tagName = tagName;
        }

        public String getTagName() {
            return tagName;
        }
    }

    interface Rules {
        void apply(Node node);
    }

    abstract class TransformRule implements Rules {
        protected Tag applyingTag;

        public abstract void apply(Node node);

        public TransformRule(Tag applyingTag) {
            this.applyingTag = applyingTag;
        }

        public Tag getApplyingTag() {
            return applyingTag;
        }

        protected boolean check(Node node) {
            return node.getNodeName().equals(applyingTag.getTagName());
        }
    }

    private class ChangeTagNameRule extends TransformRule {
        private Tag newTag;

        private ChangeTagNameRule(Tag oldTag, Tag newTag) {
            super(oldTag);
            this.newTag = newTag;
        }

        @Override
        public void apply(Node node) {
            if(check(node)) {
                Node temp = downloadedXML.createElement(newTag.getTagName());
                node.getParentNode().replaceChild(temp, node);
                if (node.hasChildNodes()) {
                    NodeList childNodeList = node.getChildNodes();
                    for(int index = 0; childNodeList.getLength() > 0 ; ) {
                        temp.appendChild(childNodeList.item(index));
                    }
                }
            }
        }
    }

    private class AddNewTagRule extends TransformRule {
        private Tag newTag;

        private AddNewTagRule(Tag rootTag, Tag newTag) {
            super(rootTag);
            this.newTag = newTag;
        }

        @Override
        public void apply(Node node) {
            if(check(node)) {
                Node temp = downloadedXML.createElement(newTag.getTagName());
                node.appendChild(temp);
            }
        }
    }

    private class ChangeRootTagRule extends TransformRule {
        private Tag newParentTag;

        private ChangeRootTagRule(Tag nodeTag, Tag newParentTag) {
            super(nodeTag);
            this.newParentTag = newParentTag;
        }

        @Override
        public void apply(Node node) {
            if(check(node)) {
                    NodeList parentNodes = downloadedXML.getElementsByTagName(newParentTag.getTagName());
                    if (parentNodes.getLength() == 1) {
                        if (node.hasChildNodes()) {
                            NodeList childNodeList = node.getChildNodes();
                            Node parentNode = downloadedXML.getElementsByTagName(newParentTag.getTagName()).item(0);
                            Node tempNode = downloadedXML.createElement(node.getNodeName());
                            for (int index = 0; childNodeList.getLength() > 0; ) {
                                tempNode.appendChild(childNodeList.item(index));
                            }
                            parentNode.appendChild(tempNode);
                            node.getParentNode().removeChild(node);
                        }
                    }
                    for (int parentIndex = parentNodes.getLength() - 1; parentIndex >= 0; parentIndex--) {
                        Node parentNode = parentNodes.item(parentIndex);
                        if(parentNode.hasChildNodes()) {
                            if (parentNode.getLastChild() != null &&
                                    !parentNode.getLastChild().getNodeName().equals(node.getNodeName())) {
                                if (node.hasChildNodes()) {
                                    NodeList childNodeList = node.getChildNodes();
                                    Node tempNode = downloadedXML.createElement(node.getNodeName());
                                    for (int index = 0; childNodeList.getLength() > 0; ) {
                                        tempNode.appendChild(childNodeList.item(index));
                                    }
                                    parentNode.appendChild(tempNode);
                                    node.getParentNode().removeChild(node);
                                }
                                break;
                            }
                        } else {
                            if (node.hasChildNodes()) {
                                NodeList childNodeList = node.getChildNodes();
                                Node tempNode = downloadedXML.createElement(node.getNodeName());
                                for (int index = 0; childNodeList.getLength() > 0; ) {
                                    tempNode.appendChild(childNodeList.item(index));
                                }
                                parentNode.appendChild(tempNode);
                                node.getParentNode().removeChild(node);
                            }
                        }
                    }
            }
        }
    }

    private class AddNewTextNodeRule extends TransformRule {
        private Tag newTextTag;

        private AddNewTextNodeRule(Tag targetTextNode, Tag newTextTag) {
            super(targetTextNode);
            this.newTextTag = newTextTag;
        }

        @Override
        public void apply(Node node) {
            if(check(node)) {
                Node temp = downloadedXML.createElement(newTextTag.getTagName());
                temp.setTextContent(node.getTextContent());
                node.setTextContent("");
                node.appendChild(temp);
            }
        }
    }

    abstract class ValidationRule implements Rules {
        protected Tag targetTag;

        public abstract void apply(Node node);

        public ValidationRule(Tag targetTag) {
            this.targetTag = targetTag;
        }

        public Tag getTargetTag() {
            return targetTag;
        }

        protected boolean check(Node node) {
            return node.getNodeName().equals(targetTag.getTagName());
        }


    }

    enum ValidationError {
        TYPE_ERROR("Тип не соответствует заданному. Ошибка в теге: ");

        private String errorText;

        ValidationError(String errorText) {
            this.errorText = errorText;
        }

        public String getMessage(String tagName) {
            return errorText + tagName;
        }
    }

    private class TypeValidationRule extends ValidationRule {
        private String pattern;

        private TypeValidationRule(Tag targetTag, String pattern) {
            super(targetTag);
            this.pattern = pattern;
        }

        @Override
        public void apply(Node node) {
            if(!Pattern.matches(pattern, node.getTextContent())){
                validationLog.addLogMessage(ValidationError.TYPE_ERROR.getMessage(node.getNodeName()));
            }
        }
    }

    private class XmlValidationLog {
        private Set<String> buffer;
        private boolean valid;

        private XmlValidationLog() {
            buffer = new HashSet<>();
            valid = true;
        }

        public void addLogMessage(String logMessage) {
            buffer.add(logMessage);
            valid = false;
        }

        public String getValidationLog() {
            StringBuffer temp = new StringBuffer();
            for(String message : buffer) {
                temp.append(message);
                temp.append("\n");
            }
            return temp.toString();
        }

        public boolean isValid() {
            return valid;
        }
    }

}