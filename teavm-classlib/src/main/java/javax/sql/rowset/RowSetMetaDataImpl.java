/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.sql.rowset;

import java.io.Serializable;

import javax.sql.RowSetMetaData;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.sql.TResultSetMetaData;
import org.teavm.classlib.java.sql.TSQLException;

/**
 * This class is a concrete implementation of javax.sql.RowSetMetatData, which
 * provides methods that get and set column information.
 * 
 * A RowSetMetaDataImpl object can be obtained by the getMetaData() method in
 * javax.sql.RowSet.
 * 
 */
public class RowSetMetaDataImpl implements RowSetMetaData, Serializable {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final int DEFAULT_COLUMN_COUNT = 5;

    private static final long serialVersionUID = 6893806403181801867L;

    private int colCount;

    private ColInfo[] colInfo;

    /**
     * The default constructor.
     */
    public RowSetMetaDataImpl() {
        // do nothing
    }

    private void checkNegativeValue(int value, String msg) throws TSQLException {
        if (value < 0) {
            throw new TSQLException(Messages.getString(msg));
        }
    }

    private void checkColumnIndex(int columnIndex) throws TSQLException {
        if (null == colInfo || columnIndex < 1 || columnIndex >= colInfo.length) {
            throw new TSQLException(Messages
                    .getString("sql.27", columnIndex + 1)); //$NON-NLS-1$
        }
        // lazy initialization
        if (null == colInfo[columnIndex]) {
            colInfo[columnIndex] = new ColInfo();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setColumnCount(int)
     */
    public void setColumnCount(int columnCount) throws TSQLException {
        if (columnCount <= 0) {
            throw new TSQLException(Messages.getString("sql.26")); //$NON-NLS-1$
        }
        try {
            if (columnCount + 1 > 0) {
                colInfo = new ColInfo[columnCount + 1];
            } else {
                colInfo = new ColInfo[DEFAULT_COLUMN_COUNT];
            }
        } catch (OutOfMemoryError e) {
            // For compatibility, use same default value as RI
            colInfo = new ColInfo[DEFAULT_COLUMN_COUNT];
        }
        colCount = columnCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setAutoIncrement(int, boolean)
     */
    public void setAutoIncrement(int columnIndex, boolean property)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].autoIncrement = property;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setCaseSensitive(int, boolean)
     */
    public void setCaseSensitive(int columnIndex, boolean property)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].caseSensitive = property;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setSearchable(int, boolean)
     */
    public void setSearchable(int columnIndex, boolean property)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].searchable = property;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setCurrency(int, boolean)
     */
    public void setCurrency(int columnIndex, boolean property)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].currency = property;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setNullable(int, int)
     */
    public void setNullable(int columnIndex, int property) throws TSQLException {
        if (property != TResultSetMetaData.columnNoNulls
                && property != TResultSetMetaData.columnNullable
                && property != TResultSetMetaData.columnNullableUnknown) {
            throw new TSQLException(Messages.getString("sql.29")); //$NON-NLS-1$
        }

        checkColumnIndex(columnIndex);
        colInfo[columnIndex].nullable = property;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setSigned(int, boolean)
     */
    public void setSigned(int columnIndex, boolean property)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].signed = property;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setColumnDisplaySize(int, int)
     */
    public void setColumnDisplaySize(int columnIndex, int size)
            throws TSQLException {
        checkNegativeValue(size, "sql.30"); //$NON-NLS-1$

        checkColumnIndex(columnIndex);
        colInfo[columnIndex].columnDisplaySize = size;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setColumnLabel(int, String)
     */
    public void setColumnLabel(int columnIndex, String label)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].columnLabel = label == null ? EMPTY_STRING : label;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setColumnName(int, String)
     */
    public void setColumnName(int columnIndex, String columnName)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].columnName = columnName == null ? EMPTY_STRING
                : columnName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setSchemaName(int, String)
     */
    public void setSchemaName(int columnIndex, String schemaName)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].schemaName = schemaName == null ? EMPTY_STRING
                : schemaName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setPrecision(int, int)
     */
    public void setPrecision(int columnIndex, int precision)
            throws TSQLException {
        checkNegativeValue(precision, "sql.31"); //$NON-NLS-1$

        checkColumnIndex(columnIndex);
        colInfo[columnIndex].precision = precision;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setScale(int, int)
     */
    public void setScale(int columnIndex, int scale) throws TSQLException {
        checkNegativeValue(scale, "sql.32"); //$NON-NLS-1$

        checkColumnIndex(columnIndex);
        colInfo[columnIndex].scale = scale;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setTableName(int, String)
     */
    public void setTableName(int columnIndex, String tableName)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].tableName = tableName == null ? EMPTY_STRING
                : tableName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setCatalogName(int, String)
     */
    public void setCatalogName(int columnIndex, String catalogName)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].catalogName = catalogName == null ? EMPTY_STRING
                : catalogName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setColumnType(int, int)
     */
    public void setColumnType(int columnIndex, int SQLType) throws TSQLException {
        SqlUtil.validateType(SQLType);

        checkColumnIndex(columnIndex);
        colInfo[columnIndex].colType = SQLType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.sql.RowSetMetaData#setColumnTypeName(int, String)
     */
    public void setColumnTypeName(int columnIndex, String typeName)
            throws TSQLException {
        checkColumnIndex(columnIndex);
        colInfo[columnIndex].colTypeName = typeName == null ? EMPTY_STRING
                : typeName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnCount()
     */
    public int getColumnCount() throws TSQLException {
        return colCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].autoIncrement;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isCaseSensitive(int)
     */
    public boolean isCaseSensitive(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].caseSensitive;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isSearchable(int)
     */
    public boolean isSearchable(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].searchable;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isCurrency(int)
     */
    public boolean isCurrency(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].currency;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isNullable(int)
     */
    public int isNullable(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].nullable;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isSigned(int)
     */
    public boolean isSigned(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].signed;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnDisplaySize(int)
     */
    public int getColumnDisplaySize(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].columnDisplaySize;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnLabel(int)
     */
    public String getColumnLabel(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].columnLabel;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnName(int)
     */
    public String getColumnName(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].columnName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getSchemaName(int)
     */
    public String getSchemaName(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].schemaName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getPrecision(int)
     */
    public int getPrecision(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].precision;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getScale(int)
     */
    public int getScale(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].scale;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getTableName(int)
     */
    public String getTableName(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].tableName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getCatalogName(int)
     */
    public String getCatalogName(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].catalogName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnType(int)
     */
    public int getColumnType(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].colType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnTypeName(int)
     */
    public String getColumnTypeName(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].colTypeName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isReadOnly(int)
     */
    public boolean isReadOnly(int columnIndex) throws TSQLException {
        return !isWritable(columnIndex);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isWritable(int)
     */
    public boolean isWritable(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].writeable;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#isDefinitelyWritable(int)
     */
    public boolean isDefinitelyWritable(int columnIndex) throws TSQLException {
        checkColumnIndex(columnIndex);
        return colInfo[columnIndex].definiteWritable;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TResultSetMetaData#getColumnClassName(int)
     */
    public String getColumnClassName(int columnIndex) throws TSQLException {
        return SqlUtil.getClassNameByType(getColumnType(columnIndex));
    }

    /**
     * The inner class to store meta information of columns.
     */
    private class ColInfo implements Serializable {

        private static final long serialVersionUID = 5490834817919311283L;

        public boolean autoIncrement;

        public boolean caseSensitive;

        public boolean currency;

        public boolean signed;

        public boolean searchable;

        public boolean writeable = true;

        public boolean definiteWritable = true;

        public String columnLabel;

        public String columnName;

        public String schemaName = EMPTY_STRING;

        public String colTypeName;

        public int colType;

        public int nullable;

        public int columnDisplaySize;

        public int precision;

        public int scale;

        public String tableName = EMPTY_STRING;

        public String catalogName = EMPTY_STRING;
    }
}
