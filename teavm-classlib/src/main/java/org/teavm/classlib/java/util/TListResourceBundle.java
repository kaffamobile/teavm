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

package org.teavm.classlib.java.util;

import java.util.Enumeration;

/**
 * {@code ListResourceBundle} is the abstract superclass of classes which provide
 * resources by implementing the {@code getContents()} method to return
 * the list of resources.
 * 
 * @see TResourceBundle
 * @since 1.1
 */
public abstract class TListResourceBundle extends TResourceBundle {
    THashMap<String, Object> table;

    /**
     * Constructs a new instance of this class.
     */
    public TListResourceBundle() {
        super();
    }

    /**
     * Returns an {@code Object} array which contains the resources of this
     * {@code ListResourceBundle}. Each element in the array is an array of two
     * elements, the first is the resource key string and the second is the
     * resource.
     * 
     * @return a {@code Object} array containing the resources.
     */
    protected abstract Object[][] getContents();

    /**
     * Returns the names of the resources contained in this {@code ListResourceBundle}.
     * 
     * @return an {@code Enumeration} of the resource names.
     */
    @Override
    public Enumeration<String> getKeys() {
        initializeTable();
        if (parent != null) {
            return new Enumeration<String>() {
                TIterator<String> local = table.keySet().iterator();

                Enumeration<String> pEnum = parent.getKeys();

                String nextElement;

                private boolean findNext() {
                    if (nextElement != null) {
                        return true;
                    }
                    while (pEnum.hasMoreElements()) {
                        String next = pEnum.nextElement();
                        if (!table.containsKey(next)) {
                            nextElement = next;
                            return true;
                        }
                    }
                    return false;
                }

                public boolean hasMoreElements() {
                    if (local.hasNext()) {
                        return true;
                    }
                    return findNext();
                }

                public String nextElement() {
                    if (local.hasNext()) {
                        return local.next();
                    }
                    if (findNext()) {
                        String result = nextElement;
                        nextElement = null;
                        return result;
                    }
                    // Cause an exception
                    return pEnum.nextElement();
                }
            };
        } else {
            return new Enumeration<String>() {
                TIterator<String> it = table.keySet().iterator();

                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                public String nextElement() {
                    return it.next();
                }
            };
        }
    }

    @Override
    public final Object handleGetObject(String key) {
        initializeTable();
        if (key == null) {
            throw new NullPointerException();
        }
        return table.get(key);
    }

    private synchronized void initializeTable() {
        if (table == null) {
            Object[][] contents = getContents();
            table = new THashMap<String, Object>(contents.length / 3 * 4 + 3);
            for (Object[] content : contents) {
                if (content[0] == null || content[1] == null) {
                    throw new NullPointerException();
                }
                table.put((String) content[0], content[1]);
            }
        }
    }
}