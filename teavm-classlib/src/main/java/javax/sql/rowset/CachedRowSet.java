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

import java.util.Collection;

import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;

import org.teavm.classlib.java.sql.TConnection;
import org.teavm.classlib.java.sql.TResultSet;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSavepoint;

public interface CachedRowSet extends Joinable, TResultSet, RowSet {
    boolean COMMIT_ON_ACCEPT_CHANGES = true;

    void populate(TResultSet data) throws TSQLException;

    void execute(TConnection conn) throws TSQLException;

    void acceptChanges() throws SyncProviderException;

    void acceptChanges(TConnection con) throws SyncProviderException;

    void restoreOriginal() throws TSQLException;

    void release() throws TSQLException;

    void undoDelete() throws TSQLException;

    void undoInsert() throws TSQLException;

    void undoUpdate() throws TSQLException;

    boolean columnUpdated(int idx) throws TSQLException;

    boolean columnUpdated(String columnName) throws TSQLException;

    Collection<?> toCollection() throws TSQLException;

    Collection<?> toCollection(int column) throws TSQLException;

    Collection<?> toCollection(String column) throws TSQLException;

    SyncProvider getSyncProvider() throws TSQLException;

    void setSyncProvider(String provider) throws TSQLException;

    int size();

    void setMetaData(RowSetMetaData md) throws TSQLException;

    TResultSet getOriginal() throws TSQLException;

    TResultSet getOriginalRow() throws TSQLException;

    void setOriginalRow() throws TSQLException;

    String getTableName() throws TSQLException;

    void setTableName(String tabName) throws TSQLException;

    int[] getKeyColumns() throws TSQLException;

    void setKeyColumns(int[] keys) throws TSQLException;

    RowSet createShared() throws TSQLException;

    CachedRowSet createCopy() throws TSQLException;

    CachedRowSet createCopySchema() throws TSQLException;

    CachedRowSet createCopyNoConstraints() throws TSQLException;

    RowSetWarning getRowSetWarnings() throws TSQLException;

    boolean getShowDeleted() throws TSQLException;

    void setShowDeleted(boolean b) throws TSQLException;

    void commit() throws TSQLException;

    void rollback() throws TSQLException;

    void rollback(TSavepoint s) throws TSQLException;

    void rowSetPopulated(RowSetEvent event, int numRows) throws TSQLException;

    void populate(TResultSet rs, int startRow) throws TSQLException;

    void setPageSize(int size) throws TSQLException;

    int getPageSize();

    boolean nextPage() throws TSQLException;

    boolean previousPage() throws TSQLException;
}
