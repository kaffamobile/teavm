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


import org.apache.harmony.luni.internal.nls.Messages;

/**
 * A specialized {@link TOutputStream} that writes to a file in the file system.
 * All write requests made by calling methods in this class are directly
 * forwarded to the equivalent function of the underlying operating system.
 * Since this may induce some performance penalty, in particular if many small
 * write requests are made, a FileOutputStream is often wrapped by a
 * BufferedOutputStream.
 * 
 */
public class TFileOutputStream extends TOutputStream implements TCloseable {

    /**
     * The FileDescriptor representing this FileOutputStream.
     */
    TFileDescriptor fd;

    boolean innerFD;

    private TIFileSystem fileSystem = TIFileSystem.FILE_SYSTEM;

    /**
     * Constructs a new FileOutputStream on the File {@code file}. If the file
     * exists, it is overwritten.
     * 
     * @param file
     *            the file to which this stream writes.
     * @throws TFileNotFoundException
     *             if {@code file} cannot be opened for writing.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     * @see java.lang.SecurityManager#checkWrite(TFileDescriptor)
     */
    public TFileOutputStream(TFile file) throws TFileNotFoundException {
        this(file, false);
    }

    /**
     * Constructs a new FileOutputStream on the File {@code file}. The
     * parameter {@code append} determines whether or not the file is opened and
     * appended to or just opened and overwritten.
     * 
     * @param file
     *            the file to which this stream writes.
     * @param append
     *            indicates whether or not to append to an existing file.
     * @throws TFileNotFoundException
     *             if the {@code file} cannot be opened for writing.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     * @see java.lang.SecurityManager#checkWrite(TFileDescriptor)
     * @see java.lang.SecurityManager#checkWrite(String)
     */
    public TFileOutputStream(TFile file, boolean append)
            throws TFileNotFoundException {
        super();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(file.getPath());
        }
        fd = new TFileDescriptor();
        fd.filename = file.properPath(true);
        fileSystem.open(fd,
                append ? TIFileSystem.O_APPEND : TIFileSystem.O_WRONLY);
        innerFD = true;
    }

    /**
     * Constructs a new FileOutputStream on the FileDescriptor {@code fd}. The
     * file must already be open, therefore no {@code FileNotFoundException}
     * will be thrown.
     * 
     * @param fd
     *            the FileDescriptor to which this stream writes.
     * @throws NullPointerException
     *             if {@code fd} is {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     * @see java.lang.SecurityManager#checkWrite(TFileDescriptor)
     */
    public TFileOutputStream(TFileDescriptor fd) {
        super();
        if (fd == null) {
            throw new NullPointerException(Messages.getString("luni.B6")); //$NON-NLS-1$
        }
        this.fd = fd;
        innerFD = false;
    }

    /**
     * Constructs a new FileOutputStream on the file named {@code filename}. If
     * the file exists, it is overwritten. The {@code filename} may be absolute
     * or relative to the system property {@code "user.dir"}.
     * 
     * @param filename
     *            the name of the file to which this stream writes.
     * @throws TFileNotFoundException
     *             if the file cannot be opened for writing.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     */
    public TFileOutputStream(String filename) throws TFileNotFoundException {
        this(filename, false);
    }

    /**
     * Constructs a new FileOutputStream on the file named {@code filename}.
     * The parameter {@code append} determines whether or not the file is opened
     * and appended to or just opened and overwritten. The {@code filename} may
     * be absolute or relative to the system property {@code "user.dir"}.
     * 
     * @param filename
     *            the name of the file to which this stream writes.
     * @param append
     *            indicates whether or not to append to an existing file.
     * @throws TFileNotFoundException
     *             if the file cannot be opened for writing.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     */
    public TFileOutputStream(String filename, boolean append)
            throws TFileNotFoundException {
        this(new TFile(filename), append);
    }

    /**
     * Closes this stream. This implementation closes the underlying operating
     * system resources allocated to represent this stream.
     * 
     * @throws TIOException
     *             if an error occurs attempting to close this stream.
     */
    @Override
    public void close() throws TIOException {
        if (fd == null) {
            // if fd is null, then the underlying file is not opened, so nothing
            // to close
            return;
        }

        synchronized (this) {
            if (fd.descriptor != null && innerFD) {
                fileSystem.close(fd);
            }
        }
    }

    /**
     * Frees any resources allocated for this stream before it is garbage
     * collected. This method is called from the Java Virtual Machine.
     * 
     * @throws TIOException
     *             if an error occurs attempting to finalize this stream.
     */
    @Override
    protected void finalize() throws TIOException {
        close();
    }

    /**
     * Returns a FileDescriptor which represents the lowest level representation
     * of an operating system stream resource.
     * 
     * @return a FileDescriptor representing this stream.
     * @throws TIOException
     *             if an error occurs attempting to get the FileDescriptor of
     *             this stream.
     */
    public final TFileDescriptor getFD() throws TIOException {
        return fd;
    }

    /**
     * Writes the entire contents of the byte array {@code buffer} to this
     * stream.
     * 
     * @param buffer
     *            the buffer to be written to the file.
     * @throws TIOException
     *             if this stream is closed or an error occurs attempting to
     *             write to this stream.
     */
    @Override
    public void write(byte[] buffer) throws TIOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * {@code offset} to this stream.
     * 
     * @param buffer
     *            the buffer to write to this stream.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes from {@code buffer} to write.
     * @throws IndexOutOfBoundsException
     *             if {@code count < 0} or {@code offset < 0}, or if
     *             {@code count + offset} is greater than the length of
     *             {@code buffer}.
     * @throws TIOException
     *             if this stream is closed or an error occurs attempting to
     *             write to this stream.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     */
    @Override
    public void write(byte[] buffer, int offset, int count) throws TIOException {
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (count < 0 || offset < 0 || offset > buffer.length
                || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        if (count == 0) {
            return;
        }

        openCheck();
        fileSystem.write(fd, buffer, offset, count);
    }

    /**
     * Writes the specified byte {@code oneByte} to this stream. Only the low
     * order byte of the integer {@code oneByte} is written.
     * 
     * @param oneByte
     *            the byte to be written.
     * @throws TIOException
     *             if this stream is closed an error occurs attempting to write
     *             to this stream.
     */
    @Override
    public void write(int oneByte) throws TIOException {
        openCheck();
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) oneByte;
        fileSystem.write(fd, byteArray, 0, 1);
    }

    private synchronized void openCheck() throws TIOException {
        if (fd.descriptor == null) {
            throw new TIOException();
        }
    }
}
