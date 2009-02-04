/*
 *  Copyright 2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ibatis.ibator.generator.ibatis2.sqlmap.elements;

import org.apache.ibatis.ibator.api.IntrospectedColumn;
import org.apache.ibatis.ibator.api.dom.xml.Attribute;
import org.apache.ibatis.ibator.api.dom.xml.TextElement;
import org.apache.ibatis.ibator.api.dom.xml.XmlElement;
import org.apache.ibatis.ibator.generator.ibatis2.XmlConstants;

/**
 * 
 * @author Jeff Butler
 *
 */
public class BlobColumnListElementGenerator extends AbstractXmlElementGenerator {

    public BlobColumnListElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("sql"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                XmlConstants.BLOB_COLUMN_LIST_ID));

        ibatorContext.getCommentGenerator().addComment(answer);

        boolean comma = false;
        StringBuilder sb = new StringBuilder();
        
        for (IntrospectedColumn introspectedColumn : introspectedTable.getBLOBColumns()) {
            if (comma) {
                sb.append(", "); //$NON-NLS-1$
            } else {
                comma = true;
            }

            sb.append(introspectedColumn.getSelectListPhrase());
        }
        
        answer.addElement((new TextElement(sb.toString())));

        if (ibatorContext.getPlugins().sqlMapBlobColumnListElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
