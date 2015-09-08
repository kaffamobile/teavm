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

package org.teavm.classlib.java.io;

import org.apache.harmony.luni.platform.JSFileStream;


/**
 * The lowest-level representation of a file, device, or
 * socket. If is often used for wrapping an operating system "handle". Some
 * I/O classes can be queried for the FileDescriptor they are operating on, and
 * this descriptor can subsequently be used during the instantiation of another
 * I/O class, so that the new object will reuse it.
 * <p>
 * The FileDescriptor class also contains static fields representing the
 * system's standard input, output and error streams. These can be used directly
 * if desired, but it is recommended to go through System.in, System.out, and
 * System.err streams respectively.
 * <p>
 * Applications should not create new FileDescriptors.
 * 
 */
public final class TFileDescriptor {

    /**
     * The FileDescriptor representing standard input.
     */
    public static final TFileDescriptor in = new TFileDescriptor();

    /**
     * FileDescriptor representing standard out.
     */
    public static final TFileDescriptor out = new TFileDescriptor();

    /**
     * FileDescriptor representing standard error.
     */
    public static final TFileDescriptor err = new TFileDescriptor();

    /**
     * Represents a link to any underlying OS resources for this FileDescriptor.
     * A value of -1 indicates that this FileDescriptor is invalid.
     */
    public JSFileStream descriptor = null;
    
    boolean readOnly = false;

    public String filename; 

//    private static native void oneTimeInitialization();

    static {
//        in.descriptor = 0;
//        out.descriptor = 1;
//        err.descriptor = 2;

//        oneTimeInitialization();
    }

    /**
     * Constructs a new FileDescriptor containing an invalid handle. The
     * contained handle is usually modified by native code at a later point.
     */
    public TFileDescriptor() {
        super();
    }

    /**
     * Ensures that data which is buffered within the underlying implementation
     * is written out to the appropriate device before returning.
     * 
     * @throws TSyncFailedException
     *             when the operation fails.
     */
    public void sync() throws TSyncFailedException {
        // if the descriptor is a read-only one, do nothing
        if (!readOnly) {
            syncImpl();
        }
    }
    
    private native void syncImpl() throws TSyncFailedException;

    /**
     * Indicates whether this FileDescriptor is valid.
     * 
     * @return {@code true} if this FileDescriptor is valid, {@code false}
     *         otherwise.
     */
    public boolean valid() {
        return descriptor != null;
    }
}
