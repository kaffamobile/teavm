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

import java.io.BufferedInputStream;

import org.apache.harmony.luni.internal.nls.Messages;
import org.teavm.classlib.java.lang.TString;

/**
 * A specialized {@link InputStream} that reads from a file in the file system.
 * All read requests made by calling methods in this class are directly
 * forwarded to the equivalent function of the underlying operating system.
 * Since this may induce some performance penalty, in particular if many small
 * read requests are made, a FileInputStream is often wrapped by a
 * BufferedInputStream.
 * 
 * @see BufferedInputStream
 * @see TFileOutputStream
 */
public class TFileInputStream extends TInputStream implements TCloseable {
    /**
     * The {@link TFileDescriptor} representing this {@code FileInputStream}.
     */
    TFileDescriptor fd;

    boolean innerFD;

    private TIFileSystem fileSystem = TIFileSystem.FILE_SYSTEM;

    private static class RepositioningLock {
    }

    private Object repositioningLock = new RepositioningLock();

    /**
     * Constructs a new {@code FileInputStream} based on {@code file}.
     * 
     * @param file
     *            the file from which this stream reads.
     * @throws TFileNotFoundException
     *             if {@code file} does not exist.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     */
    public TFileInputStream(TFile file) throws TFileNotFoundException {
        super();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            // For compatibility, nulls are passed to the manager.
            String filePath = (null == file ? null : file.getPath());
            security.checkRead(filePath);
        }
        if (file == null) {
            // luni.4D=Argument must not be null
            throw new NullPointerException(Messages.getString("luni.4D")); //$NON-NLS-1$
        }
        fd = new TFileDescriptor();
        fd.filename = file.properPath(true);
        fileSystem.open(fd, TIFileSystem.O_RDONLY);
        fd.readOnly = true;
        		
        innerFD = true;
    }

    /**
     * Constructs a new {@code FileInputStream} on the {@link TFileDescriptor}
     * {@code fd}. The file must already be open, therefore no
     * {@code FileNotFoundException} will be thrown.
     * 
     * @param fd
     *            the FileDescriptor from which this stream reads.
     * @throws NullPointerException
     *             if {@code fd} is {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     */
    public TFileInputStream(TFileDescriptor fd) {
        super();
        if (fd == null) {
            throw new NullPointerException();
        }
        this.fd = fd;
        innerFD = false;
    }

    /**
     * Constructs a new {@code FileInputStream} on the file named
     * {@code fileName}. The path of {@code fileName} may be absolute or
     * relative to the system property {@code "user.dir"}.
     * 
     * @param fileName
     *            the path and name of the file from which this stream reads.
     * @throws TFileNotFoundException
     *             if there is no file named {@code fileName}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     */
    public TFileInputStream(String fileName) throws TFileNotFoundException {
        this(null == fileName ? (TFile) null : new TFile(fileName));
    }

    /**
     * Returns the number of bytes that are available before this stream will
     * block. This method always returns the size of the file minus the current
     * position.
     * 
     * @return the number of bytes available before blocking.
     * @throws TIOException
     *             if an error occurs in this stream.
     */
    @Override
    public int available() throws TIOException {
        openCheck();
        synchronized (repositioningLock) {
            // stdin requires special handling
            if (fd == TFileDescriptor.in) {
                return (int) fileSystem.ttyAvailable();
            }
            return (int) fileSystem.available(fd);
        }
    }

    /**
     * Closes this stream.
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
            if (fd.valid() && innerFD) {
                fileSystem.close(fd);
            }
        }
    }

    /**
     * Ensures that all resources for this stream are released when it is about
     * to be garbage collected.
     * 
     * @throws TIOException
     *             if an error occurs attempting to finalize this stream.
     */
    @Override
    protected void finalize() throws TIOException {
        close();
    }

    /**
     * Returns the {@link TFileDescriptor} representing the operating system
     * resource for this stream.
     * 
     * @return the {@code FileDescriptor} for this stream.
     * @throws TIOException
     *             if an error occurs while getting this stream's
     *             {@code FileDescriptor}.
     */
    public final TFileDescriptor getFD() throws TIOException {
        return fd;
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of this stream has been
     * reached.
     * 
     * @return the byte read or -1 if the end of this stream has been reached.
     * @throws TIOException
     *             if this stream is closed or another I/O error occurs.
     */
    @Override
    public int read() throws TIOException {
        byte[] readed = new byte[1];
        int result = read(readed, 0, 1);
        return result == -1 ? -1 : readed[0] & 0xff;
    }

    /**
     * Reads bytes from this stream and stores them in the byte array
     * {@code buffer}.
     * 
     * @param buffer
     *            the byte array in which to store the bytes read.
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws TIOException
     *             if this stream is closed or another I/O error occurs.
     */
    @Override
    public int read(byte[] buffer) throws TIOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads at most {@code count} bytes from this stream and stores them in the
     * byte array {@code buffer} starting at {@code offset}.
     * 
     * @param buffer
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes read
     *            from this stream.
     * @param count
     *            the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is greater than the size of
     *             {@code buffer}.
     * @throws TIOException
     *             if the stream is closed or another TIOException occurs.
     */
    @Override
    public int read(byte[] buffer, int offset, int count) throws TIOException {
        if (count > buffer.length - offset || count < 0 || offset < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (0 == count) {
            return 0;
        }
        openCheck();
        synchronized (repositioningLock) {
            // stdin requires special handling
            if (fd == TFileDescriptor.in) {
                return (int) fileSystem.ttyRead(buffer, offset, count);
            }
            return (int) fileSystem.read(fd, buffer, offset, count);
        }
    }

    /**
     * Skips {@code count} number of bytes in this stream. Subsequent
     * {@code read()}'s will not return these bytes unless {@code reset()} is
     * used. This method may perform multiple reads to read {@code count} bytes.
     * 
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws TIOException
     *             if {@code count < 0}, this stream is closed or another
     *             TIOException occurs.
     */
    @Override
    public long skip(long count) throws TIOException {
        openCheck();

        if (count == 0) {
            return 0;
        }
        if (count < 0) {
            // luni.AC=Number of bytes to skip cannot be negative
            throw new TIOException(TString.wrap("Number of bytes to skip cannot be negative")); //$NON-NLS-1$
        }

        // stdin requires special handling
        if (fd == TFileDescriptor.in) {
            // Read and discard count bytes in 8k chunks
            long skipped = 0, numRead;
            int chunk = count < 8192 ? (int) count : 8192;
            byte[] buffer = new byte[chunk];
            for (long i = count / chunk; i >= 0; i--) {
                numRead = fileSystem.ttyRead(buffer, 0, chunk);
                skipped += numRead;
                if (numRead < chunk) {
                    return skipped;
                }
            }
            return skipped;
        }

        synchronized (repositioningLock) {
            final int currentPosition = fileSystem.seek(fd, 0,
                    TIFileSystem.SEEK_CUR);
            final int newPosition = fileSystem.seek(fd,
                    currentPosition + (int) count, TIFileSystem.SEEK_SET);
            return newPosition - currentPosition;
        }
    }

    private synchronized void openCheck() throws TIOException {
        if (!fd.valid()) {
            throw new TIOException();
        }
    }
}
