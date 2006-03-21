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
	
	public static boolean generateRecordExtendingPrimaryKey(ColumnDefinitions columnDefinitions) {
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
	
	public static boolean generateRecordExtendingNothing(ColumnDefinitions columnDefinitions) {
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

	public static boolean generateRecordWithBLOBsExtendingRecord(ColumnDefinitions columnDefinitions) {
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

	public static boolean generateExampleExtendingRecord(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
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
	
	public static boolean generateBaseResultMap(TableConfiguration tc) {
		return tc.isSelectByExampleStatementEnabled()
			|| tc.isSelectByPrimaryKeyStatementEnabled();
	}
	
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

	public static boolean generateSelectByExample(TableConfiguration tc) {
		return tc.isSelectByExampleStatementEnabled();
	}
	
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

	public static boolean generateInsert(TableConfiguration tc) {
		return tc.isInsertStatementEnabled();
	}
	
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
	
	public static boolean generateUpdateByPrimaryKey(ColumnDefinitions columnDefinitions, TableConfiguration tc) {
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
	
	public static boolean generateDeleteByExample(TableConfiguration tc) {
		return tc.isDeleteByExampleStatementEnabled();
	}
}
