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

import java.util.Map;

/**
 * A Java representation of the SQL {@code ARRAY} type.
 */
public interface TArray {

    /**
     * Retrieves the contents of the SQL {@code ARRAY} value as a Java array
     * object.
     * 
     * @return A Java array containing the elements of this Array
     * @throws TSQLException
     *             if there is a database error.
     */
    public Object getArray() throws TSQLException;

    /**
     * Returns part of the SQL {@code ARRAY} associated with this array,
     * starting at a particular {@code index} and comprising up to {@code count}
     * successive elements of the SQL array.
     * 
     * @param index
     *            the start position in the array where the values are
     *            retrieved.
     * @param count
     *            the number of elements to retrieve.
     * @return A Java array containing the desired set of elements from this Array
     * @throws TSQLException
     *             if there is a database error.
     */
    public Object getArray(long index, int count) throws TSQLException;

    /**
     * Returns part of the SQL {@code ARRAY} associated with this array,
     * starting at a particular {@code index} and comprising up to {@code count}
     * successive elements of the SQL array.
     * 
     * @param index
     *            the start position in the array where the values are
     *            retrieved.
     * @param count
     *            the number of elements to retrieve.
     * @param map
     *            the map defining the correspondence between SQL type names 
     *            and Java types.
     * @return A Java array containing the desired set of elements from this Array
     * @throws TSQLException
     *             if there is a database error.
     */
    public Object getArray(long index, int count, Map<String, Class<?>> map)
            throws TSQLException;

    /**
     * Returns the data from the underlying SQL {@code ARRAY} as a Java array.
     * 
     * @param map
     *            the map defining the correspondence between SQL type names 
     *            and Java types.
     * @return A Java array containing the elements of this array
     * @throws TSQLException
     *             if there is a database error.
     */
    public Object getArray(Map<String, Class<?>> map) throws TSQLException;

    /**
     * Returns the JDBC type of the entries in this array's underlying 
     * SQL array.
     * 
     * @return An integer constant from the {@code java.sql.Types} class
     * @throws TSQLException
     *             if there is a database error.
     */
    public int getBaseType() throws TSQLException;

    /**
     * Returns the SQL type name of the entries in this array's underlying 
     * SQL array.
     * 
     * @return The database specific name or a fully-qualified SQL type name.
     * @throws TSQLException
     *              if there is a database error.
     */
    public String getBaseTypeName() throws TSQLException;

    /**
     * Returns a ResultSet object which holds the entries of the SQL {@code
     * ARRAY} associated with this array.
     * 
     * @return the elements of the array as a {@code ResultSet}.
     * @throws TSQLException
     *             if there is a database error.
     */
    public TResultSet getResultSet() throws TSQLException;

    /**
     * Returns a {@code ResultSet} object that holds the entries of a subarray,
     * beginning at a particular index and comprising up to {@code count} 
     * successive entries.
     * 
     * @param index
     *            the start position in the array where the values are
     *            retrieved.
     * @param count
     *            the number of elements to retrieve.
     * @return the elements of the array as a {@code ResultSet}.
     * @throws TSQLException
     *             if there is a database error.
     */
    public TResultSet getResultSet(long index, int count) throws TSQLException;

    /**
     * Returns a {@code ResultSet} object that holds the entries of a subarray,
     * beginning at a particular index and comprising up to {@code count} 
     * successive entries.
     * 
     * @param index
     *            the start position in the array where the values are
     *            retrieved.
     * @param count
     *            the number of elements to retrieve.
     * @param map
     *            the map defining the correspondence between SQL type names 
     *            and Java types.
     * @return the {@code ResultSet} the array's custom type values. if a
     *         database error has occurred.
     * @throws TSQLException
     *             if there is a database error.
     */
    public TResultSet getResultSet(long index, int count,
            Map<String, Class<?>> map) throws TSQLException;

    /**
     * Returns a {@code ResultSet} object which holds the entries of the SQL
     * {@code ARRAY} associated with this array.
     * 
     * @param map
     *            the map defining the correspondence between SQL type names 
     *            and Java types.
     * @return the array as a {@code ResultSet}.
     * @throws TSQLException
     *             if there is a database error.
     */
    public TResultSet getResultSet(Map<String, Class<?>> map)
            throws TSQLException;

}
