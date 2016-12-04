package com.company;

import java.io.File;

public class Main {

    private static final String FILE_PATH_75_2 = "C:\\Users\\basic\\Desktop\\Клиентские\\75-2_client.xml";
    private static final String FILE_PATH_75_3 = "C:\\Users\\basic\\Desktop\\Клиентские\\75-3_client.xml";
    private static final String FILE_PATH_75_4 = "C:\\Users\\basic\\Desktop\\Клиентские\\75-4_client.xml";

    public static void main(String[] args) {
        File file = new File(FILE_PATH_75_2);
        XmlFile xmlFile;
        try {
            xmlFile = new XmlFile(file);
            xmlFile.write();
            xmlFile.addTextNode(XmlFile.Tag.NOTE, XmlFile.Tag.NEW_TEST)
            .applyChanges();
/*
           xmlFile.addNewTag(XmlFile.Tag.SUBDOCUMENTS, XmlFile.Tag.TEST_TAG)
                    .changeRootElement(XmlFile.Tag.ITEM, XmlFile.Tag.TEST_TAG)
                    .changeTag(XmlFile.Tag.TEST_TAG, XmlFile.Tag.BEAN_LIST)
                    .changeTag(XmlFile.Tag.CODE, XmlFile.Tag.NEW_TEST)
                    .addNewTag(XmlFile.Tag.POE_EQUIP, XmlFile.Tag.CODE)
                    .changeRootElement(XmlFile.Tag.NEW_TEST, XmlFile.Tag.CODE)
                    .applyChanges();
*/


            xmlFile.addTypeValidation(XmlFile.Tag.ALL_TAGS, "^\\d+$")
                    .applyChanges();

/*            xmlFile.changeTag(XmlFile.Tag.CODE, XmlFile.Tag.NEW_TEST)
                    .addNewTag(XmlFile.Tag.POE_EQUIP, XmlFile.Tag.CODE)
                   .changeRootElement(XmlFile.Tag.NEW_TEST, XmlFile.Tag.CODE)
                    .applyChanges();*/
//            xmlFile.changeTag(XmlFile.Tag.NOTE, XmlFile.Tag.TEST_TAG)
//                    .addNewTag(XmlFile.Tag.POE_TYPE_CODE, XmlFile.Tag.NEW_TEST)
//                    .applyChanges();
            xmlFile.write();
            System.out.println(xmlFile.getValidationLog());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
