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




/**
 * TODO Type description
 * 
 */
public interface TIFileSystem {

    public final int SHARED_LOCK_TYPE = 1;

    public final int EXCLUSIVE_LOCK_TYPE = 2;

    public final int SEEK_SET = 1;

    public final int SEEK_CUR = 2;

    public final int SEEK_END = 4;

    public final int O_RDONLY = 0x00000000;

    public final int O_WRONLY = 0x00000001;

    public final int O_RDWR = 0x00000010;

    public final int O_RDWRSYNC = 0x00000020;
    
    public final int O_APPEND = 0x00000100;

    public final int O_CREAT = 0x00001000;

    public final int O_EXCL = 0x00010000;

    public final int O_NOCTTY = 0x00100000;

    public final int O_NONBLOCK = 0x01000000;

    public final int O_TRUNC = 0x10000000;

    public int read(TFileDescriptor fileDescriptor, byte[] bytes, int offset, int length)
            throws TIOException;

    public int write(TFileDescriptor fileDescriptor, byte[] bytes, int offset, int length)
            throws TIOException;

    public int readv(TFileDescriptor fileDescriptor, int[] addresses, int[] offsets,
            int[] lengths, int size) throws TIOException;

    public int writev(TFileDescriptor fileDescriptor, Object[] buffers, int[] offsets,
            int[] lengths, int size) throws TIOException;

    // Required to support direct byte buffers
    public int readDirect(TFileDescriptor fileDescriptor, int address, int offset,
            int length) throws TIOException;

    public int writeDirect(TFileDescriptor fileDescriptor, int address, int offset,
            int length) throws TIOException;

    public boolean lock(TFileDescriptor fileDescriptor, int start, int length, int type,
            boolean waitFlag) throws TIOException;

    public void unlock(TFileDescriptor fileDescriptor, int start, int length)
            throws TIOException;

    public int seek(TFileDescriptor fileDescriptor, int offset, int whence)
            throws TIOException;

    public void fflush(TFileDescriptor fileDescriptor, boolean metadata)
            throws TIOException;

    public void close(TFileDescriptor fileDescriptor) throws TIOException;

    public void truncate(TFileDescriptor fileDescriptor, int size) throws TIOException;

    /**
     * Returns the granularity for virtual memory allocation.
     */
    public int getAllocGranularity() throws TIOException;

    public void open(TFileDescriptor fileDescriptor, int mode) throws TFileNotFoundException;

    public int transfer(int fileHandler, TFileDescriptor socketDescriptor,
            int offset, int count) throws TIOException;

    public int ttyAvailable() throws TIOException;

    public int available(TFileDescriptor fileDescriptor) throws TIOException;

    public int size(TFileDescriptor fileDescriptor) throws TIOException;
    
    public int ttyRead(byte[] bytes, int offset, int length) throws TIOException;
    
    static final TIFileSystem FILE_SYSTEM = null;
}
