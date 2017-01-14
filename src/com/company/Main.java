package com.company;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String FILE_PATH_75_2 = "C:\\";

    public static void main(String[] args) {
        /* иницилизация класса для обработки и валидации загруженного XML-файла */
        File file = new File(FILE_PATH_75_2);
        XmlFile xmlFile = null;
        try {
            xmlFile = new XmlFile(file);
         //   xmlFile.write();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(xmlFile == null) {
            System.out.println("Не удалось прочитать xml-файл.");
            return;
        }


        /* Иницилизация тестовых элементов, которые будут загружаться из базы данных */
        DbEntity root = new DbEntity().setId(1).setName("root").setChangesName(null).setParentTag(null).setChildTag(null).setPatternType(null);
        DbEntity document = new DbEntity().setId(2).setName("Document").setChangesName("Form").setParentTag(null).setChildTag(null).setPatternType(null);
        DbEntity powerFacilitiesVid = new DbEntity().setId(3).setName("powerFacilitiesVid").setChangesName("powerFacility").setParentTag(null).setChildTag("vid").setPatternType("^\\d+$");
        DbEntity periodID = new DbEntity().setId(4).setName("periodId").setChangesName(null).setParentTag(null).setChildTag(null).setPatternType("^\\d+$");
        DbEntity subdocuments = new DbEntity().setId(5).setName("subdocuments").setChangesName(null).setParentTag("item").setChildTag("BeanList").setPatternType(null);

        List<DbEntity> dbEntityList  = new ArrayList<>();
        dbEntityList.add(root);
        dbEntityList.add(document);
        dbEntityList.add(powerFacilitiesVid);
        dbEntityList.add(periodID);
        dbEntityList.add(subdocuments);


        /* Составление правил трансформации и валидации документа */

        List<XmlFile.Rules> rulesList = new ArrayList<>();
        for(DbEntity dbEntity : dbEntityList) {
            //сначала правила валидации
            if(dbEntity.patternType != null) {
                xmlFile.typeValidation(dbEntity.name, dbEntity.patternType);
            }
            //добавление новых тегов
            if(dbEntity.childTag != null) {
                //если тег содержит внутри себя другой тег, который нужно перенести в новый тег, то
                //в parentTag пишем его значение
                if(dbEntity.parentTag != null) {
                    xmlFile.addNewNode(dbEntity.name, dbEntity.childTag);
                    xmlFile.changeRootTag(dbEntity.parentTag, dbEntity.childTag);
                } else {
                //если тег содержит только текст, то просто переносим его
                    xmlFile.addNewTextNode(dbEntity.name, dbEntity.childTag);
                }
            }
            //изменяем имя тега
            if(dbEntity.changesName != null) {
                xmlFile.changeNodeName(dbEntity.name, dbEntity.changesName);
            }
        }

        /* Применение всех правил валидации и трансфорамции*/

        for(XmlFile.Rules rule : rulesList) {
            rule.runRule();
        }

        /* Вывод в консоль изменненго документа и лога, если в логе есть записи */

        try {
            xmlFile.write();
            if(!xmlFile.isValid()) {
                System.out.println();
                System.out.println(xmlFile.getValidationLog());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
