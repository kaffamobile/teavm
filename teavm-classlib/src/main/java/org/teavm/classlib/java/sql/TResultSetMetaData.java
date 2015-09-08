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

package org.teavm.classlib.java.sql;

/**
 * Provides information about the columns returned in a {@code ResultSet}.
 */
public interface TResultSetMetaData {

    /**
     * Indicates that a column cannot contain {@code NULL} values.
     */
    public static final int columnNoNulls = 0;

    /**
     * Indicates that a column can contain {@code NULL} values.
     */
    public static final int columnNullable = 1;

    /**
     * Indicates that it is unknown whether a column can contain {@code NULL}s or not.
     */
    public static final int columnNullableUnknown = 2;

    /**
     * Returns the title of an indexed column's catalog.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the catalog title.
     * @throws TSQLException
     *             if there is a database error.
     */
    public String getCatalogName(int column) throws TSQLException;

    /**
     * Returns the fully-qualified type of the class that is produced when
     * invoking {@code ResultSet.getObject} to recover this column's value.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the fully-qualified class name.
     * @throws TSQLException
     *             if there is a database error.
     * @see TResultSet#getObject
     */
    public String getColumnClassName(int column) throws TSQLException;

    /**
     * Returns number of columns contained in the associated result set.
     * 
     * @return the column count.
     * @throws TSQLException
     *             if there is a database error.
     */
    public int getColumnCount() throws TSQLException;

    /**
     * Returns the indexed column's standard maximum width, expressed in number
     * of characters.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the column's max width.
     * @throws TSQLException
     *             if there is a database error.
     */
    public int getColumnDisplaySize(int column) throws TSQLException;

    /**
     * Returns a recommended title for the indexed column, to be used when the
     * title needs to be displayed.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the column's title.
     * @throws TSQLException
     *             if there is a database error.
     */
    public String getColumnLabel(int column) throws TSQLException;

    /**
     * Returns the title of the indexed column.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the column title.
     * @throws TSQLException
     *             if there is a database error.
     */
    public String getColumnName(int column) throws TSQLException;

    /**
     * Returns the type of the indexed column as SQL type code.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the column type code.
     * @throws TSQLException
     *             if there is a database error.
     * @see TTypes
     */
    public int getColumnType(int column) throws TSQLException;

    /**
     * Returns the type name of the indexed column.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the type name.
     * @throws TSQLException
     *             if there is a database error.
     */
    public String getColumnTypeName(int column) throws TSQLException;

    /**
     * Returns the decimal precision of the indexed column.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the precision.
     * @throws TSQLException
     *             if there is a database error.
     */
    public int getPrecision(int column) throws TSQLException;

    /**
     * Returns the number of digits to the right of the decimal point of the
     * indexed column.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return number of decimal places.
     * @throws TSQLException
     *             if there is a database error.
     */
    public int getScale(int column) throws TSQLException;

    /**
     * Returns the name of the indexed columns schema.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the name of the columns schema.
     * @throws TSQLException
     *             if there is a database error.
     */
    public String getSchemaName(int column) throws TSQLException;

    /**
     * Returns the title of the indexed columns table.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return the table title.
     * @throws TSQLException
     *             if there is a database error.
     */
    public String getTableName(int column) throws TSQLException;

    /**
     * Returns an indication of whether the indexed column is automatically
     * incremented and is therefore read-only.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if it is automatically numbered, {@code false}
     *         otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isAutoIncrement(int column) throws TSQLException;

    /**
     * Returns an indication of whether the case of the indexed column is
     * important.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if case matters, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isCaseSensitive(int column) throws TSQLException;

    /**
     * Returns whether the indexed column contains a monetary amount.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if it is a monetary value, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isCurrency(int column) throws TSQLException;

    /**
     * Returns an indication of whether writing to the indexed column is
     * guaranteed to be successful.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if the write is guaranteed, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isDefinitelyWritable(int column) throws TSQLException;

    /**
     * Returns whether the indexed column is nullable.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if it is nullable, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public int isNullable(int column) throws TSQLException;

    /**
     * Returns an indication of whether writing to the indexed column is
     * guaranteed to be unsuccessful.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if the column is read-only, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isReadOnly(int column) throws TSQLException;

    /**
     * Returns an indication of whether the indexed column is searchable.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if the indexed column is searchable, {@code false}
     *         otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isSearchable(int column) throws TSQLException;

    /**
     * Returns an indication of whether the values contained in the indexed
     * column are signed.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if they are signed, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isSigned(int column) throws TSQLException;

    /**
     * Returns an indication of whether writing to the indexed column is
     * possible.
     * 
     * @param column
     *            the column index, starting at 1.
     * @return {@code true} if it is possible to write, {@code false} otherwise.
     * @throws TSQLException
     *             if there is a database error.
     */
    public boolean isWritable(int column) throws TSQLException;
}
