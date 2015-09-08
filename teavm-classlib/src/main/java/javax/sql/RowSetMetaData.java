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

package javax.sql;

import org.teavm.classlib.java.sql.TResultSetMetaData;
import org.teavm.classlib.java.sql.TSQLException;

/**
 * An interface which provides facilities for getting information about the
 * columns in a {@code RowSet}.
 * <p>
 * {@code RowSetMetaData} extends {@link org.teavm.classlib.java.sql.TResultSetMetaData}, adding new
 * operations for carrying out value sets.
 * <p>
 * Application code would not normally call this interface directly. It would be
 * called internally when {@code RowSet.execute} is called.
 *
 * @see RowSetInternal#setMetaData(RowSetMetaData)
 */
public interface RowSetMetaData extends TResultSetMetaData {

    /**
     * Sets automatic numbering for a specified column in the {@code RowSet}. If
     * automatic numbering is on, the column is read-only. The default value for
     * the auto increment parameter is {@code false}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param autoIncrement
     *            {@code true} to set automatic numbering on, {@code false} to
     *            turn it off (default).
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setAutoIncrement(int columnIndex, boolean autoIncrement)
            throws TSQLException;

    /**
     * Sets the case sensitive property for a specified column in the {@code
     * RowSet}. The default is that the column is not case sensitive.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param caseSensitive
     *            {@code true} to make the column case sensitive, {@code false}
     *            to make it case insensitive (default).
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setCaseSensitive(int columnIndex, boolean caseSensitive)
            throws TSQLException;

    /**
     * Sets the catalog name for a specified column in the {@code RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param catalogName
     *            the new catalog's name.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setCatalogName(int columnIndex, String catalogName)
            throws TSQLException;

    /**
     * Sets the number of columns contained in the row set.
     * 
     * @param columnCount
     *            the number of columns contained in the {@code RowSet}.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setColumnCount(int columnCount) throws TSQLException;

    /**
     * Sets the normal maximum width in characters for a specified column in the
     * {@code RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param displaySize
     *            the normal maximum column width in characters.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setColumnDisplaySize(int columnIndex, int displaySize)
            throws TSQLException;

    /**
     * Sets the suggested name as label for the column contained in the {@code
     * RowSet}. The label is an alias for printing and displaying purposes.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theLabel
     *            the alias name for the column.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setColumnLabel(int columnIndex, String theLabel)
            throws TSQLException;

    /**
     * Sets the column name for a specified column in the {@code RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theColumnName
     *            the column's label.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setColumnName(int columnIndex, String theColumnName)
            throws TSQLException;

    /**
     * Sets the SQL type for a specified column in the {@code RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theSQLType
     *            the SQL Type, as defined by {@code java.sql.Types}.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setColumnType(int columnIndex, int theSQLType)
            throws TSQLException;

    /**
     * Sets the type name for a specified column in the {@code RowSet}, where
     * the data type is specific to the data source.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theTypeName
     *            the SQL type name for the column.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setColumnTypeName(int columnIndex, String theTypeName)
            throws TSQLException;

    /**
     * Sets whether a specified column is a currency value. The default value is
     * {@code false}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param isCurrency
     *            {@code true} if the column should be treated as a currency
     *            value, {@code false} if it should not be treated as a currency
     *            value (default).
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setCurrency(int columnIndex, boolean isCurrency)
            throws TSQLException;

    /**
     * Sets whether a specified column can contain SQL {@code NULL} values.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param nullability
     *            an integer which is one of the following values:
     *            <ul>
     *            <li>{@code ResultSetMetaData.columnNoNulls}</li>
     *            <li>{@code ResultSetMetaData.columnNullable}</li>
     *            <li>{@code ResultSetMetaData.columnNullableUnknown}</li>
     *            </ul>
     *            <p>
     *            The default value is {@code
     *            ResultSetMetaData.columnNullableUnknown}.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setNullable(int columnIndex, int nullability)
            throws TSQLException;

    /**
     * Sets the number of decimal digits for a specified column in the {@code
     * RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param thePrecision
     *            the number of decimal digits.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setPrecision(int columnIndex, int thePrecision)
            throws TSQLException;

    /**
     * Declares how many decimal digits there should be after a decimal point
     * for the column specified by {@code columnIndex}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theScale
     *            the number of digits after the decimal point.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setScale(int columnIndex, int theScale) throws TSQLException;

    /**
     * Sets the schema name for a specified column in the {@code RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theSchemaName
     *            a {@code String} containing the schema name.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setSchemaName(int columnIndex, String theSchemaName)
            throws TSQLException;

    /**
     * Sets whether a specified column can be used in a search involving a
     * {@code WHERE} clause. The default value is {@code false}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param isSearchable
     *            {@code true} of the column can be used in a {@code WHERE}
     *            clause search, {@code false} otherwise.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setSearchable(int columnIndex, boolean isSearchable)
            throws TSQLException;

    /**
     * Sets if a specified column can contain signed numbers.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param isSigned
     *            {@code true} if the column can contain signed numbers, {@code
     *            false} otherwise.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setSigned(int columnIndex, boolean isSigned)
            throws TSQLException;

    /**
     * Sets the table name for a specified column in the {@code RowSet}.
     * 
     * @param columnIndex
     *            the index number for the column; the first column's index is
     *            1.
     * @param theTableName
     *            the table name for the column.
     * @throws TSQLException
     *             if a problem occurs accessing the database.
     */
    public void setTableName(int columnIndex, String theTableName)
            throws TSQLException;
}
