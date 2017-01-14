package com.company;

import org.w3c.dom.Document;
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
 * Класс представляющий собой xml-документ с метода для его изменения.
 */
public class XmlFile {

    /**
     * Загруженный xml-документ.
     */
    private Document downloadedXML;

    /**
     * Лог, в который пишутся сообщения во время валидации.
     */
    private XmlValidationLog validationLog;

    /**
     * Лист хранящий правила трансформации и валидации для последующего запуска.
     */
    private List<Rules> rulesList;

    /**
     * Конструктор.
     * @param File на загруженный xml-файл
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public XmlFile(File file) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document downloadedXML = documentBuilder.parse(file);
        downloadedXML.getDocumentElement().normalize();
        rulesList = new ArrayList<>();
        this.downloadedXML = downloadedXML;
        validationLog = new XmlValidationLog();
    }

    /**
     * Метод выводящий в консоль xml-файл.
     * @throws Exception
     */
    public void write() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(downloadedXML);
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);
    }

    /**
     * Возвращает лог валидации xml-документа.
     * @return все сообщений из лога в виде одной строки.
     */
    public String getValidationLog() {
        return validationLog.getValidationLog();
    }

    /**
     * Были ли ошибки при валидации документа?
     * @return веренет true, если ошибок не было, и false иначе.
     */
    public boolean isValid() {
        return validationLog.isValid();
    }

    /**
     * Запуск добавленных правил трансформации и валидации.
     */
    public void execute() {
        for(XmlFile.Rules rule : rulesList) {
            rule.runRule();
        }
        rulesList = new ArrayList<>();
    }

    /**
     * Ошибки валидации.
     */
    enum ValidationError {
        TYPE_ERROR("Тип не соответствует заданному. Ошибка в теге: %s."),
        CROSS_VALIDATION_ERROR("Значения %s и %s не совпадают.");

        private String errorText;

        ValidationError(String errorText) {
            this.errorText = errorText;
        }

        public String getMessage(String tagName) {
            return String.format(errorText, tagName);
        }

        public String getMessage(String firstTagName, String secondTagName) {
            return String.format(errorText, firstTagName, secondTagName);
        }
    }

    /**
     * Лог для валидации xml-документа.
     */
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


        /*                                                                         */
        /* Методы позволяющие получить правила трансформации и валидации XML-файла */
        /*                                                                         */

    /**
     * Меняет название тега.
     * @param targetNodeName название тега к которому надо применить правило.
     * @param nodeNewName название нового тега.
     * @return этот xml-документ.
     */
    public XmlFile changeNodeName(String targetNodeName, String nodeNewName){
        rulesList.add(new ChangeNodeNameRule(targetNodeName, nodeNewName));
        return this;
    }

    /**
     * Добавляет пустой тег с именем newNodeName в ноду с именем targetNodeName.
     * @param targetNodeName название тега к которому надо применить правило.
     * @param nodeNewName название нового тега.
     * @return этот xml-документ.
     */
    public XmlFile addNewNode(String targetNodeName, String newNodeName) {
        rulesList.add(new AddNewNodeRule(targetNodeName, newNodeName));
        return this;
    }

    /**
     * Меняет рутовый тег. Нормально работает если рутовый элемент один. Так же работает если количество элементов
     * и количество рутовых элементов одинаковое, но в этом случае, его следует использовать только если не возможно достичь
     * желаемого результата другими методами, тк он сложный, и скорее всего долгий. Если количество элементов и рутовых
     * элементов разное - то будут переименована часть тегов, начиная с конца документа.
     * @param targetNodeName название тега к которому надо применить правило.
     * @param newParentNodeName название нового тега.
     * @return этот xml-документ.
     */
    public XmlFile changeRootTag(String targetNodeName, String newParentNodeName) {
        rulesList.add(new ChangeRootTagRule(targetNodeName, newParentNodeName));
        return this;
    }

    /**
     * Добавляет вокруг текста ноды с именем targetTextNodeName новый тег с именем newNodeTextName.
     * @param targetTextNodeName название тега к которому надо применить правило.
     * @param newNodeTextName название нового тега.
     * @return этот xml-документ.
     */
    public XmlFile addNewTextNode(String targetTextNodeName, String newNodeTextName) {
        rulesList.add(new AddNewTextNodeRule(targetTextNodeName, newNodeTextName));
        return this;
    }

    /**
     * Валидирует по заданному паттерну, и в случае провала выдает ошибку о неверном типе данных.
     * @param targetTextNodeName название тега к которому надо применить правило.
     * @param pattern название нового тега.
     * @return этот xml-документ.
     */
    public XmlFile typeValidation(String targetTextNodeName, String pattern) {
        rulesList.add(new TypeValidationRule(targetTextNodeName, pattern));
        return this;
    }

    /**
     * Ищет вхождение текста из fromTextNodeName в тексте ноды с именем targetTextNodeName.
     * @param targetTextNodeName название тега к которому надо применить правило.
     * @param fromTextNodeName название нового тега.
     * @return этот xml-документ.
     */
    public XmlFile crossValidation(String targetTextNodeName, String fromTextNodeName) {
        rulesList.add(new CrossValidationRule(targetTextNodeName, fromTextNodeName));
        return this;
    }

        /*                                                                         */
        /*              Правила для трансформации и валидации XML-файла.           */
        /*                                                                         */

    /**
     * Общий интерфейс для всех правил.
     */
    interface Rules {
        /**
         * Метод должен содержать обработку выбранной ноды.
         * @param node
         */
        void apply(Node node);

        /**
         * Метод для запуска правила.
         */
        void runRule();
    }

    /**
     * Абстрактный класс для правил. Содержит код запуска правил и хранит имя выбранного тега XML-документа.
     */
    abstract class AbstractRunnableRule implements Rules {
        private String targetNodeName;

        public AbstractRunnableRule(String targetNodeName) {
            this.targetNodeName = targetNodeName;
        }

        public String getTargetNodeName() {
            return targetNodeName;
        }

        @Override
        public abstract void apply(Node node);

        @Override
        public void runRule() {
            NodeList applyingNodes = downloadedXML.getElementsByTagName(targetNodeName);
            if (applyingNodes.getLength() > 0) {
                for (int index = applyingNodes.getLength() - 1; index >= 0; index--) {
                    apply(applyingNodes.item(index));
                }
            }
        }
    }

    private class ChangeNodeNameRule extends AbstractRunnableRule {
        private String nodeNewName;

        private ChangeNodeNameRule(String targetNodeName, String nodeNewName) {
            super(targetNodeName);
            this.nodeNewName = nodeNewName;
        }

        @Override
        public void apply(Node node) {
            Node temp = downloadedXML.createElement(nodeNewName);
            node.getParentNode().replaceChild(temp, node);
            if (node.hasChildNodes()) {
                NodeList childNodeList = node.getChildNodes();
                for(int index = 0; childNodeList.getLength() > 0 ; ) {
                    temp.appendChild(childNodeList.item(index));
                }
            }
        }
    }

    private class AddNewNodeRule extends AbstractRunnableRule {
        private String newNodeName;

        private AddNewNodeRule(String addToNodeName, String newNodeName) {
            super(addToNodeName);
            this.newNodeName = newNodeName;
        }

        @Override
        public void apply(Node node) {
            Node temp = downloadedXML.createElement(newNodeName);
            node.appendChild(temp);
        }
    }

    private class ChangeRootTagRule extends AbstractRunnableRule {
        private String newParentNodeName;

        private ChangeRootTagRule(String targetNodeName, String newParentNodeName) {
            super(targetNodeName);
            this.newParentNodeName = newParentNodeName;
        }

        @Override
        public void apply(Node node) {
            NodeList parentNodes = downloadedXML.getElementsByTagName(newParentNodeName);
            if (parentNodes.getLength() == 1) {
                if (node.hasChildNodes()) {
                    NodeList childNodeList = node.getChildNodes();
                    Node parentNode = downloadedXML.getElementsByTagName(newParentNodeName).item(0);
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

    private class AddNewTextNodeRule extends AbstractRunnableRule {
        private String newTextTag;

        private AddNewTextNodeRule(String targetTextNodeName, String newNodeTextName) {
            super(targetTextNodeName);
            this.newTextTag = newNodeTextName;
        }

        @Override
        public void apply(Node node) {
            Node temp = downloadedXML.createElement(newTextTag);
            temp.setTextContent(node.getTextContent());
            node.setTextContent("");
            node.appendChild(temp);
        }
    }

    private class TypeValidationRule extends AbstractRunnableRule {
        private String pattern;

        private TypeValidationRule(String targetTextNodeName, String pattern) {
            super(targetTextNodeName);
            this.pattern = pattern;
        }

        @Override
        public void apply(Node node) {
            if(!Pattern.matches(pattern, node.getTextContent())) {
                validationLog.addLogMessage(ValidationError.TYPE_ERROR.getMessage(node.getNodeName()));
            }
        }
    }

    private class CrossValidationRule extends AbstractRunnableRule {
        private String fromTextNodeName;

        private CrossValidationRule(String targetTextNodeName, String fromTextNodeName) {
            super(targetTextNodeName);
            this.fromTextNodeName = fromTextNodeName;
        }

        @Override
        public void apply(Node node) {
            String textToFound = downloadedXML.getElementsByTagName(fromTextNodeName)
                    .item(0).getTextContent();
            if(!Pattern.matches(".*"+textToFound+".*", node.getTextContent())) {
                validationLog.addLogMessage(ValidationError.CROSS_VALIDATION_ERROR
                        .getMessage(fromTextNodeName, node.getNodeName()));
            }

        }
    }

}