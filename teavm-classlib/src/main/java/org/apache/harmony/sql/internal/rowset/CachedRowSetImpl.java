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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetInternal;
import javax.sql.RowSetListener;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.BaseRowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialRef;
import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncFactoryException;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TConnection;
import org.teavm.classlib.java.sql.TDate;
import org.teavm.classlib.java.sql.TDriverManager;
import org.teavm.classlib.java.sql.TPreparedStatement;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TResultSet;
import org.teavm.classlib.java.sql.TResultSetMetaData;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSQLWarning;
import org.teavm.classlib.java.sql.TSavepoint;
import org.teavm.classlib.java.sql.TStatement;
import org.teavm.classlib.java.sql.TStruct;
import org.teavm.classlib.java.sql.TTime;
import org.teavm.classlib.java.sql.TTimestamp;
import org.teavm.classlib.java.sql.TTypes;

public class CachedRowSetImpl extends BaseRowSet implements CachedRowSet,
        RowSetInternal {

    private static final long serialVersionUID = 1L;

    protected ArrayList<CachedRow> rows;

    protected RowSetMetaData meta;

    protected CachedRow currentRow;

    /**
     * current row index include deleted rows, start from 1
     */
    protected int currentRowIndex;

    // the number of the rows in one "page"
    private int pageSize;

    /**
     * used for paging, record row index for next page in ResultSet, start from
     * 1
     */
    private int nextPageRowIndex = -1;

    /**
     * used for paging, record row index for previous page in ResultSet, start
     * from 1
     */
    private int previousPageRowIndex = -1;

    /**
     * cached ResultSet for paging in memory
     */
    private CachedRowSet cachedResultSet = null;

    private String tableName;

    private int rememberedCursorPosition;

    private CachedRow insertRow;

    private boolean isCursorOnInsert;

    private int[] keyCols;

    protected int columnCount;

    private int deletedRowCount;

    private SyncProvider syncProvider;

    protected CachedRowSetImpl originalResultSet;

    private TSQLWarning sqlwarn = new TSQLWarning();

    // TODO deal with rowSetWarning
    private RowSetWarning rowSetWarning = new RowSetWarning();

    protected Class[] columnTypes;

    private String[] matchColumnNames;

    private int[] matchColumnIndexes;

    private String cursorName;

    private boolean isLastColNull;

    protected transient TConnection conn;

    private boolean isNotifyListener = true;

    protected static final Map<Integer, Class<?>> TYPE_MAPPING = initialTypeMapping();

    public static final String PROVIDER_ID = "Apache Harmony HYOptimisticProvider"; //$NON-NLS-1$

    public CachedRowSetImpl(String providerID) throws SyncFactoryException {
        syncProvider = SyncFactory.getInstance(providerID);
        initialProperties();
    }

    private static Map<Integer, Class<?>> initialTypeMapping() {
        HashMap<Integer, Class<?>> map = new HashMap<Integer, Class<?>>();
        map.put(Integer.valueOf(TTypes.ARRAY), TArray.class);
        map.put(Integer.valueOf(TTypes.BIGINT), Long.class);
        map.put(Integer.valueOf(TTypes.BINARY), byte[].class);
        map.put(Integer.valueOf(TTypes.BIT), Boolean.class);
        map.put(Integer.valueOf(TTypes.BLOB), TBlob.class);
        map.put(Integer.valueOf(TTypes.BOOLEAN), Boolean.class);
        map.put(Integer.valueOf(TTypes.CHAR), String.class);
        map.put(Integer.valueOf(TTypes.CLOB), TClob.class);
        map.put(Integer.valueOf(TTypes.DATE), TDate.class);
        map.put(Integer.valueOf(TTypes.DECIMAL), BigDecimal.class);
        map.put(Integer.valueOf(TTypes.DOUBLE), Double.class);
        map.put(Integer.valueOf(TTypes.FLOAT), Double.class);
        map.put(Integer.valueOf(TTypes.INTEGER), Integer.class);
        map.put(Integer.valueOf(TTypes.LONGVARBINARY), byte[].class);
        map.put(Integer.valueOf(TTypes.LONGVARCHAR), String.class);
        map.put(Integer.valueOf(TTypes.NUMERIC), BigDecimal.class);
        map.put(Integer.valueOf(TTypes.REAL), Float.class);
        map.put(Integer.valueOf(TTypes.REF), TRef.class);
        map.put(Integer.valueOf(TTypes.SMALLINT), Short.class);
        map.put(Integer.valueOf(TTypes.STRUCT), TStruct.class);
        map.put(Integer.valueOf(TTypes.TIME), TTime.class);
        map.put(Integer.valueOf(TTypes.TIMESTAMP), TTimestamp.class);
        map.put(Integer.valueOf(TTypes.TINYINT), Byte.class);
        map.put(Integer.valueOf(TTypes.VARBINARY), byte[].class);
        map.put(Integer.valueOf(TTypes.VARCHAR), String.class);

        return map;
    }

    private void initialProperties() {
        try {
            setEscapeProcessing(true);
            setTransactionIsolation(TConnection.TRANSACTION_READ_COMMITTED);
            setConcurrency(TResultSet.CONCUR_UPDATABLE);
            setType(TResultSet.TYPE_SCROLL_INSENSITIVE);
            setMaxRows(0);
            setQueryTimeout(0);
            setShowDeleted(false);
            setUsername(null);
            setPassword(null);
            setMaxFieldSize(0);
            setTypeMap(null);
            setFetchSize(0);
        } catch (TSQLException e) {
            // ignore, never reached
        }

    }

    public CachedRowSetImpl() throws SyncFactoryException {
        this(PROVIDER_ID);
    }

    public void setRows(ArrayList<CachedRow> data, int cloumnCount) {
        rows = data;
        this.columnCount = cloumnCount;
    }

    public void acceptChanges() throws SyncProviderException {
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new SyncProviderException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }

        TConnection currentConn = null;
        TConnection preConn = conn;
        try {
            currentConn = retrieveConnection();
            currentConn.setTypeMap(getTypeMap());
            acceptChanges(currentConn);
            currentConn.commit();
        } catch (TSQLException e) {
            try {
                if (currentConn != null) {
                    currentConn.rollback();
                }
            } catch (TSQLException ex) {
                // ignore
            }
            SyncProviderException ex = new SyncProviderException();
            ex.initCause(e);
            throw ex;
        } finally {
            conn = preConn;
            if (currentConn != null) {
                try {
                    currentConn.close();
                } catch (TSQLException ex) {
                    SyncProviderException spe = new SyncProviderException();
                    spe.initCause(ex);
                    throw spe;
                }
            }
        }
    }

    public void acceptChanges(TConnection connection)
            throws SyncProviderException {
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new SyncProviderException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }

        // Follow RI to assign conn before checking whether conn is null
        conn = connection;

        if (conn == null) {
            throw new SyncProviderException();
        }

        boolean isShowDeleted = false;
        try {
            isShowDeleted = getShowDeleted();
        } catch (TSQLException e) {
            // ignore
        }

        try {
            conn.setAutoCommit(false);
            CachedRowSetWriter rowSetWriter = (CachedRowSetWriter) syncProvider
                    .getRowSetWriter();
            rowSetWriter.setConnection(conn);
            int beforeWriteIndex = currentRowIndex;
            // writer use next navigate rowset, so must make all rows visible
            setShowDeleted(true);

            if (!rowSetWriter.writeData(this)) {
                throw rowSetWriter.getSyncException();
            }

            // must reset curosr before reset showDeleted
            absolute(beforeWriteIndex);
            setShowDeleted(isShowDeleted);

            // record to the next visible row index
            int index = getRow();
            if (index == 0) {
                next();
                index = getRow();
                if (index == 0) {
                    index = rows.size() + 1;
                }
            }

            boolean isChanged = false;
            for (int i = rows.size() - 1; i >= 0; i--) {
                currentRow = rows.get(i);
                if (rowDeleted()) {
                    isChanged = true;
                    setOriginalRow();
                } else if (rowInserted() || rowUpdated()) {
                    isChanged = true;
                    setOriginalRow();
                }
            }
            // Set originalResultSet
            if (isChanged) {
                try {
                    ArrayList<CachedRow> nowRows = new ArrayList<CachedRow>();
                    for (int i = 0; i < rows.size(); i++) {
                        nowRows.add(rows.get(i).createClone());
                        nowRows.get(i).restoreOriginal();
                    }
                    originalResultSet.setRows(nowRows, columnCount);
                } catch (CloneNotSupportedException cloneE) {
                    throw new SyncProviderException(cloneE.getMessage());
                }
            }

            deletedRowCount = 0;

            // move cursor
            if (index > rows.size()) {
                afterLast();
            } else if (index <= 0) {
                beforeFirst();
            } else {
                absolute(index);
            }

            if (isNotifyListener) {
                notifyRowSetChanged();
            }

        } catch (SyncProviderException e) {
            throw e;
        } catch (TSQLException e) {
            SyncProviderException ex = new SyncProviderException();
            ex.initCause(e);
            throw ex;
        } finally {
            try {
                setShowDeleted(isShowDeleted);
            } catch (TSQLException e) {
                // ignore
            }
        }
    }

    public boolean columnUpdated(int idx) throws TSQLException {
        if (currentRow == null || idx > meta.getColumnCount() || idx <= 0
                || currentRow == insertRow) {
            // rowset.0 = Not a valid position
            throw new TSQLException(Messages.getString("rowset.0")); //$NON-NLS-1$
        }
        return currentRow.getUpdateMask(idx - 1);
    }

    public boolean columnUpdated(String columnName) throws TSQLException {
        return columnUpdated(getIndexByName(columnName));
    }

    private int getIndexByName(String columnName) throws TSQLException {
        if (meta == null || columnName == null) {
            throw new NullPointerException();
        }

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(meta.getColumnName(i))) {
                return i;
            }
        }
        // rowset.1=Not a valid column name
        throw new TSQLException(Messages.getString("rowset.1")); //$NON-NLS-1$
    }

    public void commit() throws TSQLException {
        if (conn == null) {
            throw new NullPointerException();
        }
        conn.commit();
    }

    public CachedRowSet createCopy() throws TSQLException {
        CachedRowSetImpl output;
        try {
            /*
             * the attribute of BaseRowSet which are needed to deep copy
             */
            // BaseRowSet.params <Hashtable>
            Object[] paramsObjArray = super.getParams();
            Hashtable<Object, Object> paramsHashtable = new Hashtable<Object, Object>();
            for (int i = 0; i < paramsObjArray.length; i++) {
                paramsHashtable.put(Integer.valueOf(i), paramsObjArray[i]);
            }
            // BaseRowSet.listeners <Vector>
            Vector<RowSetListener> listeners = new Vector<RowSetListener>();

            /*
             * deep copy BaseRowSet
             */
            output = (CachedRowSetImpl) super.clone();
            // BaseRowSet.params
            Field paramsField = BaseRowSet.class.getDeclaredField("params"); //$NON-NLS-1$
            paramsField.setAccessible(true);
            paramsField.set(output, paramsHashtable);
            // BaseRowSet.listeners
            Field listenersField = BaseRowSet.class
                    .getDeclaredField("listeners"); //$NON-NLS-1$
            listenersField.setAccessible(true);
            listenersField.set(output, listeners);
            // BaseRowSet.map
            if (super.getTypeMap() != null) {
                Map<String, Class<?>> originalTypeMap = super.getTypeMap();
                Map<String, Class<?>> copyTypeMap = new HashMap<String, Class<?>>();
                copyTypeMap.putAll(originalTypeMap);
                output.setTypeMap(copyTypeMap);
            }

            /*
             * deep copy CachedRowSetImpl
             */
            // CachedRowSetImpl.rows <ArrayList>
            ArrayList<CachedRow> copyRows = new ArrayList<CachedRow>();
            for (int i = 0; i < rows.size(); i++) {
                copyRows.add(rows.get(i).createClone());
            }
            output.setRows(copyRows, columnCount);
            // CachedRowSetImpl.meta <RowSetMetaData>
            output.setMetaData(copyMetaData(getMetaData()));
            // set currentRow
            if ((currentRowIndex > 0) && (currentRowIndex <= rows.size())) {
                output.absolute(currentRowIndex);
            }
            // others
            if (getKeyColumns() != null) {
                output.setKeyColumns(getKeyColumns().clone());
            }
            // CachedRowSetImpl.originalResultSet
            CachedRowSetImpl copyOriginalRs = new CachedRowSetImpl();
            copyOriginalRs.populate(getOriginal());
            getOriginal().beforeFirst();
            output.originalResultSet = copyOriginalRs;

            if (matchColumnIndexes != null) {
                output.matchColumnIndexes = matchColumnIndexes.clone();
            }

            if (matchColumnNames != null) {
                output.matchColumnNames = matchColumnNames.clone();
            }

            output.setSyncProvider(getSyncProvider().getProviderID());
            if (insertRow != null) {
                output.insertRow = insertRow.createClone();
            }

            return output;
        } catch (CloneNotSupportedException e) {
            throw new TSQLException(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new TSQLException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new TSQLException(e.getMessage());
        }
    }

    public CachedRowSet createCopyNoConstraints() throws TSQLException {
        CachedRowSetImpl output = (CachedRowSetImpl) createCopy();
        output.initialProperties();
        return output;
    }

    public CachedRowSet createCopySchema() throws TSQLException {
        CachedRowSetImpl output = (CachedRowSetImpl) createCopy();

        // clean up rows data
        output.currentRow = null;
        output.currentRowIndex = 0;
        output.insertRow = null;
        output.isCursorOnInsert = false;
        output.isLastColNull = false;
        output.nextPageRowIndex = -1;
        output.rememberedCursorPosition = 0;
        output.rows = new ArrayList<CachedRow>();
        output.sqlwarn = null;
        output.deletedRowCount = 0;

        return output;
    }

    public RowSet createShared() throws TSQLException {
        // shallow copy
        RowSet result = null;
        try {
            result = (RowSet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new TSQLException(e.getMessage());
        }

        return result;
    }

    public void execute(TConnection connection) throws TSQLException {
        String localCommand = getCommand();
        if (localCommand == null || getParams() == null) {
            // rowset.16=Not a valid command
            throw new TSQLException(Messages.getString("rowset.16")); //$NON-NLS-1$
        }

        conn = connection;
        TPreparedStatement ps = connection.prepareStatement(localCommand);
        setParameter(ps);

        if (ps.execute()) {
            doPopulate(ps.getResultSet(), true);

            if (getPageSize() != 0) {
                nextPageRowIndex = rows.size() + 1;
                previousPageRowIndex = 0;
                cachedResultSet = null;
            } else {
                previousPageRowIndex = -1;
                nextPageRowIndex = -1;
                cachedResultSet = null;
            }
        }

    }

    private void setParameter(TPreparedStatement ps) throws TSQLException {
        Object[] params = getParams();
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Object[]) {
                Object[] objs = (Object[]) params[i];
                // character stream
                if (objs.length == 2) {
                    ps.setCharacterStream(i + 1, (Reader) objs[0],
                            ((Integer) objs[1]).intValue());
                } else {
                    int type = ((Integer) objs[2]).intValue();
                    switch (type) {
                    case BaseRowSet.ASCII_STREAM_PARAM:
                        ps.setAsciiStream(i + 1, (InputStream) objs[0],
                                ((Integer) objs[1]).intValue());
                        break;
                    case BaseRowSet.BINARY_STREAM_PARAM:
                        ps.setBinaryStream(i + 1, (InputStream) objs[0],
                                ((Integer) objs[1]).intValue());
                        break;
                    case BaseRowSet.UNICODE_STREAM_PARAM:
                        ps.setUnicodeStream(i + 1, (InputStream) objs[0],
                                ((Integer) objs[1]).intValue());
                        break;
                    }
                }
            } else {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    public int[] getKeyColumns() throws TSQLException {
        if (rows == null) {
            // rowset.26=The object has not been initialized
            throw new TSQLException(Messages.getString("rowset.26")); //$NON-NLS-1$
        }
        if (keyCols == null) {
            return new int[0];
        }

        return keyCols;
    }

    public TResultSet getOriginal() throws TSQLException {
        if (originalResultSet == null) {
            throw new NullPointerException();
        }
        return originalResultSet;
    }

    public TResultSet getOriginalRow() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }

        CachedRowSetImpl originalRowRset = new CachedRowSetImpl();
        ArrayList<CachedRow> data = new ArrayList<CachedRow>();
        CachedRow originalCachedRow = currentRow.getOriginal();
        data.add(originalCachedRow);
        originalRowRset.setMetaData(meta);
        originalRowRset.setRows(data, columnCount);
        originalRowRset.setType(TResultSet.TYPE_SCROLL_INSENSITIVE);
        originalRowRset.setConcurrency(TResultSet.CONCUR_UPDATABLE);
        return originalRowRset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public RowSetWarning getRowSetWarnings() throws TSQLException {
        return rowSetWarning;
    }

    public SyncProvider getSyncProvider() throws TSQLException {
        return syncProvider;
    }

    public String getTableName() throws TSQLException {
        return tableName;
    }

    /**
     * TODO refactor paging
     */
    public boolean nextPage() throws TSQLException {
        if (rows == null || nextPageRowIndex == -1 || getPageSize() == 0) {
            // rowset.19=Populate data before calling
            throw new TSQLException(Messages.getString("rowset.19")); //$NON-NLS-1$
        }

        if (cachedResultSet == null) {
            String localCommand = getCommand();
            if (localCommand == null || getParams() == null) {
                // rowset.16=Not a valid command
                throw new TSQLException(Messages.getString("rowset.16")); //$NON-NLS-1$
            }

            TPreparedStatement ps = retrieveConnection().prepareStatement(
                    localCommand);

            setParameter(ps);

            if (ps.execute()) {
                TResultSet rs = ps.getResultSet();
                int index = 1;
                while (rs.next() && index < nextPageRowIndex - 1) {
                    index++;
                }

                if (isNotifyListener) {
                    isNotifyListener = false;
                    doPopulate(rs, true);
                    notifyRowSetChanged();
                    isNotifyListener = true;
                } else {
                    doPopulate(rs, true);
                }

                if (rows.size() == 0) {
                    return false;
                }
                previousPageRowIndex = nextPageRowIndex - 1;
                nextPageRowIndex += rows.size();
                return true;

            }
            return false;

        }

        if (cachedResultSet.absolute(nextPageRowIndex)) {
            cachedResultSet.previous();

            if (isNotifyListener) {
                isNotifyListener = false;
                doPopulate(cachedResultSet, true);
                notifyRowSetChanged();
                isNotifyListener = true;
            } else {
                doPopulate(cachedResultSet, true);
            }

            if (rows.size() == 0) {
                return false;
            }
            previousPageRowIndex = nextPageRowIndex - 1;
            nextPageRowIndex += rows.size();
            return true;
        }
        return false;

    }

    public void populate(TResultSet rs) throws TSQLException {
        doPopulate(rs, false);
        previousPageRowIndex = -1;
        nextPageRowIndex = -1;
        cachedResultSet = null;
    }

    public void populate(TResultSet rs, int startRow) throws TSQLException {
        if (rs == null) {
            // sql.42=Illegal Argument
            throw new TSQLException(Messages.getString("sql.42")); //$NON-NLS-1$
        }

        if (startRow == 1) {
            rs.beforeFirst();
            // maybe use next to move is better
        } else if (startRow <= 0 || !rs.absolute(startRow - 1)) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }

        // paging in memory
        if (getPageSize() != 0) {
            cachedResultSet = new CachedRowSetImpl();
            cachedResultSet.setMaxRows(getMaxRows());
            cachedResultSet.populate(rs, startRow);
            doPopulate(cachedResultSet, true);

            nextPageRowIndex = rows.size() + 1;
            previousPageRowIndex = 0;

        } else {
            doPopulate(rs, true);
            previousPageRowIndex = -1;
            nextPageRowIndex = -1;
            cachedResultSet = null;
        }
    }

    protected void doPopulate(TResultSet rs, boolean isPaging)
            throws TSQLException {
        boolean oldIsNotifyListener = isNotifyListener;
        isNotifyListener = false;
        meta = copyMetaData(rs.getMetaData());

        columnCount = meta.getColumnCount();
        // initial columnTypes
        columnTypes = new Class[columnCount];
        for (int i = 1; i <= columnTypes.length; ++i) {
            columnTypes[i - 1] = TYPE_MAPPING.get(Integer.valueOf(meta
                    .getColumnType(i)));
        }
        try {
            cursorName = rs.getCursorName();
        } catch (TSQLException e) {
            cursorName = null;
            // ignore
        }

        if (rs.getStatement() != null
                && rs.getStatement().getConnection() != null) {
            setTypeMap(rs.getStatement().getConnection().getTypeMap());
        }

        /*
         * this method not support paging, so before readData set pageSize and
         * maxRowsto 0 and restore previous values after readData
         */
        CachedRowSetReader crsReader = (CachedRowSetReader) syncProvider
                .getRowSetReader();
        crsReader.setResultSet(rs);
        if (!isPaging) {
            int prePageSize = getPageSize();
            setPageSize(0);
            int preMaxRows = getMaxRows();
            setMaxRows(0);
            crsReader.readData(this);
            setPageSize(prePageSize);
            setMaxRows(preMaxRows);
        } else {
            crsReader.readData(this);
        }

        setTableName(rs.getMetaData().getTableName(1));

        originalResultSet = new CachedRowSetImpl();
        crsReader.setResultSet(this);
        crsReader.readData(originalResultSet);
        originalResultSet.setMetaData((RowSetMetaData) (getMetaData()));

        // recovery the states
        beforeFirst();

        isNotifyListener = true;
        if (oldIsNotifyListener) {
            notifyRowSetChanged();
        }
    }

    // deep copy of ResultSetMetaData
    protected RowSetMetaData copyMetaData(TResultSetMetaData metaData)
            throws TSQLException {
        RowSetMetaDataImpl rowSetMetaData = new RowSetMetaDataImpl();
        rowSetMetaData.setColumnCount(metaData.getColumnCount());
        for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
            rowSetMetaData.setAutoIncrement(columnIndex, metaData
                    .isAutoIncrement(columnIndex));
            doCopyMetaData(rowSetMetaData, columnIndex, metaData, columnIndex);
        }
        return rowSetMetaData;
    }

    protected void doCopyMetaData(RowSetMetaData targetRsmd, int targetIndex,
            TResultSetMetaData srcRsmd, int srcIndex) throws TSQLException {
        targetRsmd.setAutoIncrement(targetIndex, srcRsmd
                .isAutoIncrement(srcIndex));
        targetRsmd.setCaseSensitive(targetIndex, srcRsmd
                .isCaseSensitive(srcIndex));
        targetRsmd
                .setCatalogName(targetIndex, srcRsmd.getCatalogName(srcIndex));
        targetRsmd.setColumnDisplaySize(targetIndex, srcRsmd
                .getColumnDisplaySize(srcIndex));
        targetRsmd
                .setColumnLabel(targetIndex, srcRsmd.getColumnLabel(srcIndex));
        targetRsmd.setColumnName(targetIndex, srcRsmd.getColumnName(srcIndex));
        targetRsmd.setColumnType(targetIndex, srcRsmd.getColumnType(srcIndex));
        targetRsmd.setColumnTypeName(targetIndex, srcRsmd
                .getColumnTypeName(srcIndex));
        targetRsmd.setCurrency(targetIndex, srcRsmd.isCurrency(srcIndex));
        targetRsmd.setNullable(targetIndex, srcRsmd.isNullable(srcIndex));
        targetRsmd.setPrecision(targetIndex, srcRsmd.getPrecision(srcIndex));
        targetRsmd.setScale(targetIndex, srcRsmd.getScale(srcIndex));
        targetRsmd.setSchemaName(targetIndex, srcRsmd.getSchemaName(srcIndex));
        targetRsmd.setSearchable(targetIndex, srcRsmd.isSearchable(srcIndex));
        targetRsmd.setSigned(targetIndex, srcRsmd.isSigned(srcIndex));
        targetRsmd.setTableName(targetIndex, srcRsmd.getTableName(srcIndex));
    }

    public boolean previousPage() throws TSQLException {
        if (rows == null || previousPageRowIndex == -1 || getPageSize() == 0) {
            // rowset.19=Populate data before calling
            throw new TSQLException(Messages.getString("rowset.19")); //$NON-NLS-1$
        }

        if (previousPageRowIndex == 0) {
            return false;
        }

        if (cachedResultSet == null) {
            String localCommand = getCommand();
            if (localCommand == null || getParams() == null) {
                // rowset.16=Not a valid command
                throw new TSQLException(Messages.getString("rowset.16")); //$NON-NLS-1$
            }

            TPreparedStatement ps = retrieveConnection().prepareStatement(
                    localCommand);

            setParameter(ps);

            if (ps.execute()) {
                TResultSet rs = ps.getResultSet();
                int startIndex = previousPageRowIndex - getPageSize() + 1;

                if (startIndex <= 0) {
                    startIndex = 1;
                }

                int index = 0;
                while (index < startIndex - 1) {
                    if (!rs.next()) {
                        break;
                    }
                    index++;
                }

                int prePageSize = getPageSize();
                if (prePageSize != 0
                        && previousPageRowIndex - startIndex + 1 != prePageSize) {
                    setPageSize(previousPageRowIndex - startIndex + 1);
                }
                if (isNotifyListener) {
                    isNotifyListener = false;
                    doPopulate(rs, true);
                    notifyRowSetChanged();
                    isNotifyListener = true;
                } else {
                    doPopulate(rs, true);
                }

                setPageSize(prePageSize);

                if (rows.size() == 0) {
                    return false;
                }
                nextPageRowIndex = previousPageRowIndex + 1;
                previousPageRowIndex = startIndex - 1;
                return true;
            }

            return false;
        }

        int startIndex = previousPageRowIndex - getPageSize() + 1;

        if (startIndex <= 0) {
            startIndex = 1;
        }

        if (!cachedResultSet.absolute(startIndex)) {
            return false;
        }

        cachedResultSet.previous();

        int prePageSize = getPageSize();
        if (prePageSize != 0
                && previousPageRowIndex - startIndex + 1 != prePageSize) {
            setPageSize(previousPageRowIndex - startIndex + 1);
        }

        doPopulate(cachedResultSet, true);

        setPageSize(prePageSize);

        if (rows.size() == 0) {
            return false;
        }
        nextPageRowIndex = previousPageRowIndex + 1;
        previousPageRowIndex = startIndex - 1;
        return true;
    }

    public void release() throws TSQLException {
        rows = new ArrayList<CachedRow>();
        if (isNotifyListener) {
            notifyRowSetChanged();
        }
    }

    public void restoreOriginal() throws TSQLException {
        if (rows == null) {
            return;
        }

        boolean oldIsNotifyListener = isNotifyListener;
        isNotifyListener = false;
        List<CachedRow> insertedRows = new ArrayList<CachedRow>();
        for (CachedRow row : rows) {
            if (row.isInsert()) {
                insertedRows.add(row);
            } else if (row.isDelete() || row.isUpdate()) {
                row.restoreOriginal();
            }
        }
        rows.removeAll(insertedRows);
        insertRow = null;
        isCursorOnInsert = false;
        deletedRowCount = 0;

        first();
        isNotifyListener = true;
        if (oldIsNotifyListener) {
            notifyRowSetChanged();
        }
    }

    public void rollback() throws TSQLException {
        if (conn == null) {
            throw new NullPointerException();
        }
        conn.rollback();
    }

    public void rollback(TSavepoint s) throws TSQLException {
        if (conn == null) {
            throw new NullPointerException();
        }
        conn.rollback(s);
    }

    public void rowSetPopulated(RowSetEvent event, int numRows)
            throws TSQLException {
        if (numRows <= 0) {
            // sql.42=Illegal Argument
            throw new TSQLException(Messages.getString("sql.42")); //$NON-NLS-1$
        }
        if (numRows < getFetchSize()) {
            // rowset.22=Number of rows is less than fetch size
            throw new TSQLException(Messages.getString("rowset.22")); //$NON-NLS-1$
        }
        if (size() == 0 || size() % numRows == 0) {
            if (isNotifyListener) {
                notifyRowSetChanged();
            }
        }
    }

    public void setKeyColumns(int[] keys) throws TSQLException {
        if (keys == null) {
            throw new NullPointerException();
        }

        if (rows == null) {
            keyCols = keys.clone();
        } else {
            for (int key : keys) {
                if (key <= 0 || key > columnCount) {
                    // sql.27=Invalid column index :{0}
                    throw new TSQLException(Messages.getString("sql.27", key)); //$NON-NLS-1$
                }
            }

            keyCols = keys.clone();
        }
    }

    public void setMetaData(RowSetMetaData md) throws TSQLException {
        meta = md;
    }

    public void setOriginalRow() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }

        if (rowDeleted()) {
            rows.remove(currentRow);
            deletedRowCount--;
        } else if (rowUpdated() || rowInserted()) {
            currentRow.setOriginal();
        }
    }

    public void setPageSize(int size) throws TSQLException {
        if (size < 0) {
            // rowset.2=Negative page size
            throw new TSQLException(Messages.getString("rowset.2")); //$NON-NLS-1$
        }
        if ((getMaxRows() != 0) && (getMaxRows() < size)) {
            // rowset.9=PageSize can not larger than MaxRows
            throw new TSQLException(Messages.getString("rowset.9")); //$NON-NLS-1$
        }
        pageSize = size;
    }

    public void setSyncProvider(String provider) throws TSQLException {
        syncProvider = SyncFactory.getInstance(provider);
    }

    public void setTableName(String tabName) throws TSQLException {
        if (tabName == null) {
            // rowset.3=Table name should not be null
            throw new TSQLException(Messages.getString("rowset.3")); //$NON-NLS-1$
        }
        tableName = tabName;
    }

    public int size() {
        if (rows == null) {
            return 0;
        }
        return rows.size();
    }

    public Collection<?> toCollection() throws TSQLException {
        if (rows == null) {
            // sql.38=Object is invalid
            throw new TSQLException(Messages.getString("sql.38")); //$NON-NLS-1$
        }
        List<Vector<Object>> list = new ArrayList<Vector<Object>>();
        if (rows.size() > 0) {
            Vector<Object> vector = null;
            for (int i = 0; i < rows.size(); i++) {
                CachedRow row = rows.get(i);
                vector = new Vector<Object>();
                for (int j = 1; j <= columnCount; j++) {
                    vector.add(row.getObject(j));
                }
                list.add(vector);
            }
        }
        return list;
    }

    public Collection<?> toCollection(int column) throws TSQLException {
        if (rows == null) {
            return new Vector<Object>();
        }

        if (column <= 0 || column > columnCount) {
            // sql.42=Illegal Argument
            throw new TSQLException(Messages.getString("sql.42")); //$NON-NLS-1$
        }

        Vector<Object> vector = new Vector<Object>();
        if (rows.size() > 0) {
            for (int i = 0; i < rows.size(); i++) {
                vector.add(rows.get(i).getObject(column));
            }
        }
        return vector;
    }

    public Collection<?> toCollection(String column) throws TSQLException {
        return toCollection(getIndexByName(column));
    }

    public void undoDelete() throws TSQLException {
        if (isAfterLast() || isBeforeFirst()) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }

        if (currentRow != null && !currentRow.isDelete()) {
            // rowset.0=Not a valid position
            throw new TSQLException(Messages.getString("rowset.0")); //$NON-NLS-1$
        }

        if (currentRow != null && currentRow.isDelete()) {
            currentRow.undoDelete();
            deletedRowCount--;
        }

        if (isNotifyListener) {
            notifyRowChanged();
        }
    }

    public void undoInsert() throws TSQLException {
        checkValidRow();
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }
        if (!rowInserted()) {
            // rowset.4=Not an insert row
            throw new TSQLException(Messages.getString("rowset.4")); //$NON-NLS-1$
        }
        rows.remove(currentRow);
        next();
        if (isNotifyListener) {
            notifyRowChanged();
        }
    }

    public void undoUpdate() throws TSQLException {
        checkValidRow();
        if (isCursorOnInsert && insertRow == null) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }
        if (currentRow == insertRow) {
            currentRow = new CachedRow(new Object[columnCount]);
        } else if (rowUpdated()) {
            currentRow.restoreOriginal();
        }

        if (isNotifyListener) {
            notifyRowChanged();
        }
    }

    public int[] getMatchColumnIndexes() throws TSQLException {
        if (matchColumnIndexes == null || matchColumnIndexes.length == 0
                || matchColumnIndexes[0] == -1) {
            // rowset.13=Set Match columns before getting them
            throw new TSQLException(Messages.getString("rowset.13")); //$NON-NLS-1$
        }

        return matchColumnIndexes.clone();
    }

    public String[] getMatchColumnNames() throws TSQLException {
        if (matchColumnNames == null || matchColumnNames.length == 0
                || matchColumnNames[0] == null) {
            // rowset.13=Set Match columns before getting them
            throw new TSQLException(Messages.getString("rowset.13")); //$NON-NLS-1$
        }
        return matchColumnNames.clone();
    }

    public void setMatchColumn(int columnIdx) throws TSQLException {
        if (columnIdx < 0) {
            // TODO why is 0 valid?
            // rowset.20=Match columns should be greater than 0
            throw new TSQLException(Messages.getString("rowset.20")); //$NON-NLS-1$
        }

        if (matchColumnIndexes == null) {
            /*
             * FIXME initial match column, the default length of array is 10 in
             * RI, we don't know why, just follow now
             */
            matchColumnIndexes = new int[10];
            Arrays.fill(matchColumnIndexes, -1);
        }

        matchColumnIndexes[0] = columnIdx;
    }

    public void setMatchColumn(int[] columnIdxes) throws TSQLException {
        if (columnIdxes == null) {
            throw new NullPointerException();
        }

        for (int i : columnIdxes) {
            if (i < 0) {
                // TODO why is 0 valid?
                // rowset.20=Match columns should be greater than 0
                throw new TSQLException(Messages.getString("rowset.20")); //$NON-NLS-1$
            }
        }

        if (matchColumnIndexes == null) {
            /*
             * FIXME initial match column, the default length of array is 10 in
             * RI, we don't know why, just follow now
             */
            matchColumnIndexes = new int[10];
            Arrays.fill(matchColumnIndexes, -1);
        }

        int[] newValue = new int[matchColumnIndexes.length + columnIdxes.length];
        System.arraycopy(columnIdxes, 0, newValue, 0, columnIdxes.length);
        System.arraycopy(matchColumnIndexes, 0, newValue, columnIdxes.length,
                matchColumnIndexes.length);

        matchColumnIndexes = newValue;
    }

    public void setMatchColumn(String columnName) throws TSQLException {
        if (columnName == null || columnName.equals("")) { //$NON-NLS-1$
            // rowset.12=Match columns should not be empty or null string
            throw new TSQLException(Messages.getString("rowset.12")); //$NON-NLS-1$
        }

        if (matchColumnNames == null) {
            /*
             * FIXME initial match column, the default length of array is 10 in
             * RI, we don't know why, just follow now
             */
            matchColumnNames = new String[10];
        }

        matchColumnNames[0] = columnName;
    }

    public void setMatchColumn(String[] columnNames) throws TSQLException {
        if (columnNames == null) {
            throw new NullPointerException();
        }
        for (String name : columnNames) {
            if (name == null || name.equals("")) { //$NON-NLS-1$
                // rowset.12=Match columns should not be empty or null string
                throw new TSQLException(Messages.getString("rowset.12")); //$NON-NLS-1$
            }
        }

        if (matchColumnNames == null) {
            /*
             * FIXME initial match column, the default length of array is 10 in
             * RI, we don't know why, just follow now
             */
            matchColumnNames = new String[10];
        }

        String[] newValue = new String[matchColumnNames.length
                + columnNames.length];
        System.arraycopy(columnNames, 0, newValue, 0, columnNames.length);
        System.arraycopy(matchColumnNames, 0, newValue, columnNames.length,
                matchColumnNames.length);

        matchColumnNames = newValue;
    }

    public void unsetMatchColumn(int columnIdx) throws TSQLException {

        if (matchColumnIndexes == null || matchColumnIndexes.length == 0
                || matchColumnIndexes[0] != columnIdx) {
            throw new TSQLException(Messages.getString("rowset.15")); //$NON-NLS-1$
        }

        matchColumnIndexes[0] = -1;
    }

    public void unsetMatchColumn(int[] columnIdxes) throws TSQLException {
        if (columnIdxes == null) {
            throw new NullPointerException();
        }

        if (columnIdxes.length == 0) {
            return;
        }

        if (matchColumnIndexes == null
                || matchColumnIndexes.length < columnIdxes.length) {
            throw new TSQLException(Messages.getString("rowset.15")); //$NON-NLS-1$
        }

        for (int i = 0; i < columnIdxes.length; i++) {
            if (matchColumnIndexes[i] != columnIdxes[i]) {
                throw new TSQLException(Messages.getString("rowset.15")); //$NON-NLS-1$    
            }
        }

        Arrays.fill(matchColumnIndexes, 0, columnIdxes.length, -1);
    }

    public void unsetMatchColumn(String columnName) throws TSQLException {
        if (matchColumnNames == null || matchColumnNames.length == 0
                || !matchColumnNames[0].equals(columnName)) {
            throw new TSQLException(Messages.getString("rowset.15")); //$NON-NLS-1$
        }

        matchColumnNames[0] = null;

    }

    public void unsetMatchColumn(String[] columnName) throws TSQLException {
        if (columnName == null) {
            throw new NullPointerException();
        }

        if (columnName.length == 0) {
            return;
        }

        if (matchColumnNames == null
                || matchColumnNames.length < columnName.length) {
            throw new TSQLException(Messages.getString("rowset.15")); //$NON-NLS-1$
        }

        for (int i = 0; i < columnName.length; i++) {
            if (matchColumnNames[i] != columnName[i]) {
                throw new TSQLException(Messages.getString("rowset.15")); //$NON-NLS-1$    
            }
        }

        Arrays.fill(matchColumnNames, 0, columnName.length, null);
    }

    public boolean absolute(int row) throws TSQLException {
        return doAbsolute(getIndexIncludeDeletedRows(row), true);
    }

    /**
     * internal implement of absolute
     * 
     * @param row
     *            index of row cursor to move, include deleted rows
     * @param checkType
     *            whether to check property ResultSet.TYPE_FORWARD_ONLY
     * @return whether the cursor is on result set
     * @throws TSQLException
     */
    protected boolean doAbsolute(int row, boolean checkType)
            throws TSQLException {
        if (isCursorOnInsert) {
            // rowset.0=Not a valid position
            throw new TSQLException(Messages.getString("rowset.0")); //$NON-NLS-1$
        }
        if (rows == null || rows.size() == 0) {
            if (isNotifyListener) {
                notifyCursorMoved();
            }
            return false;
        }

        if (checkType && getType() == TResultSet.TYPE_FORWARD_ONLY) {
            // rowset.8=The Result Set Type is TYPE_FORWARD_ONLY
            throw new TSQLException(Messages.getString("rowset.8")); //$NON-NLS-1$
        }

        if (row < 0) {
            row = rows.size() + row + 1;
        }

        if (row <= 0) {
            currentRowIndex = 0;
            currentRow = null;
            if (isNotifyListener) {
                notifyCursorMoved();
            }
            return false;
        }

        if (row > rows.size()) {
            currentRowIndex = rows.size() + 1;
            currentRow = null;
            if (isNotifyListener) {
                notifyCursorMoved();
            }
            return false;
        }

        currentRowIndex = row;
        currentRow = rows.get(currentRowIndex - 1);
        if (isNotifyListener) {
            notifyCursorMoved();
        }
        return true;
    }

    public void afterLast() throws TSQLException {
        if (rows == null) {
            return;
        }

        doAbsolute(rows.size() + 1, true);
    }

    public void beforeFirst() throws TSQLException {
        doAbsolute(0, true);
    }

    public void cancelRowUpdates() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }

        if (rowUpdated()) {
            currentRow.restoreOriginal();
            if (isNotifyListener) {
                notifyRowChanged();
            }
        }
    }

    public void clearWarnings() throws TSQLException {
        sqlwarn = null;
    }

    public void close() throws TSQLException {
        String username = getUsername();
        String password = getPassword();
        initialProperties();
        setUsername(username);
        setPassword(password);

        rows = new ArrayList<CachedRow>();
        currentRowIndex = 0;
        currentRow = null;
        deletedRowCount = 0;
        isCursorOnInsert = false;
        isLastColNull = false;
        matchColumnNames = null;
        matchColumnIndexes = null;
        conn = null;
    }

    public void deleteRow() throws TSQLException {
        checkValidRow();
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }
        currentRow.setDelete();
        deletedRowCount++;
        if (isNotifyListener) {
            notifyRowChanged();
        }
    }

    private void checkValidRow() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }
    }

    /**
     * convert <code>index</code> consider property <code>showDeleted</code>.
     * If <code>showDeleted</code> is true, do nothing, otherwise, re-compute
     * <code>index</code> add deleted rows.
     * 
     * @param index
     *            maybe negative, indicate the row number counting from the end
     *            of the result set
     * @return row index include delted rows
     */
    private int getIndexIncludeDeletedRows(int index) throws TSQLException {
        if (rows == null || rows.size() == 0) {
            return -1;
        }

        if (getShowDeleted()) {
            return index;
        }

        if (index == 0) {
            return 0;
        }

        if (index > 0) {
            int indexIncludeDeletedRows = 0;
            for (; index > 0; ++indexIncludeDeletedRows) {
                if (indexIncludeDeletedRows == rows.size()) {
                    indexIncludeDeletedRows += index;
                    break;
                }

                if (!rows.get(indexIncludeDeletedRows).isDelete()) {
                    index--;
                }
            }
            return indexIncludeDeletedRows;
        }

        // index < 0
        int indexIncludeDeletedRows = rows.size();
        for (; index < 0; --indexIncludeDeletedRows) {
            if (indexIncludeDeletedRows == 0) {
                break;
            }

            if (!rows.get(indexIncludeDeletedRows - 1).isDelete()) {
                index++;
            }
        }
        if (indexIncludeDeletedRows != 0) {
            indexIncludeDeletedRows++;
        }

        return indexIncludeDeletedRows;
    }

    /**
     * If <code>showDeleted</code> property is true, return the rows size
     * include deleted rows. Otherwise not include deleted rows.
     * 
     * @return
     * @throws TSQLException
     */
    private int getValidRowSize() throws TSQLException {
        if (rows == null) {
            return 0;
        }

        if (getShowDeleted()) {
            return rows.size();
        }

        return rows.size() - deletedRowCount;
    }

    public int findColumn(String columnName) throws TSQLException {
        return getIndexByName(columnName);
    }

    public boolean first() throws TSQLException {
        return doAbsolute(getIndexIncludeDeletedRows(1), true);
    }

    public TArray getArray(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TArray) {
            return (TArray) obj;
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public TArray getArray(String colName) throws TSQLException {
        return getArray(getIndexByName(colName));
    }

    public InputStream getAsciiStream(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            obj = ((String) obj).toCharArray();
        }

        if (obj instanceof char[]) {
            char[] cs = (char[]) obj;
            byte[] bs = new byte[cs.length];

            for (int i = 0; i < cs.length; i++) {
                // if out of range, convert to unknown char ox3F
                if (cs[i] > Byte.MAX_VALUE || cs[i] < Byte.MIN_VALUE) {
                    bs[i] = 63;
                } else {
                    bs[i] = (byte) cs[i];
                }
            }

            return new ByteArrayInputStream(bs);
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public InputStream getAsciiStream(String columnName) throws TSQLException {
        return getAsciiStream(getIndexByName(columnName));
    }

    public BigDecimal getBigDecimal(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws TSQLException {
        BigDecimal big = getBigDecimal(columnIndex);
        if (big == null) {
            return null;
        }
        return big.setScale(scale);
    }

    public BigDecimal getBigDecimal(String columnName) throws TSQLException {
        return getBigDecimal(getIndexByName(columnName));
    }

    public BigDecimal getBigDecimal(String columnName, int scale)
            throws TSQLException {
        return getBigDecimal(getIndexByName(columnName), scale);
    }

    public InputStream getBinaryStream(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) obj);
        }

        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public InputStream getBinaryStream(String columnName) throws TSQLException {
        return getBinaryStream(getIndexByName(columnName));
    }

    public TBlob getBlob(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TBlob) {
            return (TBlob) obj;
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public TBlob getBlob(String columnName) throws TSQLException {
        return getBlob(getIndexByName(columnName));
    }

    public boolean getBoolean(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            if ("0".equals(obj.toString()) || "0.0".equals(obj.toString())) { //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
            return true;
        }
        // rowset.10=Data type mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public boolean getBoolean(String columnName) throws TSQLException {
        return getBoolean(getIndexByName(columnName));
    }

    public byte getByte(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Byte) {
            return (Byte) obj;
        }
        try {
            return Byte.parseByte(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public byte getByte(String columnName) throws TSQLException {
        return getByte(getIndexByName(columnName));
    }

    public byte[] getBytes(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public byte[] getBytes(String columnName) throws TSQLException {
        return getBytes(getIndexByName(columnName));
    }

    public Reader getCharacterStream(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return new StringReader((String) obj);
        }

        if (obj instanceof byte[]) {
            return new StringReader(new String((byte[]) obj));
        }

        if (obj instanceof char[]) {
            return new StringReader(new String((char[]) obj));
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public Reader getCharacterStream(String columnName) throws TSQLException {
        return getCharacterStream(getIndexByName(columnName));
    }

    public TClob getClob(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TClob) {
            return (TClob) obj;
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public TClob getClob(String colName) throws TSQLException {
        return getClob(getIndexByName(colName));
    }

    public String getCursorName() throws TSQLException {
        if (cursorName == null) {
            // rowset.14=Positioned updates not supported
            throw new TSQLException(Messages.getString("rowset.14")); //$NON-NLS-1$
        }
        return cursorName;
    }

    public TDate getDate(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TDate) {
            return (TDate) obj;
        } else if (obj instanceof TTimestamp) {
            return new TDate(((TTimestamp) obj).getTime());
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public TDate getDate(int columnIndex, Calendar cal) throws TSQLException {
        TDate date = getDate(columnIndex);
        if (date == null) {
            return null;
        }

        Calendar tempCal = Calendar.getInstance(cal.getTimeZone());
        tempCal.setTime(date);
        cal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH),
                tempCal.get(Calendar.DAY_OF_MONTH));
        return new TDate(cal.getTimeInMillis());
    }

    public TDate getDate(String columnName) throws TSQLException {
        return getDate(getIndexByName(columnName));
    }

    public TDate getDate(String columnName, Calendar cal) throws TSQLException {
        return getDate(getIndexByName(columnName), cal);
    }

    public double getDouble(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Double) {
            return (Double) obj;
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public double getDouble(String columnName) throws TSQLException {
        return getDouble(getIndexByName(columnName));
    }

    public float getFloat(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Float) {
            return (Float) obj;
        }
        try {
            return Float.parseFloat(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public float getFloat(String columnName) throws TSQLException {
        return getFloat(getIndexByName(columnName));
    }

    public int getInt(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public int getInt(String columnName) throws TSQLException {
        return getInt(getIndexByName(columnName));
    }

    public long getLong(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public long getLong(String columnName) throws TSQLException {
        return getLong(getIndexByName(columnName));
    }

    public TResultSetMetaData getMetaData() throws TSQLException {
        return meta;
    }

    public Object getObject(int columnIndex) throws TSQLException {
        if (meta == null || currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }
        if (columnIndex <= 0 || columnIndex > columnCount) {
            // sql.27=Invalid column index :{0}
            throw new TSQLException(Messages.getString("sql.27", columnIndex)); //$NON-NLS-1$
        }

        Object obj = currentRow.getObject(columnIndex);
        if (obj == null) {
            isLastColNull = true;
        } else {
            isLastColNull = false;
        }
        return obj;
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map)
            throws TSQLException {
        // FIXME the usage of map
        return getObject(columnIndex);
    }

    public Object getObject(String columnName) throws TSQLException {
        return getObject(getIndexByName(columnName));
    }

    public Object getObject(String columnName, Map<String, Class<?>> map)
            throws TSQLException {
        return getObject(getIndexByName(columnName), map);
    }

    public TRef getRef(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TRef) {
            return (TRef) obj;
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public TRef getRef(String colName) throws TSQLException {
        return getRef(getIndexByName(colName));
    }

    public int getRow() throws TSQLException {
        if (currentRow == null || rows == null || isCursorOnInsert) {
            return 0;
        }

        if (!getShowDeleted() && currentRow.isDelete()) {
            return 0;
        }

        if (getShowDeleted() || currentRowIndex == 0) {
            return currentRowIndex;
        }

        // doesn't show deleted rows, skip them
        int index = 0;
        for (int i = 0; i < currentRowIndex; ++i) {
            if (!rows.get(i).isDelete()) {
                index++;
            }
        }
        return index;

    }

    public short getShort(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Short) {
            return (Short) obj;
        }
        try {
            return Short.parseShort(obj.toString());
        } catch (NumberFormatException e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public short getShort(String columnName) throws TSQLException {
        return getShort(getIndexByName(columnName));
    }

    public TStatement getStatement() throws TSQLException {
        return null;
    }

    // columnIndex: from 1 rather than 0
    public String getString(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    private void checkColumnValid(int columnIndex) throws TSQLException {
        if (columnIndex <= 0 || columnIndex > meta.getColumnCount()) {
            // sql.27=Invalid column index :{0}
            throw new TSQLException(Messages.getString("sql.27", Integer //$NON-NLS-1$
                    .valueOf(columnIndex)));
        }
    }

    public String getString(String columnName) throws TSQLException {
        return getString(getIndexByName(columnName));
    }

    public TTime getTime(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TTime) {
            return (TTime) obj;
        } else if (obj instanceof TTimestamp) {
            return new TTime(((TTimestamp) obj).getTime());
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public TTime getTime(int columnIndex, Calendar cal) throws TSQLException {
        TTime time = getTime(columnIndex);
        if (time == null) {
            return null;
        }
        Calendar tempCal = Calendar.getInstance(cal.getTimeZone());
        tempCal.setTimeInMillis(time.getTime());
        cal.set(Calendar.HOUR, tempCal.get(Calendar.HOUR));
        cal.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, tempCal.get(Calendar.SECOND));
        return new TTime(cal.getTimeInMillis());
    }

    public TTime getTime(String columnName) throws TSQLException {
        return getTime(getIndexByName(columnName));
    }

    public TTime getTime(String columnName, Calendar cal) throws TSQLException {
        return getTime(getIndexByName(columnName), cal);
    }

    public TTimestamp getTimestamp(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof TDate) {
            return new TTimestamp(((TDate) obj).getTime());
        } else if (obj instanceof TTime) {
            return new TTimestamp(((TTime) obj).getTime());
        }
        try {
            return TTimestamp.valueOf(obj.toString());
        } catch (Exception e) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }
    }

    public TTimestamp getTimestamp(int columnIndex, Calendar cal)
            throws TSQLException {
        TTimestamp timestamp = getTimestamp(columnIndex);
        if (timestamp == null) {
            return null;
        }
        Calendar tempCal = Calendar.getInstance(cal.getTimeZone());
        tempCal.setTimeInMillis(timestamp.getTime());
        cal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONDAY),
                tempCal.get(Calendar.DAY_OF_MONTH), tempCal.get(Calendar.HOUR),
                tempCal.get(Calendar.MINUTE), tempCal.get(Calendar.SECOND));
        return new TTimestamp(cal.getTimeInMillis());
    }

    public TTimestamp getTimestamp(String columnName) throws TSQLException {
        return getTimestamp(getIndexByName(columnName));
    }

    public TTimestamp getTimestamp(String columnName, Calendar cal)
            throws TSQLException {
        return getTimestamp(getIndexByName(columnName), cal);
    }

    public org.teavm.classlib.java.net.TURL getURL(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof org.teavm.classlib.java.net.TURL) {
            return (org.teavm.classlib.java.net.TURL) obj;
        }
        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public org.teavm.classlib.java.net.TURL getURL(String columnName) throws TSQLException {
        return getURL(getIndexByName(columnName));
    }

    @SuppressWarnings("deprecation")
    public InputStream getUnicodeStream(int columnIndex) throws TSQLException {
        Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return new StringBufferInputStream(new String((byte[]) obj));
        }

        if (obj instanceof String) {
            return new StringBufferInputStream((String) obj);
        }

        if (obj instanceof char[]) {
            return new StringBufferInputStream(new String((char[]) obj));
        }

        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public InputStream getUnicodeStream(String columnName) throws TSQLException {
        return getUnicodeStream(getIndexByName(columnName));
    }

    public TSQLWarning getWarnings() throws TSQLException {
        if (sqlwarn == null) {
            return null;
        }
        if (currentRow != null && currentRow.getSqlWarning() != null) {
            return currentRow.getSqlWarning();
        }
        return sqlwarn;
    }

    public void insertRow() throws TSQLException {
        checkValidRow();
        if (currentRow != insertRow) {
            // rowset.4=Not an insert row
            throw new TSQLException(Messages.getString("rowset.4")); //$NON-NLS-1$
        }
        boolean isValueSet = false;
        for (int i = 0; i < columnCount; i++) {
            if (currentRow.getUpdateMask(i)) {
                isValueSet = true;
                break;
            }
        }
        if (!isValueSet) {
            // rowset.18=None column is updated
            throw new TSQLException(Messages.getString("rowset.18")); //$NON-NLS-1$
        }
        insertRow.setInsert();
        if (rememberedCursorPosition > rows.size()) {
            rows.add(insertRow);
        } else {
            rows.add(rememberedCursorPosition, insertRow);
        }
        insertRow = null;
        if (isNotifyListener) {
            notifyRowChanged();
        }
    }

    public boolean isAfterLast() throws TSQLException {
        if (rows == null || rows.size() == 0) {
            return false;
        }

        return currentRowIndex > rows.size();
    }

    public boolean isBeforeFirst() throws TSQLException {
        if (rows == null || rows.size() == 0) {
            return false;
        }

        return currentRowIndex == 0;
    }

    public boolean isFirst() throws TSQLException {
        return getRow() == 1;
    }

    public boolean isLast() throws TSQLException {
        if (rows == null || rows.size() == 0) {
            return false;
        }

        return getRow() == getValidRowSize();
    }

    public boolean last() throws TSQLException {
        return doAbsolute(getIndexIncludeDeletedRows(-1), true);
    }

    public void moveToCurrentRow() throws TSQLException {
        if (isCursorOnInsert) {
            currentRowIndex = rememberedCursorPosition;
            if (currentRowIndex >= 1 && currentRowIndex <= rows.size()) {
                currentRow = rows.get(currentRowIndex - 1);
            } else {
                currentRow = null;
            }
            rememberedCursorPosition = -1;
            isCursorOnInsert = false;
        }
    }

    public void moveToInsertRow() throws TSQLException {
        if (meta == null) {
            // rowset.26=The object has not been initialized
            throw new TSQLException(Messages.getString("rowset.26")); //$NON-NLS-1$
        }
        insertRow = new CachedRow(new Object[columnCount]);
        currentRow = insertRow;
        if (!isCursorOnInsert) {
            rememberedCursorPosition = currentRowIndex;
            currentRowIndex = -1;
            isCursorOnInsert = true;
        }
    }

    public boolean next() throws TSQLException {
        /*
         * spec next() is identical with relative(1), but they can't:
         * 
         * next() doesn't check TYPE_FORWARD_ONLY property, relative(1) does.
         */
        return doAbsolute(findNextValidRow(), false);
    }

    /**
     * Valid row is row which is visible to users. If <code>showDeleted</code>
     * is true, deleted rows are valid rows, otherwise deleted rows are invalid.
     * 
     * @return next valid row
     * @throws TSQLException
     */
    private int findNextValidRow() throws TSQLException {
        if (isCursorOnInsert) {
            // rowset.0=Not a valid position
            throw new TSQLException(Messages.getString("rowset.0")); //$NON-NLS-1$
        }
        if (rows == null || rows.size() == 0) {
            return -1;
        }
        int index = currentRowIndex + 1;

        if (getShowDeleted()) {
            return index;
        }

        if (index > rows.size()) {
            return rows.size() + 1;
        }

        while (index <= rows.size()) {
            if (!rows.get(index - 1).isDelete()) {
                break;
            }
            index++;
        }

        return index;
    }

    public boolean previous() throws TSQLException {
        return doAbsolute(findPreviousValidRow(), true);
    }

    /**
     * Valid row is row which is visible to users. If <code>showDeleted</code>
     * is true, deleted rows are valid rows, otherwise deleted rows are invalid.
     * 
     * @return previous valid row
     * @throws TSQLException
     */
    private int findPreviousValidRow() throws TSQLException {
        if (isCursorOnInsert) {
            // rowset.0=Not a valid position
            throw new TSQLException(Messages.getString("rowset.0")); //$NON-NLS-1$
        }
        if (rows == null || rows.size() == 0) {
            return -1;
        }
        int index = currentRowIndex - 1;

        if (index <= 0) {
            return 0;
        }

        if (getShowDeleted()) {
            return index;
        }

        while (index > 0) {
            if (!rows.get(index - 1).isDelete()) {
                break;
            }
            index--;
        }

        return index;
    }

    public void refreshRow() throws TSQLException {
        checkValidRow();
        if (isCursorOnInsert) {
            // rowset.0=Not a valid position
            throw new TSQLException(Messages.getString("rowset.0")); //$NON-NLS-1$
        }
        currentRow.restoreOriginal();
    }

    public boolean relative(int moveRows) throws TSQLException {
        checkValidRow();
        // TODO use more effective way to move cursor
        int index = getRow() + moveRows;

        if (isCursorOnInsert || currentRow.isDelete()) {
            if (moveRows > 0) {
                if (next()) {
                    index = getRow() + moveRows - 1;
                } else {
                    return false;
                }
            }

            if (moveRows < 0) {
                if (previous()) {
                    index = getRow() + moveRows + 1;
                } else {
                    return false;
                }
            }
        }

        if (index <= 0) {
            beforeFirst();
            return false;
        }

        if (index > rows.size()) {
            afterLast();
            return false;
        }

        return doAbsolute(getIndexIncludeDeletedRows(index), true);
    }

    public boolean rowDeleted() throws TSQLException {
        checkValidRow();
        return currentRow.isDelete();
    }

    public boolean rowInserted() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }
        return currentRow.isInsert();
    }

    public boolean rowUpdated() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }

        if (!currentRow.isUpdate()) {
            return false;
        }

        boolean sign = false;
        for (int i = 0; i < meta.getColumnCount(); ++i) {
            sign = currentRow.getUpdateMask(i) | sign;
        }
        return sign;
    }

    public void updateArray(int columnIndex, TArray x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateArray(String columnName, TArray x) throws TSQLException {
        updateArray(getIndexByName(columnName), x);
    }

    public void updateAsciiStream(int columnIndex, InputStream in, int length)
            throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        initInsertRow(columnIndex, in);

        Class<?> type = columnTypes[columnIndex - 1];
        if (type != null && !type.equals(String.class)
                && !type.equals(byte[].class)) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }

        if (length < 0) {
            throw new NegativeArraySizeException();
        }

        byte[] byteArray = null;
        try {
            if (length == 0 && in.read() == -1) {
                throw new IndexOutOfBoundsException();
            }

            byteArray = new byte[length];
            for (int i = 0; i < length; ++i) {
                int value = in.read();
                if (value == -1) {
                    throw new IndexOutOfBoundsException();
                }
                byteArray[i] = (byte) value;
            }
        } catch (IOException e) {
            TSQLException ex = new TSQLException();
            ex.initCause(e);
            throw ex;
        }

        try {
            updateString(columnIndex, new String(byteArray, "ISO-8859-1")); //$NON-NLS-1$
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws TSQLException {
        updateAsciiStream(getIndexByName(columnName), x, length);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws TSQLException {
        if (x == null) {
            throw new NullPointerException();
        }

        updateByType(columnIndex, x);
    }

    public void updateBigDecimal(String columnName, BigDecimal x)
            throws TSQLException {
        updateBigDecimal(getIndexByName(columnName), x);
    }

    public void updateBinaryStream(int columnIndex, InputStream in, int length)
            throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        initInsertRow(columnIndex, in);

        Class<?> type = columnTypes[columnIndex - 1];
        if (type != null && !type.equals(byte[].class)) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }

        if (length < 0) {
            throw new NegativeArraySizeException();
        }

        byte[] byteArray = new byte[length];
        try {
            for (int i = 0; i < length; ++i) {
                int value = in.read();
                if (value == -1) {
                    byteArray[i] = 0;
                } else {
                    byteArray[i] = (byte) value;
                }
            }

        } catch (IOException e) {
            TSQLException ex = new TSQLException();
            ex.initCause(e);
            throw ex;
        }

        updateBytes(columnIndex, byteArray);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws TSQLException {
        updateBinaryStream(getIndexByName(columnName), x, length);
    }

    public void updateBlob(int columnIndex, TBlob x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateBlob(String columnName, TBlob x) throws TSQLException {
        updateBlob(getIndexByName(columnName), x);
    }

    public void updateBoolean(int columnIndex, boolean x) throws TSQLException {
        updateByType(columnIndex, Boolean.valueOf(x));
    }

    public void updateBoolean(String columnName, boolean x) throws TSQLException {
        updateBoolean(getIndexByName(columnName), x);
    }

    public void updateByte(int columnIndex, byte x) throws TSQLException {
        updateByType(columnIndex, Byte.valueOf(x));
    }

    public void updateByte(String columnName, byte x) throws TSQLException {
        updateByte(getIndexByName(columnName), x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateBytes(String columnName, byte[] x) throws TSQLException {
        updateBytes(getIndexByName(columnName), x);
    }

    public void updateCharacterStream(int columnIndex, Reader in, int length)
            throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        initInsertRow(columnIndex, in);

        Class<?> type = columnTypes[columnIndex - 1];
        if (type != null && !type.equals(String.class)
                && !type.equals(byte[].class)) {
            // rowset.10=Data Type Mismatch
            throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
        }

        if (length < 0) {
            throw new NegativeArraySizeException();
        }

        StringWriter out = new StringWriter();
        try {
            for (int i = 0; i < length; ++i) {
                int value = in.read();
                if (value == -1) {
                    throw new IndexOutOfBoundsException();
                }
                out.write(value);
            }
        } catch (IOException e) {
            TSQLException ex = new TSQLException();
            ex.initCause(e);
            throw ex;
        }

        updateString(columnIndex, out.toString());
    }

    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws TSQLException {
        updateCharacterStream(getIndexByName(columnName), reader, length);
    }

    public void updateClob(int columnIndex, TClob x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateClob(String columnName, TClob x) throws TSQLException {
        updateClob(getIndexByName(columnName), x);
    }

    public void updateDate(int columnIndex, TDate x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateDate(String columnName, TDate x) throws TSQLException {
        updateDate(getIndexByName(columnName), x);
    }

    public void updateDouble(int columnIndex, double x) throws TSQLException {
        updateByType(columnIndex, Double.valueOf(x));
    }

    public void updateDouble(String columnName, double x) throws TSQLException {
        updateDouble(getIndexByName(columnName), x);
    }

    public void updateFloat(int columnIndex, float x) throws TSQLException {
        updateByType(columnIndex, Float.valueOf(x));
    }

    public void updateFloat(String columnName, float x) throws TSQLException {
        updateFloat(getIndexByName(columnName), x);
    }

    public void updateInt(int columnIndex, int x) throws TSQLException {
        updateByType(columnIndex, Integer.valueOf(x));
    }

    public void updateInt(String columnName, int x) throws TSQLException {
        updateInt(getIndexByName(columnName), x);
    }

    public void updateLong(int columnIndex, long x) throws TSQLException {
        updateByType(columnIndex, Long.valueOf(x));
    }

    public void updateLong(String columnName, long x) throws TSQLException {
        updateLong(getIndexByName(columnName), x);
    }

    public void updateNull(int columnIndex) throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        currentRow.updateObject(columnIndex, null);
    }

    public void updateNull(String columnName) throws TSQLException {
        updateNull(getIndexByName(columnName));
    }

    /**
     * note check type compatibility
     */
    public void updateObject(int columnIndex, Object x) throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        initInsertRow(columnIndex, x);
        currentRow.updateObject(columnIndex, x);
    }

    public void updateObject(int columnIndex, Object x, int scale)
            throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        Class<?> type = columnTypes[columnIndex - 1];
        // ava.sql.Types.DECIMA or java.sql.Types.NUMERIC types
        if (type.equals(BigDecimal.class)) {
            /*
             * TODO ri doesn't check type of x and only support BigDecimal,
             * should we follow ri here? If not, uncomment below fragment of
             * code
             */
            // if (x instanceof BigDecimal) {
            x = ((BigDecimal) x).setScale(scale);
            // } else if (x instanceof Double) {
            // x = new BigDecimal(((Double) x).doubleValue());
            // x = ((BigDecimal) x).setScale(scale);
            // } else if (x instanceof Float) {
            // x = new BigDecimal(((Float) x).doubleValue());
            // x = ((BigDecimal) x).setScale(scale);
            // }
        }
        initInsertRow(columnIndex, x);
        currentRow.updateObject(columnIndex, x);
    }

    public void updateObject(String columnName, Object x) throws TSQLException {
        updateObject(getIndexByName(columnName), x);
    }

    public void updateObject(String columnName, Object x, int scale)
            throws TSQLException {
        updateObject(getIndexByName(columnName), x, scale);
    }

    public void updateRef(int columnIndex, TRef x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateRef(String columnName, TRef x) throws TSQLException {
        updateRef(getIndexByName(columnName), x);
    }

    public void updateRow() throws TSQLException {
        if (currentRow == null) {
            // rowset.7=Not a valid cursor
            throw new TSQLException(Messages.getString("rowset.7")); //$NON-NLS-1$
        }
        if (isCursorOnInsert) {
            // rowset.11=Illegal operation on an insert row
            throw new TSQLException(Messages.getString("rowset.11")); //$NON-NLS-1$
        }
        if (getConcurrency() == (TResultSet.CONCUR_READ_ONLY)) {
            // rowset.17=The Result Set is CONCUR_READ_ONLY
            throw new TSQLException(Messages.getString("rowset.17")); //$NON-NLS-1$
        }
        currentRow.setUpdate();
        if (isNotifyListener) {
            notifyRowChanged();
        }
    }

    public void updateShort(int columnIndex, short x) throws TSQLException {
        updateByType(columnIndex, Short.valueOf(x));
    }

    public void updateShort(String columnName, short x) throws TSQLException {
        updateShort(getIndexByName(columnName), x);
    }

    public void updateString(int columnIndex, String x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    /**
     * Check type compatibility and update value
     * 
     * @param columnIndex
     * @param value
     * @throws TSQLException
     */
    private void updateByType(int columnIndex, Object value)
            throws TSQLException {
        checkValidRow();
        checkColumnValid(columnIndex);
        initInsertRow(columnIndex, value);
        currentRow.updateObject(columnIndex, convertUpdateValue(columnIndex,
                value));
    }

    /**
     * The implementation of FilteredRowSet would override this method. The
     * parameters also are used for override.
     * 
     * @param columnIndex
     * @param value
     */
    protected void initInsertRow(int columnIndex, Object value)
            throws TSQLException {
        if (isCursorOnInsert && insertRow == null) {
            insertRow = new CachedRow(new Object[columnCount]);
            currentRow = insertRow;
        }
    }

    /**
     * Convert <code>value</code> to the JDBC type of the
     * <code>columnIndex</code>. The columnIndex is not checked in this
     * method, so caller must be sure the <code>columnIndex</code> is valid,
     * or invoke <code>checkColumnValid</code> before invoke this method.
     * 
     * TODO any better ways to do this?
     * 
     * @param columnIndex
     *            index of column to be updated
     * @param value
     *            the new value to be updated
     */
    @SuppressWarnings("boxing")
    private Object convertUpdateValue(int columnIndex, Object value)
            throws TSQLException {

        if (value == null) {
            return value;
        }

        Class<?> type = columnTypes[columnIndex - 1];

        /*
         * TODO if type == null, the type mapping is not supported by Harmony
         * now, leave this type check to JDBC driver
         */

        if (type == null) {
            return value;
        }

        // convert to serializable object
        if (type.isInstance(value)) {
            if (type.equals(TArray.class) && !(value instanceof SerialArray)) {
                return new SerialArray((TArray) value);
            }

            if (type.equals(TBlob.class) && !(value instanceof SerialBlob)) {
                return new SerialBlob((TBlob) value);
            }

            if (type.equals(TClob.class) && !(value instanceof SerialClob)) {
                return new SerialClob((TClob) value);
            }

            if (type.equals(TRef.class) && !(value instanceof SerialRef)) {
                return new SerialRef((TRef) value);
            }

            return value;
        }

        if (type.equals(byte[].class)) {
            return value;
        }

        if (type.equals(String.class)) {
            if (!(value instanceof TArray) && !(value instanceof byte[])) {
                return value.toString();
            }
        }

        if (type.equals(Integer.class)) {
            if (value instanceof Integer || value instanceof Short
                    || value instanceof Byte) {
                return value;
            }

            if (value instanceof Long) {
                long l = ((Long) value).longValue();
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return (int) l;
                }
            }

            if (value instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal) value;
                try {
                    return bigDecimal.intValueExact();

                } catch (ArithmeticException e) {
                    // rowset.10=Data Type Mismatch
                    throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
                }
            }

            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(Short.class)) {
            if (value instanceof Short || value instanceof Byte) {
                return value;
            }
            if (value instanceof Long) {
                long l = ((Long) value).longValue();
                if (l >= Short.MIN_VALUE && l <= Short.MAX_VALUE) {
                    return (short) l;
                }
            }
            if (value instanceof Integer) {
                int i = ((Integer) value).intValue();
                if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
                    return (short) i;
                }
            }
            if (value instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal) value;
                try {
                    return bigDecimal.intValueExact();

                } catch (ArithmeticException e) {
                    // rowset.10=Data Type Mismatch
                    throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
                }
            }
            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(Byte.class)) {
            if (value instanceof Byte) {
                return value;
            }
            if (value instanceof Long) {
                long l = ((Long) value).longValue();
                if (l >= Byte.MIN_VALUE && l <= Byte.MAX_VALUE) {
                    return (byte) l;
                }
            }
            if (value instanceof Integer) {
                int i = ((Integer) value).intValue();
                if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
                    return (byte) i;
                }
            }
            if (value instanceof Short) {
                int i = ((Short) value).shortValue();
                if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
                    return (byte) i;
                }
            }
            if (value instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal) value;
                try {
                    return bigDecimal.byteValueExact();

                } catch (ArithmeticException e) {
                    // rowset.10=Data Type Mismatch
                    throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
                }
            }
            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(Long.class)) {
            if (value instanceof Integer || value instanceof Short
                    || value instanceof Byte || value instanceof Long) {
                return value;
            }
            if (value instanceof BigDecimal) {
                BigDecimal bigDecimal = (BigDecimal) value;
                try {
                    return bigDecimal.longValueExact();

                } catch (ArithmeticException e) {
                    // rowset.10=Data Type Mismatch
                    throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
                }
            }
            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(Float.class) || type.equals(Double.class)) {
            if (value instanceof Float || value instanceof Double
                    || value instanceof BigDecimal) {
                return value;
            }
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(BigDecimal.class)) {
            return value;
        }

        if (type.equals(TDate.class)) {
            if (value instanceof TTimestamp) {
                TTimestamp timestamp = (TTimestamp) value;
                return new TDate(timestamp.getTime());
            }

            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(TTime.class)) {
            if (value instanceof TTimestamp) {
                TTimestamp timestamp = (TTimestamp) value;
                return new TTime(timestamp.getTime());
            }

            if (value instanceof String) {
                return value;
            }
        }

        if (type.equals(TTimestamp.class)) {
            if (value instanceof TDate) {
                TDate date = (TDate) value;
                return new TTimestamp(date.getTime());
            }
            if (value instanceof TTime) {
                TTime time = (TTime) value;
                return new TTimestamp(time.getTime());
            }

            if (value instanceof String) {
                return value;
            }
        }

        // rowset.10=Data Type Mismatch
        throw new TSQLException(Messages.getString("rowset.10")); //$NON-NLS-1$
    }

    public void updateString(String columnName, String x) throws TSQLException {
        updateString(getIndexByName(columnName), x);
    }

    public void updateTime(int columnIndex, TTime x) throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateTime(String columnName, TTime x) throws TSQLException {
        updateTime(getIndexByName(columnName), x);
    }

    public void updateTimestamp(int columnIndex, TTimestamp x)
            throws TSQLException {
        updateByType(columnIndex, x);
    }

    public void updateTimestamp(String columnName, TTimestamp x)
            throws TSQLException {
        updateTimestamp(getIndexByName(columnName), x);
    }

    public boolean wasNull() throws TSQLException {
        return isLastColNull;
    }

    public void execute() throws TSQLException {
        TConnection preConn = conn;
        execute(retrieveConnection());
        conn = preConn;
    }

    public TConnection getConnection() throws TSQLException {
        return conn;
    }

    private TConnection retrieveConnection() throws TSQLException {
        if (getUrl() != null) {
            return TDriverManager.getConnection(getUrl(), getUsername(),
                    getPassword());
        } else if (getDataSourceName() != null) {
            try {
                Context contex = new InitialContext();
                DataSource ds = (DataSource) contex.lookup(getDataSourceName());
                return ds.getConnection();
            } catch (Exception e) {
                // rowset.25=(JNDI)Unable to get connection
                TSQLException ex = new TSQLException(Messages
                        .getString("rowset.25")); //$NON-NLS-1$
                throw ex;
            }
        }
        // rowset.24=Unable to get connection
        throw new TSQLException(Messages.getString("rowset.24")); //$NON-NLS-1$
    }

    CachedRow getCurrentRow() {
        return currentRow;
    }

    @Override
    public void setCommand(String cmd) throws TSQLException {
        initParams();
        super.setCommand(cmd);
    }

    protected boolean isCursorOnInsert() {
        return isCursorOnInsert;
    }

    protected boolean isNotifyListener() {
        return isNotifyListener;
    }

    protected void setIsNotifyListener(boolean isNotifyListener) {
        this.isNotifyListener = isNotifyListener;
    }

    protected CachedRow getInsertRow() {
        return insertRow;
    }
}
