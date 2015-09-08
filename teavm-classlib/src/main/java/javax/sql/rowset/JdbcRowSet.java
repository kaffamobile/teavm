/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.sql.rowset;

import javax.sql.RowSet;

import org.teavm.classlib.java.sql.TResultSet;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSavepoint;

public interface JdbcRowSet extends Joinable, TResultSet, RowSet {
    boolean getShowDeleted() throws TSQLException;

    void setShowDeleted(boolean b) throws TSQLException;

    RowSetWarning getRowSetWarnings() throws TSQLException;

    void commit() throws TSQLException;

    boolean getAutoCommit() throws TSQLException;

    void setAutoCommit(boolean autoCommit) throws TSQLException;

    void rollback() throws TSQLException;

    void rollback(TSavepoint s) throws TSQLException;
}
