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

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.luni.internal.io.FileCanonPathCache;
import org.apache.harmony.luni.internal.nls.Messages;
import org.apache.harmony.luni.platform.JSFileStat;
import org.apache.harmony.luni.platform.JSFileStream;
import org.apache.harmony.luni.util.PriviAction;
import org.teavm.classlib.java.net.TURL;
import org.teavm.classlib.java.security.TSecureRandom;
import org.teavm.util.JSDate;

/**
 * An "abstract" representation of a file system entity identified by a
 * pathname. The pathname may be absolute (relative to the root directory
 * of the file system) or relative to the current directory in which the program
 * is running.
 * <p>
 * This class provides methods for querying/changing information about the file
 * as well as directory listing capabilities if the file represents a directory.
 * <p>
 * When manipulating file paths, the static fields of this class may be used to
 * determine the platform specific separators.
 * 
 * @see java.io.Serializable
 * @see java.lang.Comparable
 */
public class TFile implements Serializable, Comparable<TFile> {

    private static final long serialVersionUID = 301077366599181567L;

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String path;

    transient String properPath;

    /**
     * The system dependent file separator character.
     */
    public static final char separatorChar;

    /**
     * The system dependent file separator string. The initial value of this
     * field is the system property "file.separator".
     */
    public static final String separator;

    /**
     * The system dependent path separator character.
     */
    public static final char pathSeparatorChar;

    /**
     * The system dependent path separator string. The initial value of this
     * field is the system property "path.separator".
     */
    public static final String pathSeparator;

    /* Temp file counter */
    private static int counter = 0;

    /* identify for differnt VM processes */
    private static int counterBase = 0;

    private static class TempFileLocker {};

    private static TempFileLocker tempFileLocker = new TempFileLocker();

    private static boolean caseSensitive;


    static {

        // The default protection domain grants access to these properties
        separatorChar = System.getProperty("file.separator", "/").charAt(0); //$NON-NLS-1$ //$NON-NLS-2$
        pathSeparatorChar = System.getProperty("path.separator", ":").charAt(0); //$NON-NLS-1$//$NON-NLS-2$
        separator = new String(new char[] { separatorChar }, 0, 1);
        pathSeparator = new String(new char[] { pathSeparatorChar }, 0, 1);
        caseSensitive = isCaseSensitiveImpl();
    }

    /**
     * Constructs a new file using the specified directory and name.
     * 
     * @param dir
     *            the directory where the file is stored.
     * @param name
     *            the file's name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    public TFile(TFile dir, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dir == null) {
            this.path = fixSlashes(name);
        } else {
            this.path = calculatePath(dir.getPath(), name);
        }
    }

    /**
     * Constructs a new file using the specified path.
     * 
     * @param path
     *            the path to be used for the file.
     */
    public TFile(String path) {
        // path == null check & NullPointerException thrown by fixSlashes
        this.path = fixSlashes(path);
    }

    /**
     * Constructs a new File using the specified directory path and file name,
     * placing a path separator between the two.
     * 
     * @param dirPath
     *            the path to the directory where the file is stored.
     * @param name
     *            the file's name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    public TFile(String dirPath, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dirPath == null) {
            this.path = fixSlashes(name);
        } else {
            this.path = calculatePath(dirPath, name);
        }
    }

    /**
     * Constructs a new File using the path of the specified URI. {@code uri}
     * needs to be an absolute and hierarchical Unified Resource Identifier with
     * file scheme and non-empty path component, but with undefined authority,
     * query or fragment components.
     * 
     * @param uri
     *            the Unified Resource Identifier that is used to construct this
     *            file.
     * @throws IllegalArgumentException
     *             if {@code uri} does not comply with the conditions above.
     * @see #toURI
     * @see java.net.URI
     */
    public TFile(URI uri) {
        // check pre-conditions
        checkURI(uri);
        this.path = fixSlashes(uri.getPath());
    }

    private String calculatePath(String dirPath, String name) {
        dirPath = fixSlashes(dirPath);
        if (!name.equals(EMPTY_STRING) || dirPath.equals(EMPTY_STRING)) {
            // Remove all the proceeding separator chars from name
            name = fixSlashes(name);

            int separatorIndex = 0;
            while ((separatorIndex < name.length())
                    && (name.charAt(separatorIndex) == separatorChar)) {
                separatorIndex++;
            }
            if (separatorIndex > 0) {
                name = name.substring(separatorIndex, name.length());
            }

            // Ensure there is a separator char between dirPath and name
            if (dirPath.length() > 0
                    && (dirPath.charAt(dirPath.length() - 1) == separatorChar)) {
                return dirPath + name;
            }
            return dirPath + separatorChar + name;
        }

        return dirPath;
    }

    @SuppressWarnings("nls")
    private void checkURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(Messages.getString("luni.AD", uri));
        } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
            throw new IllegalArgumentException(Messages.getString("luni.AE", uri));
        }

        String temp = uri.getScheme();
        if (temp == null || !temp.equals("file")) {
            throw new IllegalArgumentException(Messages.getString("luni.AF", uri));
        }

        temp = uri.getRawPath();
        if (temp == null || temp.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("luni.B0", uri));
        }

        if (uri.getRawAuthority() != null) {
            throw new IllegalArgumentException(Messages.getString("luni.B1",
                    new String[] { "authority", uri.toString() }));
        }

        if (uri.getRawQuery() != null) {
            throw new IllegalArgumentException(Messages.getString("luni.B1",
                    new String[] { "query", uri.toString() }));
        }

        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException(Messages.getString("luni.B1",
                    new String[] { "fragment", uri.toString() }));
        }
    }

    /**
     * Lists the file system roots. The Java platform may support zero or more
     * file systems, each with its own platform-dependent root. Further, the
     * canonical pathname of any file on the system will always begin with one
     * of the returned file system roots.
     * 
     * @return the array of file system roots.
     */
    public static TFile[] listRoots() {
    	String[] rootsList = rootsImpl();
        if (rootsList == null) {
            return new TFile[0];
        }
        TFile result[] = new TFile[rootsList.length];
        for (int i = 0; i < rootsList.length; i++) {
            result[i] = new TFile(rootsList[i]);
        }
        return result;
    }

    /**
     * The purpose of this method is to take a path and fix the slashes up. This
     * includes changing them all to the current platforms fileSeparator and
     * removing duplicates.
     */
    private String fixSlashes(String origPath) {
        int uncIndex = 1;
        int length = origPath.length(), newLength = 0;
        if (separatorChar == '/') {
            uncIndex = 0;
        } else if (length > 2 && origPath.charAt(1) == ':') {
            uncIndex = 2;
        }

        boolean foundSlash = false;
        char newPath[] = origPath.toCharArray();
        for (int i = 0; i < length; i++) {
            char pathChar = newPath[i];
            if ((separatorChar == '\\' && pathChar == '\\')
                || pathChar == '/') {
                /* UNC Name requires 2 leading slashes */
                if ((foundSlash && i == uncIndex) || !foundSlash) {
                    newPath[newLength++] = separatorChar;
                    foundSlash = true;
                }
            } else {
                // check for leading slashes before a drive
                if (pathChar == ':'
                        && uncIndex > 0
                        && (newLength == 2 || (newLength == 3 && newPath[1] == separatorChar))
                        && newPath[0] == separatorChar) {
                    newPath[0] = newPath[newLength - 1];
                    newLength = 1;
                    // allow trailing slash after drive letter
                    uncIndex = 2;
                }
                newPath[newLength++] = pathChar;
                foundSlash = false;
            }
        }
        // remove trailing slash
        if (foundSlash
                && (newLength > (uncIndex + 1) || (newLength == 2 && newPath[0] != separatorChar))) {
            newLength--;
        }

        return new String(newPath, 0, newLength);
    }

    /**
     * Indicates whether the current context is allowed to read from this file.
     * 
     * @return {@code true} if this file can be read, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     */
    public boolean canRead() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        String pp = properPath(true);
        return existsImpl(pp) && !isWriteOnlyImpl(pp);
    }

    /**
     * Indicates whether the current context is allowed to write to this file.
     * 
     * @return {@code true} if this file can be written, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     */
    public boolean canWrite() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }

        // Cannot use exists() since that does an unwanted read-check.
        boolean exists = false;
        if (path.length() > 0) {
            exists = existsImpl(properPath(true));
        }
        return exists && !isReadOnlyImpl(properPath(true));
    }

    /**
     * Returns the relative sort ordering of the paths for this file and the
     * file {@code another}. The ordering is platform dependent.
     * 
     * @param another
     *            a file to compare this file to
     * @return an int determined by comparing the two paths. Possible values are
     *         described in the Comparable interface.
     * @see Comparable
     */
    public int compareTo(TFile another) {
        if (caseSensitive) {
            return this.getPath().compareTo(another.getPath());
        }
        return this.getPath().compareToIgnoreCase(another.getPath());
    }

    /**
     * Deletes this file. Directories must be empty before they will be deleted.
     * 
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     * @see java.lang.SecurityManager#checkDelete
     */
    public boolean delete() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        String propPath = properPath(true);
        if ((path.length() != 0) && isDirectoryImpl(propPath)) {
            return deleteDirImpl(propPath);
        }
        return deleteFileImpl(propPath);
    }


    /**
     * Schedules this file to be automatically deleted once the virtual machine
     * terminates. This will only happen when the virtual machine terminates
     * normally as described by the Java Language Specification section 12.9.
     * 
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     */
    public void deleteOnExit() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }

        //DeleteOnExit.addFile(Util.toUTF8String(properPath(true)));
    }

    /**
     * Compares {@code obj} to this file and returns {@code true} if they
     * represent the <em>same</em> object using a path specific comparison.
     * 
     * @param obj
     *            the object to compare this file with.
     * @return {@code true} if {@code obj} is the same as this object,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TFile)) {
            return false;
        }
        if (!caseSensitive) {
            return path.equalsIgnoreCase(((TFile) obj).getPath());
        }
        return path.equals(((TFile) obj).getPath());
    }

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     * 
     * @return {@code true} if this file exists, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public boolean exists() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return existsImpl(properPath(true));
    }

    /**
     * Returns the absolute path of this file.
     * 
     * @return the absolute file path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public String getAbsolutePath() {
    	String absolute = properPath(false);
        return absolute;
    }

    /**
     * Returns a new file constructed using the absolute path of this file.
     * 
     * @return a new file from this file's absolute path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public TFile getAbsoluteFile() {
        return new TFile(this.getAbsolutePath());
    }

    /**
     * Returns the absolute path of this file with all references resolved. An
     * <em>absolute</em> path is one that begins at the root of the file
     * system. The canonical path is one in which all references have been
     * resolved. For the cases of '..' and '.', where the file system supports
     * parent and working directory respectively, these are removed and replaced
     * with a direct directory reference. If the file does not exist,
     * getCanonicalPath() may not resolve any references and simply returns an
     * absolute path name or throws an IOException.
     * 
     * @return the canonical path of this file.
     * @throws IOException
     *             if an I/O error occurs.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public String getCanonicalPath() throws IOException {
    	String result = properPath(false);
        String absPath = result;
        String canonPath = FileCanonPathCache.get(absPath);

        if (canonPath != null) {
            return canonPath;
        }
        if(separatorChar == '/') {
            // resolve the full path first
            result = resolveLink(result, result.length(), false);
            // resolve the parent directories
            result = resolve(result);
        }
        int numSeparators = 1;
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == separatorChar) {
                numSeparators++;
            }
        }
        int sepLocations[] = new int[numSeparators];
        int rootLoc = 0;
        if (separatorChar != '/') {
            if (result.charAt(0) == '\\') {
                rootLoc = (result.length() > 1 && result.charAt(1) == '\\') ? 1 : 0;
            } else {
                rootLoc = 2; // skip drive i.e. c:
            }
        }
        char[] newResult = new char[result.length()];
        int newLength = 0, lastSlash = 0, foundDots = 0;
        sepLocations[lastSlash] = rootLoc;
        for (int i = 0; i <= result.length(); i++) {
            if (i < rootLoc) {
                newResult[newLength++] = result.charAt(i);
            } else {
                if (i == result.length() || result.charAt(i) == separatorChar) {
                    if (i == result.length() && foundDots == 0) {
                        break;
                    }
                    if (foundDots == 1) {
                        /* Don't write anything, just reset and continue */
                        foundDots = 0;
                        continue;
                    }
                    if (foundDots > 1) {
                        /* Go back N levels */
                        lastSlash = lastSlash > (foundDots - 1) ? lastSlash
                                - (foundDots - 1) : 0;
                        newLength = sepLocations[lastSlash] + 1;
                        foundDots = 0;
                        continue;
                    }
                    sepLocations[++lastSlash] = newLength;
                    newResult[newLength++] = separatorChar;
                    continue;
                }
                if (result.charAt(i) == '.') {
                    foundDots++;
                    continue;
                }
                /* Found some dots within text, write them out */
                if (foundDots > 0) {
                    for (int j = 0; j < foundDots; j++) {
                        newResult[newLength++] = '.';
                    }
                }
                newResult[newLength++] = result.charAt(i);
                foundDots = 0;
            }
        }
        // remove trailing slash
        if (newLength > (rootLoc + 1)
                && newResult[newLength - 1] == separatorChar) {
            newLength--;
        }
        canonPath = getCanonImpl(new String(newResult, 0, newLength));
        FileCanonPathCache.put(absPath, canonPath);
        return canonPath;
    }
    
    /*
     * Resolve symbolic links in the parent directories.
     */
    private String resolve(String newResult) throws IOException {
    	return newResult;
    }

    /*
     * Resolve a symbolic link. While the path resolves to an existing path,
     * keep resolving. If an absolute link is found, resolve the parent
     * directories if resolveAbsolute is true.
     */
    private String resolveLink(String pathBytes, int length,
            boolean resolveAbsolute) throws IOException {
        boolean restart = false;
        String linkBytes, temp;
        do {
            linkBytes = getLinkImpl(pathBytes);
            if (linkBytes == pathBytes) {
                break;
            }
            if (linkBytes.charAt(0) == separatorChar) {
                // link to an absolute path, if resolving absolute paths,
                // resolve the parent dirs again
                restart = resolveAbsolute;
                pathBytes = linkBytes;
            } else {
                int last = length - 1;
                while (pathBytes.charAt(last) != separatorChar) {
                    last--;
                }
                last++;
                temp = pathBytes + linkBytes;
                pathBytes = temp;
            }
            length = pathBytes.length();
        } while (existsImpl(pathBytes));
        // resolve the parent directories
        if (restart) {
            return resolve(pathBytes);
        }
        return pathBytes;
    }

    /**
     * Returns a new file created using the canonical path of this file.
     * Equivalent to {@code new File(this.getCanonicalPath())}.
     * 
     * @return the new file constructed from this file's canonical path.
     * @throws IOException
     *             if an I/O error occurs.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public TFile getCanonicalFile() throws IOException {
        return new TFile(getCanonicalPath());
    }


    /**
     * Returns the name of the file or directory represented by this file.
     * 
     * @return this file's name or an empty string if there is no name part in
     *         the file's path.
     */
    public String getName() {
        int separatorIndex = path.lastIndexOf(separator);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1,
                path.length());
    }

    /**
     * Returns the pathname of the parent of this file. This is the path up to
     * but not including the last name. {@code null} is returned if there is no
     * parent.
     * 
     * @return this file's parent pathname or {@code null}.
     */
    public String getParent() {
        int length = path.length(), firstInPath = 0;
        if (separatorChar == '\\' && length > 2 && path.charAt(1) == ':') {
            firstInPath = 2;
        }
        int index = path.lastIndexOf(separatorChar);
        if (index == -1 && firstInPath > 0) {
            index = 2;
        }
        if (index == -1 || path.charAt(length - 1) == separatorChar) {
            return null;
        }
        if (path.indexOf(separatorChar) == index
                && path.charAt(firstInPath) == separatorChar) {
            return path.substring(0, index + 1);
        }
        return path.substring(0, index);
    }

    /**
     * Returns a new file made from the pathname of the parent of this file.
     * This is the path up to but not including the last name. {@code null} is
     * returned when there is no parent.
     * 
     * @return a new file representing this file's parent or {@code null}.
     */
    public TFile getParentFile() {
        String tempParent = getParent();
        if (tempParent == null) {
            return null;
        }
        return new TFile(tempParent);
    }

    /**
     * Returns the path of this file.
     * 
     * @return this file's path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects for which
     * {@code equals} returns {@code true} must return the same hash code.
     * 
     * @return this files's hash value.
     * @see #equals
     */
    @Override
    public int hashCode() {
        if (caseSensitive) {
            return path.hashCode() ^ 1234321;
        }
        return path.toLowerCase().hashCode() ^ 1234321;
    }

    /**
     * Indicates if this file's pathname is absolute. Whether a pathname is
     * absolute is platform specific. On UNIX, absolute paths must start with
     * the character '/'; on Windows it is absolute if either it starts with
     * '\\' (to represent a file server), or a letter followed by a colon.
     * 
     * @return {@code true} if this file's pathname is absolute, {@code false}
     *         otherwise.
     * @see #getPath
     */
    public boolean isAbsolute() {
        if (TFile.separatorChar == '\\') {
            // for windows
            if (path.length() > 1 && path.charAt(0) == TFile.separatorChar
                    && path.charAt(1) == TFile.separatorChar) {
                return true;
            }
            if (path.length() > 2) {
                if (Character.isLetter(path.charAt(0)) && path.charAt(1) == ':'
                        && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    return true;
                }
            }
            return false;
        }

        // for Linux
        return (path.length() > 0 && path.charAt(0) == TFile.separatorChar);
    }

    /**
     * Indicates if this file represents a <em>directory</em> on the
     * underlying file system.
     * 
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isDirectory() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isDirectoryImpl(properPath(true));
    }


    /**
     * Indicates if this file represents a <em>file</em> on the underlying
     * file system.
     * 
     * @return {@code true} if this file is a file, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isFile() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isFileImpl(properPath(true));
    }


    /**
     * Returns whether or not this file is a hidden file as defined by the
     * operating system. The notion of "hidden" is system-dependent. For Unix
     * systems a file is considered hidden if its name starts with a ".". For
     * Windows systems there is an explicit flag in the file system for this
     * purpose.
     * 
     * @return {@code true} if the file is hidden, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isHidden() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isHiddenImpl(properPath(true));
    }

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight.
     * 
     * @return the time when this file was last modified.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public long lastModified() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        long result = lastModifiedImpl(properPath(true));
        /* Temporary code to handle both return cases until natives fixed */
        if (result == -1 || result == 0) {
            return 0;
        }
        return result;
    }

    /**
     * Sets the time this file was last modified, measured in milliseconds since
     * January 1st, 1970, midnight.
     * 
     * @param time
     *            the last modification time for this file.
     * @return {@code true} if the operation is successful, {@code false}
     *         otherwise.
     * @throws IllegalArgumentException
     *             if {@code time < 0}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access to this file.
     */
    public boolean setLastModified(long time) {
        if (time < 0) {
            throw new IllegalArgumentException(Messages.getString("luni.B2")); //$NON-NLS-1$
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return (setLastModifiedImpl(properPath(true), time));
    }

    /**
     * Marks this file or directory to be read-only as defined by the operating
     * system.
     * 
     * @return {@code true} if the operation is successful, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access to this file.
     */
    public boolean setReadOnly() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return (setReadOnlyImpl(properPath(true)));
    }


    /**
     * Returns the length of this file in bytes.
     * 
     * @return the number of bytes in this file.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return lengthImpl(properPath(true));
    }


    /**
     * Returns an array of strings with the file names in the directory
     * represented by this file. The result is {@code null} if this file is not
     * a directory.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directory are not returned as part of the list.
     *
     * @return an array of strings with file names or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public java.lang.String[] list() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }

        if (path.length() == 0) {
            return null;
        }

        String bs = properPath(true);
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs)) {
            return null;
        }

        String[] implList = listImpl(bs);
        if (implList == null) {
            // empty list
            return new String[0];
        }
        String result[] = new String[implList.length];
        for (int index = 0; index < implList.length; index++) {
            result[index] = implList[index];
        }
        return result;
    }

    /**
     * Returns an array of files contained in the directory represented by this
     * file. The result is {@code null} if this file is not a directory. The
     * paths of the files in the array are absolute if the path of this file is
     * absolute, they are relative otherwise.
     * 
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list
     * @see #isDirectory
     */
    public TFile[] listFiles() {
        String[] tempNames = list();
        if (tempNames == null) {
            return null;
        }
        int resultLength = tempNames.length;
        TFile results[] = new TFile[resultLength];
        for (int i = 0; i < resultLength; i++) {
            results[i] = new TFile(this, tempNames[i]);
        }
        return results;
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and files with matching
     * names are returned as an array of files. Returns {@code null} if this
     * file is not a directory. If {@code filter} is {@code null} then all
     * filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list(TFilenameFilter filter)
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public TFile[] listFiles(TFilenameFilter filter) {
        String[] tempNames = list(filter);
        if (tempNames == null) {
            return null;
        }
        int resultLength = tempNames.length;
        TFile results[] = new TFile[resultLength];
        for (int i = 0; i < resultLength; i++) {
            results[i] = new TFile(this, tempNames[i]);
        }
        return results;
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FileFilter and matching files are
     * returned as an array of files. Returns {@code null} if this file is not a
     * directory. If {@code filter} is {@code null} then all files match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public TFile[] listFiles(TFileFilter filter) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }

        if (path.length() == 0) {
            return null;
        }

        String bs = properPath(true);
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs)) {
            return null;
        }

        String[] implList = listImpl(bs);
        if (implList == null) {
            return new TFile[0];
        }
        List<TFile> tempResult = new ArrayList<TFile>();
        for (int index = 0; index < implList.length; index++) {
            String aName = implList[index];
            TFile aFile = new TFile(this, aName);
            if (filter == null || filter.accept(aFile)) {
                tempResult.add(aFile);
            }
        }
        return tempResult.toArray(new TFile[tempResult.size()]);
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and the names of files
     * with matching names are returned as an array of strings. Returns
     * {@code null} if this file is not a directory. If {@code filter} is
     * {@code null} then all filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     * 
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public java.lang.String[] list(TFilenameFilter filter) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }

        if (path.length() == 0) {
            return null;
        }

        String bs = properPath(true);
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs)) {
            return null;
        }

        String[] implList = listImpl(bs);
        if (implList == null) {
            // empty list
            return new String[0];
        }
        List<String> tempResult = new ArrayList<String>();
        for (int index = 0; index < implList.length; index++) {
            String aName = implList[index];
            if (filter == null || filter.accept(this, aName)) {
                tempResult.add(aName);
            }
        }

        return tempResult.toArray(new String[tempResult.size()]);
    }

    /**
     * Creates the directory named by the trailing filename of this file. Does
     * not create the complete path required to create this directory.
     * 
     * @return {@code true} if the directory has been created, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @see #mkdirs
     */
    public boolean mkdir() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return mkdirImpl(properPath(true));
    }


    /**
     * Creates the directory named by the trailing filename of this file,
     * including the complete directory path required to create this directory.
     * 
     * @return {@code true} if the necessary directories have been created,
     *         {@code false} if the target directory already exists or one of
     *         the directories can not be created.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @see #mkdir
     */
    public boolean mkdirs() {
        /* If the terminal directory already exists, answer false */
        if (exists()) {
            return false;
        }

        /* If the receiver can be created, answer true */
        if (mkdir()) {
            return true;
        }

        String parentDir = getParent();
        /* If there is no parent and we were not created, answer false */
        if (parentDir == null) {
            return false;
        }

        /* Otherwise, try to create a parent directory and then this directory */
        return (new TFile(parentDir).mkdirs() && mkdir());
    }

    /**
     * Creates a new, empty file on the file system according to the path
     * information stored in this file.
     * 
     * @return {@code true} if the file has been created, {@code false} if it
     *         already exists.
     * @throws IOException
     *             if an I/O error occurs or the directory does not exist where
     *             the file should have been created.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     */
    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (0 == path.length()) {
            throw new IOException(Messages.getString("luni.B3")); //$NON-NLS-1$
        }
        int result = newFileImpl(properPath(true));
        switch (result) {
            case 0:
                return true;
            case 1:
                return false;
            default:
                throw new IOException(Messages.getString("luni.B4", path)); //$NON-NLS-1$
        }
    }


    /**
     * Creates an empty temporary file using the given prefix and suffix as part
     * of the file name. If suffix is {@code null}, {@code .tmp} is used. This
     * method is a convenience method that calls
     * {@link #createTempFile(String, String, TFile)} with the third argument
     * being {@code null}.
     * 
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @return the temporary file.
     * @throws IOException
     *             if an error occurs when writing the file.
     */
    public static TFile createTempFile(String prefix, String suffix)
            throws IOException {
        return createTempFile(prefix, suffix, null);
    }

    /**
     * Creates an empty temporary file in the given directory using the given
     * prefix and suffix as part of the file name.
     * 
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @param directory
     *            the location to which the temp file is to be written, or
     *            {@code null} for the default location for temporary files,
     *            which is taken from the "java.io.tmpdir" system property. It
     *            may be necessary to set this property to an existing, writable
     *            directory for this method to work properly.
     * @return the temporary file.
     * @throws IllegalArgumentException
     *             if the length of {@code prefix} is less than 3.
     * @throws IOException
     *             if an error occurs when writing the file.
     */
    @SuppressWarnings("nls")
    public static TFile createTempFile(String prefix, String suffix,
            TFile directory) throws IOException {
        // Force a prefix null check first
        if (prefix.length() < 3) {
            throw new IllegalArgumentException(Messages.getString("luni.B5"));
        }
        String newSuffix = suffix == null ? ".tmp" : suffix;
        TFile tmpDirFile;
        if (directory == null) {
            String tmpDir = AccessController.doPrivileged(
                new PriviAction<String>("java.io.tmpdir", "."));
            tmpDirFile = new TFile(tmpDir);
        } else {
            tmpDirFile = directory;
        }
        TFile result;
        do {
            result = genTempFile(prefix, newSuffix, tmpDirFile);
        } while (!result.createNewFile());
        return result;
    }

    private static TFile genTempFile(String prefix, String suffix, TFile directory) {
        int identify = 0;
        synchronized (tempFileLocker) {
            if (counter == 0) {
                int newInt = new TSecureRandom().nextInt();
                counter = ((newInt / 65535) & 0xFFFF) + 0x2710;
                counterBase = counter;
            }
            identify = counter++;
        }

        StringBuilder newName = new StringBuilder();
        newName.append(prefix);
        newName.append(counterBase);
        newName.append(identify);
        newName.append(suffix);
        return new TFile(directory, newName.toString());
    }

    /**
     * Returns a string representing the proper path for this file. If this file
     * path is absolute, the user.dir property is not prepended, otherwise it
     * is.
     * 
     * @param internal
     *            is user.dir internal.
     * @return the proper path.
     */
    String properPath(boolean internal) {
        if (properPath != null) {
            return properPath;
        }

        if (isAbsolute()) {
            return properPath = path;
        }
        // Check security by getting user.dir when the path is not absolute
        String userdir;
        userdir = System.getProperty("user.dir"); //$NON-NLS-1$

        if (path.length() == 0) {
            return properPath = userdir;
        }
        int length = userdir.length();

        // Handle windows-like path
        if (path.charAt(0) == '\\') {
            if (length > 1 && userdir.charAt(1) == ':') {
                return properPath = userdir.substring(0, 2) + path;
            }
            path = path.substring(1);
        }

        // Handle separator
        String result = userdir;
        if (userdir.charAt(length - 1) != separatorChar) {
            if (path.charAt(0) != separatorChar) {
                result += separator;
            }
        } else if (path.charAt(0) == separatorChar) {
            result = result.substring(0, length - 2);

        }
        result += path;
        return properPath = result;
    }

    /**
     * Renames this file to the name represented by the {@code dest} file. This
     * works for both normal files and directories.
     * 
     * @param dest
     *            the file containing the new name.
     * @return {@code true} if the File was renamed, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file or the {@code dest} file.
     */
    public boolean renameTo(org.teavm.classlib.java.io.TFile dest) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
            security.checkWrite(dest.path);
        }
        return renameToImpl(properPath(true), dest.properPath(true));
    }


    /**
     * Returns a string containing a concise, human-readable description of this
     * file.
     * 
     * @return a printable representation of this file.
     */
    @Override
    public String toString() {
        return path;
    }

    /**
     * Returns a Uniform Resource Identifier for this file. The URI is system
     * dependent and may not be transferable between different operating / file
     * systems.
     * 
     * @return an URI for this file.
     */
    @SuppressWarnings("nls")
    public URI toURI() {
        String name = getAbsoluteName();
        try {
            if (!name.startsWith("/")) {
                // start with sep.
                return new URI("file", null, new StringBuilder(
                        name.length() + 1).append('/').append(name).toString(),
                        null, null);
            } else if (name.startsWith("//")) {
                return new URI("file", "", name, null); // UNC path
            }
            return new URI("file", null, name, null, null);
        } catch (URISyntaxException e) {
            // this should never happen
            return null;
        }
    }

    /**
     * Returns a Uniform Resource Locator for this file. The URL is system
     * dependent and may not be transferable between different operating / file
     * systems.
     * 
     * @return a URL for this file.
     * @throws java.net.MalformedURLException
     *             if the path cannot be transformed into a URL.
     */
    @SuppressWarnings("nls")
    public TURL toURL() throws java.net.MalformedURLException {
        String name = getAbsoluteName();
        if (!name.startsWith("/")) {
            // start with sep.
            return new TURL(
                    "file", EMPTY_STRING, -1, new StringBuilder(name.length() + 1) //$NON-NLS-1$ 
                            .append('/').append(name).toString(), null);
        } else if (name.startsWith("//")) {
            return new TURL("file:" + name); // UNC path
        }
        return new TURL("file", EMPTY_STRING, -1, name, null);
    }

    private String getAbsoluteName() {
        TFile f = getAbsoluteFile();
        String name = f.getPath();

        if (f.isDirectory() && name.charAt(name.length() - 1) != separatorChar) {
            // Directories must end with a slash
            name = new StringBuilder(name.length() + 1).append(name)
                    .append('/').toString();
        }
        if (separatorChar != '/') { // Must convert slashes.
            name = name.replace(separatorChar, '/');
        }
        return name;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeChar(separatorChar);

    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        char inSeparator = stream.readChar();
        path = path.replace(inSeparator, separatorChar);
    }
    
    private boolean isDirectoryImpl(String filePath) {
    	final JSFileStat stat = TEmscriptenFileSystem.FS_stat(filePath);
    	if (stat == null) {
    		return false;
    	}
		return TEmscriptenFileSystem.FS_isDir(stat.getMode());
    }
    
    private String getCanonImpl(String filePath) {
    	return filePath;
    }

    private static String[] rootsImpl() {
    	return new String[]{"/"};
    }

    private static boolean isCaseSensitiveImpl() {
    	return true;
    }

    private boolean deleteDirImpl(String filePath) {
    	TEmscriptenFileSystem.FS_unlink(filePath);
    	return !TEmscriptenFileSystem._FS_exists(filePath);
    }

    private boolean deleteFileImpl(String filePath) {
    	TEmscriptenFileSystem.FS_unlink(filePath);
    	return !TEmscriptenFileSystem._FS_exists(filePath);
    }

    private boolean isHiddenImpl(String filePath) {
    	return false; // EmscriptenFileSystem.FS_isDir(EmscriptenFileSystem.FS_stat(filePath).getMode());
    }

    private boolean isReadOnlyImpl(String filePath) {
    	return false; // EmscriptenFileSystem.FS_isDir(EmscriptenFileSystem.FS_stat(filePath).getMode());
    }

    private boolean isWriteOnlyImpl(String filePath) {
    	return false; // EmscriptenFileSystem.FS_isDir(EmscriptenFileSystem.FS_stat(filePath).getMode());
    }

    private String getLinkImpl(String filePath) {
    	return filePath; // XXX resolve link?
    }

    private long lastModifiedImpl(String filePath) {
    	return Math.round(((JSDate) TEmscriptenFileSystem.FS_stat(filePath).getMtime()).getTime());
    }

    private boolean setLastModifiedImpl(String path, long time) {
    	nyi("setLastModifiedImpl");
    	return false;
    }

    private void nyi(String method) {
		System.out.println("NYI: " + method);
	}

	private int newFileImpl(String filePath) {
		if (!TEmscriptenFileSystem._FS_exists(filePath)) {
			JSFileStream stream = TEmscriptenFileSystem.FS_open(filePath, "w+");
			if (stream == null) {
				return -1;
			}
			TEmscriptenFileSystem.FS_close(stream);
			return 0;
		}
		
		return 1;
	}

    private synchronized static String[] listImpl(String path) {
    	final String[] list = TEmscriptenFileSystem.FS_readdir(path);
    	if (list != null && list.length >= 2) {
    		final String[] l2 = new String[list.length - 2];
    		
    		int j = 0;
    		for (int i = 0; i < list.length; i++) {
    			final String s = list[i];
    			if (!(".".equals(s) || "..".equals(s))) {
    				l2[j] = s;
    				j++;
    			}
    		}
    		
    		return l2;
    	}
		return list;
    }
    
    private long lengthImpl(String filePath) {
    	return TEmscriptenFileSystem.FS_stat(filePath).getSize();
    }
    
    private boolean setReadOnlyImpl(String path) {
    	nyi("setReadOnlyImpl");
    	return false;
    }
    
    private boolean isFileImpl(String filePath) {
    	final JSFileStat stat = TEmscriptenFileSystem.FS_stat(filePath);
    	if (stat == null) {
    		return false;
    	}
		return TEmscriptenFileSystem.FS_isFile(stat.getMode());
    }

    private boolean existsImpl(String filePath) {
    	return TEmscriptenFileSystem._FS_exists(filePath);
    }
    
    private boolean mkdirImpl(String filePath) {
    	TEmscriptenFileSystem.FS_mkdir(filePath);
    	final boolean exists = TEmscriptenFileSystem._FS_exists(filePath);
		return exists;
    }

    private boolean renameToImpl(String pathExist, String pathNew) {
    	TEmscriptenFileSystem.FS_rename(pathExist, pathNew);
    	return TEmscriptenFileSystem._FS_exists(pathNew);
    }


}
