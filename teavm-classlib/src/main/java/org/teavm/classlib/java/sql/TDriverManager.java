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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.harmony.sql.internal.nls.Messages;

/**
 * Provides facilities for managing JDBC drivers.
 * <p>
 * The {@code DriverManager} class loads JDBC drivers during its initialization,
 * from the list of drivers referenced by the system property {@code
 * "jdbc.drivers"}.
 */
public class TDriverManager {

    /*
     * Facilities for logging. The Print Stream is deprecated but is maintained
     * here for compatibility.
     */
    private static PrintStream thePrintStream;

    private static PrintWriter thePrintWriter;

    // Login timeout value - by default set to 0 -> "wait forever"
    private static int loginTimeout = 0;

    /*
     * Set to hold Registered Drivers - initial capacity 10 drivers (will expand
     * automatically if necessary.
     */
    private static final List<TDriver> theDrivers = new ArrayList<TDriver>(10);

    /*
     * Load drivers on initialization
     */
    static {
        loadInitialDrivers();
    }

    /*
     * Loads the set of JDBC drivers defined by the Property "jdbc.drivers" if
     * it is defined.
     */
    private static void loadInitialDrivers() {
    	return;
    }

    /*
     * A private constructor to prevent allocation
     */
    private TDriverManager() {
        super();
    }

    /**
     * Removes a driver from the {@code DriverManager}'s registered driver list.
     * This will only succeed when the caller's class loader loaded the driver
     * that is to be removed. If the driver was loaded by a different class
     * loader, the removal of the driver fails silently.
     * <p>
     * If the removal succeeds, the {@code DriverManager} will not use this
     * driver in the future when asked to get a {@code Connection}.
     *
     * @param driver
     *            the JDBC driver to remove.
     * @throws TSQLException
     *             if there is a problem interfering with accessing the
     *             database.
     */
    public static void deregisterDriver(TDriver driver) throws TSQLException {
        if (driver == null) {
            return;
        }
        synchronized (theDrivers) {
            theDrivers.remove(driver);
        }
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws TSQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     */
    public static TConnection getConnection(String url) throws TSQLException {
        return getConnection(url, new Properties());
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with
     * @param info
     *            a set of properties to use as arguments to set up the
     *            connection. Properties are arbitrary string/value pairs.
     *            Normally, at least the properties {@code "user"} and {@code
     *            "password"} should be passed, with appropriate settings for
     *            the user ID and its corresponding password to get access to
     *            the corresponding database.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws TSQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     */
    public static TConnection getConnection(String url, Properties info)
            throws TSQLException {
        // 08 - connection exception
        // 001 - SQL-client unable to establish SQL-connection
        String sqlState = "08001"; //$NON-NLS-1$
        if (url == null) {
            // sql.5=The url cannot be null
            throw new TSQLException(Messages.getString("sql.5"), sqlState); //$NON-NLS-1$
        }
        synchronized (theDrivers) {
            /*
             * Loop over the drivers in the DriverSet checking to see if one can
             * open a connection to the supplied URL - return the first
             * connection which is returned
             */
            for (TDriver theDriver : theDrivers) {
                TConnection theConnection = theDriver.connect(url, info);
                if (theConnection != null) {
                    return theConnection;
                }
            }
        }
        // If we get here, none of the drivers are able to resolve the URL
        // sql.6=No suitable driver
        throw new TSQLException(Messages.getString("sql.6"), sqlState); //$NON-NLS-1$ 
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with.
     * @param user
     *            a user ID used to login to the database.
     * @param password
     *            a password for the user ID to login to the database.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws TSQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     */
    public static TConnection getConnection(String url, String user,
            String password) throws TSQLException {
        Properties theProperties = new Properties();
        if (null != user) {
            theProperties.setProperty("user", user); //$NON-NLS-1$
        }
        if (null != password) {
            theProperties.setProperty("password", password); //$NON-NLS-1$
        }
        return getConnection(url, theProperties);
    }

    /**
     * Tries to find a driver that can interpret the supplied URL.
     * 
     * @param url
     *            the URL of a database.
     * @return a {@code Driver} that matches the provided URL. {@code null} if
     *         no {@code Driver} understands the URL
     * @throws TSQLException
     *             if there is any kind of problem accessing the database.
     */
    public static TDriver getDriver(String url) throws TSQLException {
        synchronized (theDrivers) {
            /*
             * Loop over the drivers in the DriverSet checking to see if one
             * does understand the supplied URL - return the first driver which
             * does understand the URL
             */
            Iterator<TDriver> theIterator = theDrivers.iterator();
            while (theIterator.hasNext()) {
                TDriver theDriver = theIterator.next();
                if (theDriver.acceptsURL(url)) {
                    return theDriver;
                }
            }
        }
        // If no drivers understand the URL, throw an SQLException
        // sql.6=No suitable driver
        // SQLState: 08 - connection exception
        // 001 - SQL-client unable to establish SQL-connection
        throw new TSQLException(Messages.getString("sql.6"), "08001"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns an {@code Enumeration} that contains all of the loaded JDBC
     * drivers that the current caller can access.
     * 
     * @return An {@code Enumeration} containing all the currently loaded JDBC
     *         {@code Drivers}.
     */
    public static Enumeration<TDriver> getDrivers() {
        /*
         * Synchronize to avoid clashes with additions and removals of drivers
         * in the DriverSet
         */
        synchronized (theDrivers) {
            /*
             * Create the Enumeration by building a Vector from the elements of
             * the DriverSet
             */
            Vector<TDriver> theVector = new Vector<TDriver>();
            Iterator<TDriver> theIterator = theDrivers.iterator();
            while (theIterator.hasNext()) {
                TDriver theDriver = theIterator.next();
                theVector.add(theDriver);
            }
            return theVector.elements();
        }
    }

    /**
     * Returns the login timeout when connecting to a database in seconds.
     * 
     * @return the login timeout in seconds.
     */
    public static int getLoginTimeout() {
        return loginTimeout;
    }

    /**
     * Gets the log {@code PrintStream} used by the {@code DriverManager} and
     * all the JDBC Drivers.
     *
     * @deprecated use {@link #getLogWriter()} instead.
     * @return the {@code PrintStream} used for logging activities.
     */
    @Deprecated
    public static PrintStream getLogStream() {
        return thePrintStream;
    }

    /**
     * Retrieves the log writer.
     * 
     * @return A {@code PrintWriter} object used as the log writer. {@code null}
     *         if no log writer is set.
     */
    public static PrintWriter getLogWriter() {
        return thePrintWriter;
    }

    /**
     * Prints a message to the current JDBC log stream. This is either the
     * {@code PrintWriter} or (deprecated) the {@code PrintStream}, if set.
     * 
     * @param message
     *            the message to print to the JDBC log stream.
     */
    public static void println(String message) {
        if (thePrintWriter != null) {
            thePrintWriter.println(message);
            thePrintWriter.flush();
        } else if (thePrintStream != null) {
            thePrintStream.println(message);
            thePrintStream.flush();
        }
        /*
         * If neither the PrintWriter not the PrintStream are set, then silently
         * do nothing the message is not recorded and no exception is generated.
         */
        return;
    }

    /**
     * Registers a given JDBC driver with the {@code DriverManager}.
     * <p>
     * A newly loaded JDBC driver class should register itself with the
     * {@code DriverManager} by calling this method.
     *
     * @param driver
     *            the {@code Driver} to register with the {@code DriverManager}.
     * @throws TSQLException
     *             if a database access error occurs.
     */
    public static void registerDriver(TDriver driver) throws TSQLException {
        if (driver == null) {
            throw new NullPointerException();
        }
        synchronized (theDrivers) {
            theDrivers.add(driver);
        }
    }

    /**
     * Sets the login timeout when connecting to a database in seconds.
     * 
     * @param seconds
     *            seconds until timeout. 0 indicates wait forever.
     */
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
        return;
    }

    /**
     * Sets the print stream to use for logging data from the {@code
     * DriverManager} and the JDBC drivers.
     *
     * @deprecated Use {@link #setLogWriter} instead.
     * @param out
     *            the {@code PrintStream} to use for logging.
     */
    @Deprecated
    public static void setLogStream(PrintStream out) {
        thePrintStream = out;
    }

    /**
     * Sets the {@code PrintWriter} that is used by all loaded drivers, and also
     * the {@code DriverManager}.
     * 
     * @param out
     *            the {@code PrintWriter} to be used.
     */
    public static void setLogWriter(PrintWriter out) {
        thePrintWriter = out;
    }


}
