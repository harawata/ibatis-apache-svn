/*
 *  Copyright 2005 The Apache Software Foundation
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
package org.apache.ibatis.abator.core.internal.db;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeff Butler
 */
public class DatabaseDialects {
	public static final int DB2 = 1;

	public static final int MYSQL = 2;

	public static final int SQLSERVER = 3;

	private static final Map identityClauses;

	static {
		identityClauses = new HashMap();

		identityClauses.put(new Integer(DB2),
				"select identity_val_local() from sysibm.sysdummy1");
		identityClauses.put(new Integer(MYSQL), "select LAST_INSERT_ID()");
		identityClauses.put(new Integer(SQLSERVER), "select SCOPE_IDENTITY()");
	}

	public static String getIdentityClause(int dialect) {
		return (String) identityClauses.get(new Integer(dialect));
	}

	/**
	 *  
	 */
	private DatabaseDialects() {
		super();
	}

}
