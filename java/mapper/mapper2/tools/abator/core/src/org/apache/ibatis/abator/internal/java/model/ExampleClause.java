/*
 *  Copyright 2006 The Apache Software Foundation
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
package org.apache.ibatis.abator.internal.java.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.internal.db.ColumnDefinition;

/**
 * @author Jeff Butler
 */
public class ExampleClause {
    private static final int IS_NULL_CLAUSE_ID = 1;

    private static final int IS_NOT_NULL_CLAUSE_ID = 2;

    private static final int EQUALS_CLAUSE_ID = 3;

    private static final int NOT_EQUALS_CLAUSE_ID = 4;

    private static final int GREATER_THAN_CLAUSE_ID = 5;

    private static final int GREATER_THAN_OR_EQUAL_CLAUSE_ID = 6;

    private static final int LESS_THAN_CLAUSE_ID = 7;

    private static final int LESS_THAN_OR_EQUAL_CLAUSE_ID = 8;

    private static final int LIKE_CLAUSE_ID = 9;

    private static final List clauses;

    private String clause;

    private boolean propertyInMapRequired;

    private boolean characterOnly;

    private String examplePropertyName;

    private int examplePropertyValue;

    static {
        List list = new ArrayList();

        list.add(new ExampleClause("{0} is null", false, false, //$NON-NLS-1$
                "EXAMPLE_NULL", IS_NULL_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} is not null", false, false, //$NON-NLS-1$
                "EXAMPLE_NOT_NULL", IS_NOT_NULL_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} =", true, false, //$NON-NLS-1$
                "EXAMPLE_EQUALS", EQUALS_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} <>", true, false, //$NON-NLS-1$
                "EXAMPLE_NOT_EQUALS", NOT_EQUALS_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} >", true, false, //$NON-NLS-1$
                "EXAMPLE_GREATER_THAN", GREATER_THAN_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} >=", true, false, //$NON-NLS-1$
                "EXAMPLE_GREATER_THAN_OR_EQUAL", //$NON-NLS-1$
                GREATER_THAN_OR_EQUAL_CLAUSE_ID));
        list.add(new ExampleClause("{0} <", true, false, //$NON-NLS-1$
                "EXAMPLE_LESS_THAN", LESS_THAN_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} <=", true, false, //$NON-NLS-1$
                "EXAMPLE_LESS_THAN_OR_EQUAL", LESS_THAN_OR_EQUAL_CLAUSE_ID)); //$NON-NLS-1$
        list.add(new ExampleClause("{0} like", true, true, //$NON-NLS-1$
                "EXAMPLE_LIKE", LIKE_CLAUSE_ID)); //$NON-NLS-1$

        clauses = Collections.unmodifiableList(list);
    }

    public static Iterator getAllExampleClauses() {
        return clauses.iterator();
    }

    /**
     *  
     */
    private ExampleClause(String clause, boolean propertyInMapRequired,
            boolean characterOnly, String examplePropertyName,
            int examplePropertyValue) {
        super();
        this.clause = clause;
        this.propertyInMapRequired = propertyInMapRequired;
        this.characterOnly = characterOnly;
        this.examplePropertyName = examplePropertyName;
        this.examplePropertyValue = examplePropertyValue;
    }

    public String getClause(ColumnDefinition cd) {
        Object[] arguments = { cd.getAliasedColumnName() };

        return MessageFormat.format(clause, arguments);
    }

    public boolean isCharacterOnly() {
        return characterOnly;
    }

    public boolean isPropertyInMapRequired() {
        return propertyInMapRequired;
    }

    public String getExamplePropertyName() {
        return examplePropertyName;
    }

    public int getExamplePropertyValue() {
        return examplePropertyValue;
    }
}
