/*
 *  Copyright 2004 Clinton Begin
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
package com.ibatis.dao.engine.transaction.external;

import com.ibatis.dao.client.DaoTransaction;

/**
 * <p/>
 * Date: Feb 22, 2004 12:10:27 PM
 *
 * @author Clinton Begin
 */
public class ExternalDaoTransaction implements DaoTransaction {

  public void commit() {
    // Do nothing
  }

  public void rollback() {
    // Do nothing
  }

}
