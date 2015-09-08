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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.BaseRowSet;
import javax.sql.rowset.spi.SyncResolver;

import org.apache.harmony.luni.util.NotImplementedException;
import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TDate;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TResultSetMetaData;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSQLWarning;
import org.teavm.classlib.java.sql.TStatement;
import org.teavm.classlib.java.sql.TTime;
import org.teavm.classlib.java.sql.TTimestamp;

/**
 * TODO seems RI's implementation is not complete, now we follow RI throw
 * <code>UnsupportedOperationException</code>. To complete implementation of
 * this class may need extends
 * org.apache.harmony.sql.internal.rowset.CachedRowSetImpl class
 * 
 */
public class SyncResolverImpl extends BaseRowSet implements SyncResolver {

    private static final long serialVersionUID = 4964648528867743289L;

    private List<ConflictedRow> conflictRows;

    private int currentIndex;

    private TResultSetMetaData metadata;

    private static class ConflictedRow {
        CachedRow row;

        int index;

        int status;

        public ConflictedRow(CachedRow row, int index, int status) {
            this.row = row;
            this.index = index;
            this.status = status;
        }
    }

    public SyncResolverImpl(TResultSetMetaData metadata) {
        super();
        this.metadata = metadata;
        conflictRows = new ArrayList<ConflictedRow>();
        currentIndex = -1;
    }

    public void addConflictRow(CachedRow row, int rowIndex, int status) {
        conflictRows.add(new ConflictedRow(row, rowIndex, status));
    }

    public Object getConflictValue(int index) throws TSQLException {
        if (index <= 0 || index > metadata.getColumnCount()) {
            // sql.27=Invalid column index :{0}
            throw new TSQLException(Messages.getString("sql.27", Integer //$NON-NLS-1$
                    .valueOf(index)));
        }

        if (currentIndex < 0 || currentIndex >= conflictRows.size()) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }

        return conflictRows.get(currentIndex).row.getObject(index);
    }

    public Object getConflictValue(String columnName) throws TSQLException {
        return getConflictValue(getIndexByName(columnName));
    }

    public int getStatus() {
        if (currentIndex < 0 || currentIndex >= conflictRows.size()) {
            /*
             * invalid cursor, can't throw SQLException, we throw
             * NullPointerException instead
             */
            // rowset.7=Not a valid cursor
            throw new NullPointerException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }

        return conflictRows.get(currentIndex).status;
    }

    /**
     * TODO close input stream and clear warning chain as spec say
     */
    public boolean nextConflict() throws TSQLException {
        if (currentIndex == conflictRows.size()) {
            return false;
        }

        currentIndex++;
        return currentIndex >= 0 && currentIndex < conflictRows.size();
    }

    public boolean previousConflict() throws TSQLException {
        if (currentIndex == -1) {
            return false;
        }

        currentIndex--;
        return currentIndex >= 0 && currentIndex < conflictRows.size();
    }

    public void setResolvedValue(int index, Object obj) throws TSQLException,NotImplementedException {
        // TODO not yet implemented
        throw new NotImplementedException();

    }

    public void setResolvedValue(String columnName, Object obj)
            throws TSQLException {
        setResolvedValue(getIndexByName(columnName), obj);
    }

    public int getRow() throws TSQLException {
        if (currentIndex < 0 || currentIndex >= conflictRows.size()) {
            return 0;
        }
        return conflictRows.get(currentIndex).index;
    }

    private int getIndexByName(String columnName) throws TSQLException {
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metadata.getColumnName(i))) {
                return i;
            }
        }
        // rowset.1=Not a valid column name
        throw new TSQLException(Messages.getString("rowset.1")); //$NON-NLS-1$
    }

    public void execute() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean absolute(int row) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void afterLast() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void beforeFirst() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void cancelRowUpdates() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void clearWarnings() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void close() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void deleteRow() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public int findColumn(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean first() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TArray getArray(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TArray getArray(String colName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getAsciiStream(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getAsciiStream(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public BigDecimal getBigDecimal(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public BigDecimal getBigDecimal(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public BigDecimal getBigDecimal(String columnName, int scale)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getBinaryStream(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getBinaryStream(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TBlob getBlob(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TBlob getBlob(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public byte getByte(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public byte getByte(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public byte[] getBytes(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public byte[] getBytes(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public Reader getCharacterStream(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public Reader getCharacterStream(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TClob getClob(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TClob getClob(String colName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public String getCursorName() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TDate getDate(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TDate getDate(int columnIndex, Calendar cal) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TDate getDate(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TDate getDate(String columnName, Calendar cal) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public double getDouble(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public double getDouble(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public float getFloat(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public float getFloat(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public int getInt(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public int getInt(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public long getLong(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public long getLong(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TResultSetMetaData getMetaData() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(String columnName, Map<String, Class<?>> map)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TRef getRef(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TRef getRef(String colName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public short getShort(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public short getShort(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TStatement getStatement() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTime getTime(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTime getTime(int columnIndex, Calendar cal) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTime getTime(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTime getTime(String columnName, Calendar cal) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTimestamp getTimestamp(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTimestamp getTimestamp(int columnIndex, Calendar cal)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTimestamp getTimestamp(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TTimestamp getTimestamp(String columnName, Calendar cal)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public org.teavm.classlib.java.net.TURL getURL(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public org.teavm.classlib.java.net.TURL getURL(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getUnicodeStream(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getUnicodeStream(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public TSQLWarning getWarnings() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void insertRow() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isAfterLast() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isBeforeFirst() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isFirst() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isLast() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean last() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void moveToCurrentRow() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void moveToInsertRow() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean next() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean previous() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void refreshRow() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean relative(int rows) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowDeleted() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowInserted() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowUpdated() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateArray(int columnIndex, TArray x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateArray(String columnName, TArray x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBigDecimal(String columnName, BigDecimal x)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(int columnIndex, TBlob x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(String columnName, TBlob x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBoolean(int columnIndex, boolean x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBoolean(String columnName, boolean x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateByte(int columnIndex, byte x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateByte(String columnName, byte x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBytes(int columnIndex, byte[] x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBytes(String columnName, byte[] x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(int columnIndex, TClob x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(String columnName, TClob x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDate(int columnIndex, TDate x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDate(String columnName, TDate x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDouble(int columnIndex, double x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDouble(String columnName, double x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateFloat(int columnIndex, float x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateFloat(String columnName, float x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateInt(int columnIndex, int x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateInt(String columnName, int x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateLong(int columnIndex, long x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateLong(String columnName, long x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNull(int columnIndex) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNull(String columnName) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateObject(int columnIndex, Object x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateObject(int columnIndex, Object x, int scale)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateObject(String columnName, Object x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateObject(String columnName, Object x, int scale)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRef(int columnIndex, TRef x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRef(String columnName, TRef x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRow() throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateShort(int columnIndex, short x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateShort(String columnName, short x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateString(int columnIndex, String x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateString(String columnName, String x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTime(int columnIndex, TTime x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTime(String columnName, TTime x) throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTimestamp(int columnIndex, TTimestamp x)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTimestamp(String columnName, TTimestamp x)
            throws TSQLException {
        throw new UnsupportedOperationException();
    }

    public boolean wasNull() throws TSQLException {
        throw new UnsupportedOperationException();
    }

}
