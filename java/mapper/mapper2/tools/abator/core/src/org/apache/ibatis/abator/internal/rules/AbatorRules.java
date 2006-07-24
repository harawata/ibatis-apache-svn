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
package org.apache.ibatis.abator.internal.rules;

import org.apache.ibatis.abator.api.IntrospectedTable;
import org.apache.ibatis.abator.api.TableType;

/**
 * This class centralizes all the rules related to code generation - including
 * the methods and objects to create, and certain attributes related to those
 * objects.
 * 
 * See package JavaDoc for more information.
 * 
 * @author Jeff Butler
 */
public class AbatorRules {

    /**
     * Utility class - no instances allowed.
     */
    private AbatorRules() {
        super();
    }

    // start of domain object generation rules

    /**
     * Implements the rule for determining whether to generate a primary key
     * class. If the physical table has a primary key, then we generate the
     * class.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the primary key should be generated
     */
    public static boolean generatePrimaryKey(IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_NO_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        return rc;
    }

    /**
     * Implements the rule for generating a base record that extends the primary
     * key. If the table has a primary key, and has non-BLOB non-Primary Key
     * fields, then generate the class.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the base record class should be generated
     */
    public static boolean generateBaseRecordExtendingPrimaryKey(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        return rc;
    }

    /**
     * Implements the rule for generating a base record with no super class. If
     * the table does not have a primary key, then generate the class.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the base record class should be generated
     */
    public static boolean generateBaseRecordWithNoSuperclass(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.NO_PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.NO_PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        return rc;
    }

    /**
     * Implements the rule for generating a record with BLOBs class extending
     * the primary key class. If the table has a primary key, but all other
     * fields are BLOBs, then generate the class.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the record with BLOBs class should be generated
     */
    public static boolean generateRecordWithBLOBsExtendingPrimaryKey(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        return rc;
    }

    /**
     * Implements the rule for generating a record with BLOBs class extending
     * the base record class. If the table has non-BLOB non-Primary Key fields,
     * then generate the class.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the record with BLOBs class should be generated
     */
    public static boolean generateRecordWithBLOBsExtendingBaseRecord(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.NO_PK_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        return rc;
    }

    /**
     * Implements the rule for generating an example class extending the primary
     * key. If there is a primary key, and there are no non-Primary Key non-Blob
     * fields, then generate the class. Also, the class should only be generated
     * if the selectByExample or deleteByExample methods are allowed. This
     * method only applies to the "Legacy" generator set.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the example class should be generated
     */
    public static boolean generateExampleExtendingPrimaryKey(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_NO_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = generateExample(introspectedTable);
        }

        return rc;
    }

    /**
     * Implements the rule for generating an example class extending the base
     * record. If there are non non-Primary Key non-Blob fields, then generate
     * the class. Also, the class should only be generated if the
     * selectByExample or deleteByExample methods are allowed. This method only
     * applies to the "Legacy" generator set.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the example class should be generated
     */
    public static boolean generateExampleExtendingBaseRecord(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.NO_PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.NO_PK_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = generateExample(introspectedTable);
        }

        return rc;
    }

    /**
     * Implements the rule for generating an example class with no super class.
     * The class should only be generated if the
     * selectByExample or deleteByExample methods are allowed. This method only
     * applies to the "Java2" and "Java5" generator sets.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the example class should be generated
     */
    public static boolean generateExample(IntrospectedTable introspectedTable) {
        boolean rc = introspectedTable.isSelectByExampleStatementEnabled()
                || introspectedTable.isDeleteByExampleStatementEnabled();

        return rc;
    }

    // end of domain object generation rules
    // start of SqlMAP and DAO Method rules rules

    /**
     * Implements the rule for generating the result map without BLOBs. If
     * either select method is allowed, then generate the result map.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the result map should be generated
     */
    public static boolean generateResultMapWithoutBLOBs(
            IntrospectedTable introspectedTable) {
        return introspectedTable.isSelectByExampleStatementEnabled()
                || introspectedTable.isSelectByPrimaryKeyStatementEnabled();
    }

    /**
     * Implements the rule for generating the result map with BLOBs. If the
     * table has BLOB columns, and either select method is allowed, then
     * generate the result map.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the result map should be generated
     */
    public static boolean generateResultMapWithBLOBs(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.NO_PK_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = introspectedTable.isSelectByExampleStatementEnabled()
                    || introspectedTable.isSelectByPrimaryKeyStatementEnabled();
        }

        return rc;
    }

    /**
     * Implements the rule for generating the SQL example where clause element.
     * Generate the element if either the selectByExample or deleteByExample
     * statement is allowed.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the SQL where clause element should be generated
     */
    public static boolean generateSQLExampleWhereClause(
            IntrospectedTable introspectedTable) {
        return introspectedTable.isSelectByExampleStatementEnabled()
                || introspectedTable.isDeleteByExampleStatementEnabled();
    }

    /**
     * Implements the rule for generating the select by example without BLOBs
     * SQL Map element and DAO method. If the selectByExample statement is
     * allowed, then generate the element and method.
     * 
     * @param tc
     *            the table configuration object
     * @return true if the element and method should be generated
     */
    public static boolean generateSelectByExampleWithoutBLOBs(
            IntrospectedTable introspectedTable) {
        return introspectedTable.isSelectByExampleStatementEnabled();
    }

    /**
     * Implements the rule for generating the select by example with BLOBs SQL
     * Map element and DAO method. If the table has BLOB fields and the
     * selectByExample statement is allowed, then generate the element and
     * method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateSelectByExampleWithBLOBs(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.NO_PK_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = introspectedTable.isSelectByExampleStatementEnabled();
        }

        return rc;
    }

    /**
     * Implements the rule for generating the select by primary key SQL Map
     * element and DAO method. If the table has a primary key as well as other
     * fields, and the selectByPrimaryKey statement is allowed, then generate
     * the element and method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateSelectByPrimaryKey(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = introspectedTable.isSelectByPrimaryKeyStatementEnabled();
        }

        return rc;
    }

    /**
     * Implements the rule for generating the insert SQL Map element and DAO
     * method. If the insert statement is allowed, then generate the element and
     * method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateInsert(IntrospectedTable introspectedTable) {
        return introspectedTable.isInsertStatementEnabled();
    }

    /**
     * Implements the rule for generating the update by primary key without
     * BLOBs SQL Map element and DAO method. If the table has a primary key as
     * well as other non-BLOB fields, and the updateByPrimaryKey statement is
     * allowed, then generate the element and method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateUpdateByPrimaryKeyWithoutBLOBs(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = introspectedTable.isUpdateByPrimaryKeyStatementEnabled();
        }

        return rc;
    }

    /**
     * Implements the rule for generating the update by primary key with BLOBs
     * SQL Map element and DAO method. If the table has a primary key as well as
     * other BLOB fields, and the updateByPrimaryKey statement is allowed, then
     * generate the element and method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateUpdateByPrimaryKeyWithBLOBs(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = introspectedTable.isUpdateByPrimaryKeyStatementEnabled();
        }

        return rc;
    }

    /**
     * Implements the rule for generating the delete by primary key SQL Map
     * element and DAO method. If the table has a primary key, and the
     * deleteByPrimaryKey statement is allowed, then generate the element and
     * method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateDeleteByPrimaryKey(
            IntrospectedTable introspectedTable) {
        TableType tableType = introspectedTable.getTableType();

        boolean rc;

        if (tableType == TableType.PK_NO_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
            rc = true;
        } else if (tableType == TableType.PK_FIELDS_BLOBS) {
            rc = true;
        } else {
            rc = false;
        }

        if (rc) {
            rc = introspectedTable.isDeleteByPrimaryKeyStatementEnabled();
        }

        return rc;
    }

    /**
     * Implements the rule for generating the delete by example SQL Map element
     * and DAO method. If the deleteByExample statement is allowed, then
     * generate the element and method.
     * 
     * @param introspectedTable
     *            the introspected table
     * @return true if the element and method should be generated
     */
    public static boolean generateDeleteByExample(
            IntrospectedTable introspectedTable) {
        return introspectedTable.isDeleteByExampleStatementEnabled();
    }
}
