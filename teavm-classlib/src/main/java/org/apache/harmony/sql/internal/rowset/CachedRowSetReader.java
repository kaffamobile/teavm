/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.sql.internal.rowset;

import java.util.ArrayList;

import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialDatalink;
import javax.sql.rowset.serial.SerialRef;

import org.teavm.classlib.java.net.TURL;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TResultSet;
import org.teavm.classlib.java.sql.TResultSetMetaData;
import org.teavm.classlib.java.sql.TSQLException;

public class CachedRowSetReader implements RowSetReader {

    private TResultSet rs;

    private TResultSetMetaData metadata;
    
    public void setResultSet(TResultSet rs) throws TSQLException {
        this.rs = rs;
        this.metadata = rs.getMetaData();
    }

    public void readData(RowSetInternal theCaller) throws TSQLException {
        CachedRowSetImpl cachedRowSet = (CachedRowSetImpl) theCaller;
        int pageSize = cachedRowSet.getPageSize();
        int maxRows = cachedRowSet.getMaxRows();

        ArrayList<CachedRow> data = new ArrayList<CachedRow>();
        int columnCount = metadata.getColumnCount();

        while (rs.next()) {
            Object[] columnData = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                Object obj = rs.getObject(i + 1);
                if (obj == null) {
                    columnData[i] = null;
                    continue;
                }
                if (obj instanceof TArray) {
                    obj = new SerialArray((TArray) obj);
                } else if (obj instanceof TBlob) {
                    obj = new SerialBlob((TBlob) obj);
                } else if (obj instanceof TClob) {
                    obj = new SerialClob((TClob) obj);
                } else if (obj instanceof TRef) {
                    obj = new SerialRef((TRef) obj);
                } else if (obj instanceof TURL) {
                    obj = new SerialDatalink((TURL) obj);
                }
                columnData[i] = obj;
            }

            CachedRow currentRow = new CachedRow(columnData);
            currentRow.setSqlWarning(rs.getWarnings());
            data.add(currentRow);

            if (maxRows > 0 && maxRows == data.size()) {
                break;
            }

            if (pageSize > 0 && data.size() == pageSize) {
                break;
            }

        }

        cachedRowSet.setRows(data, columnCount);
    }
}
