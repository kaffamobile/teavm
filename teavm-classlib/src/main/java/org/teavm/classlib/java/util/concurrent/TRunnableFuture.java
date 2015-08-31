/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.teavm.classlib.java.util.concurrent;

/**
 * A {@link TFuture} that is {@link Runnable}. Successful execution of
 * the <tt>run</tt> method causes completion of the <tt>Future</tt>
 * and allows access to its results.
 * @see TFutureTask
 * @see TExecutor
 * @since 1.6
 * @author Doug Lea
 */
public interface TRunnableFuture extends Runnable, TFuture {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    void run();
}
