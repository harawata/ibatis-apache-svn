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

import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;

/**
 * This class centralizes all the rules related to code generation - 
 * including the methods and objects to create, and certain attributes
 * related to those objects.
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
     * Implements the rule for determining whether to generate a primary key class.
     * If the physical table has a primary key, then we generate the class.
     * 
     * @param columnDefinitions the introspected table columns
     * @return true if the primary key should be generated
     */
	public static boolean generatePrimaryKey(ColumnDefinitions columnDefinitions) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
	    
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
	 * Implements the rule for generating a base record that extends the primary key.
	 * If the table has a primary key, and has non-BLOB non-Primary Key fields, then
	 * generate the class.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @return true if the base record class should be generated
	 */
	public static boolean generateBaseRecordExtendingPrimaryKey(ColumnDefinitions columnDefinitions) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
	 * Implements the rule for generating a base record with no super class.
	 * If the table does not have a primary key, then generate the class.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @return true if the base record class should be generated
	 */
	public static boolean generateBaseRecordWithNoSuperclass(ColumnDefinitions columnDefinitions) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
	 * Implements the rule for generating a record with BLOBs class extending the primary
	 * key class.  If the table has a primary key, but all other fields are BLOBs, then
	 * generate the class.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @return true if the record with BLOBs class should be generated
	 */
	public static boolean generateRecordWithBLOBsExtendingPrimaryKey(ColumnDefinitions columnDefinitions) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
		boolean rc;
		
		if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
		    rc = true;
		} else {
		    rc = false;
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating a record with BLOBs class extending the base
	 * record class.  If the table has non-BLOB non-Primary Key fields, then
	 * generate the class.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @return true if the record with BLOBs class should be generated
	 */
	public static boolean generateRecordWithBLOBsExtendingBaseRecord(ColumnDefinitions columnDefinitions) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
	 * Implements the rule for generating an example class extending the primary key.
	 * If there is a primary key, and there are no non-Primary Key non-Blob fields, then
	 * generate the class.  Also, the class should only be generated if the selectByExample
	 * or deleteByExample methods are allowed.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the example class should be generated
	 */
	public static boolean generateExampleExtendingPrimaryKey(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
		boolean rc;
		
		if (tableType == TableType.PK_NO_FIELDS_NO_BLOBS) {
		    rc = true;
		} else if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
		    rc = true;
		} else {
		    rc = false;
		}
		
		if (rc) {
			rc = tc.isSelectByExampleStatementEnabled()
				|| tc.isDeleteByExampleStatementEnabled();
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating an example class extending the base record.
	 * If there are non non-Primary Key non-Blob fields, then
	 * generate the class.  Also, the class should only be generated if the
	 * selectByExample or deleteByExample methods are allowed.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the example class should be generated
	 */
	public static boolean generateExampleExtendingBaseRecord(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
			rc = tc.isSelectByExampleStatementEnabled()
				|| tc.isDeleteByExampleStatementEnabled();
		}
		
		return rc;
	}

    // end of domain object generation rules
    // start of SqlMAP and DAO Method rules rules

	/**
	 * Implements the rule for generating the result map without BLOBs.  If
	 * either select method is allowed, then generate the result map.
	 *  
	 * @param tc the table configuration object
	 * @return true if the result map should be generated
	 */
	public static boolean generateResultMapWithoutBLOBs(TableConfiguration tc) {
		return tc.isSelectByExampleStatementEnabled()
			|| tc.isSelectByPrimaryKeyStatementEnabled();
	}

	/**
	 * Implements the rule for generating the result map with BLOBs.  If the table
	 * has BLOB columns, and either select method is allowed, then generate the
	 * result map.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the result map should be generated
	 */
	public static boolean generateResultMapWithBLOBs(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
			rc = tc.isSelectByExampleStatementEnabled()
				|| tc.isSelectByPrimaryKeyStatementEnabled();
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating the SQL example where clause element.
	 * Generate the element if either the selectByExample or deleteByExample
	 * statement is allowed.
	 * 
	 * @param tc the table configuration object
	 * @return true if the SQL where clause element should be generated
	 */
	public static boolean generateSQLExampleWhereClause(TableConfiguration tc) {
	    return tc.isSelectByExampleStatementEnabled()
	    	|| tc.isDeleteByExampleStatementEnabled();
	}

	/**
	 * Implements the rule for generating the select by example without BLOBs
	 * SQL Map element and DAO method.  If the selectByExample statement is allowed,
	 * then generate the element and method.
	 * 
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateSelectByExampleWithoutBLOBs(TableConfiguration tc) {
		return tc.isSelectByExampleStatementEnabled();
	}
	
	/**
	 * Implements the rule for generating the select by example with BLOBs
	 * SQL Map element and DAO method.  If the table has BLOB fields and the
	 * selectByExample statement is allowed, then generate the element and
	 * method.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateSelectByExampleWithBLOBs(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
			rc = tc.isSelectByExampleStatementEnabled();
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating the select by primary key
	 * SQL Map element and DAO method.  If the table has a primary key as well
	 * as other fields, and the
	 * selectByPrimaryKey statement is allowed, then generate the element and
	 * method.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateSelectByPrimaryKey(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
			rc = tc.isSelectByPrimaryKeyStatementEnabled();
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating the insert
	 * SQL Map element and DAO method.  If the insert statement is allowed,
	 * then generate the element and method.
	 * 
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateInsert(TableConfiguration tc) {
		return tc.isInsertStatementEnabled();
	}
	
	/**
	 * Implements the rule for generating the update by primary key
	 * without BLOBs SQL Map element and DAO method.  If the table has a primary key as well
	 * as other non-BLOB fields, and the
	 * updateByPrimaryKey statement is allowed, then generate the element and
	 * method.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateUpdateByPrimaryKeyWithoutBLOBs(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
		boolean rc;
		
		if (tableType == TableType.PK_FIELDS_NO_BLOBS) {
		    rc = true;
		} else if (tableType == TableType.PK_FIELDS_BLOBS) {
		    rc = true;
		} else {
		    rc = false;
		}
		
		if (rc) {
			rc = tc.isUpdateByPrimaryKeyStatementEnabled();
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating the update by primary key
	 * with BLOBs SQL Map element and DAO method.  If the table has a primary key as well
	 * as other BLOB fields, and the
	 * updateByPrimaryKey statement is allowed, then generate the element and
	 * method.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateUpdateByPrimaryKeyWithBLOBs(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
		boolean rc;
		
		if (tableType == TableType.PK_NO_FIELDS_BLOBS) {
		    rc = true;
		} else if (tableType == TableType.PK_FIELDS_BLOBS) {
		    rc = true;
		} else {
		    rc = false;
		}

		if (rc) {
			rc = tc.isUpdateByPrimaryKeyStatementEnabled();
		}
		
		return rc;
	}
	
	/**
	 * Implements the rule for generating the delete by primary key
	 * SQL Map element and DAO method.  If the table has a primary key, and the
	 * deleteByPrimaryKey statement is allowed, then generate the element and
	 * method.
	 * 
     * @param columnDefinitions the introspected table columns
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateDeleteByPrimaryKey(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
	    TableType tableType = TableType.calculateTableType(columnDefinitions);
		
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
			rc = tc.isDeleteByPrimaryKeyStatementEnabled();
		}
		
		return rc;
	}

	/**
	 * Implements the rule for generating the delete by example
	 * SQL Map element and DAO method.  If the deleteByExample statement
	 * is allowed, then generate the element and method.
	 * 
	 * @param tc the table configuration object
	 * @return true if the element and method should be generated
	 */
	public static boolean generateDeleteByExample(TableConfiguration tc) {
		return tc.isDeleteByExampleStatementEnabled();
	}
}
