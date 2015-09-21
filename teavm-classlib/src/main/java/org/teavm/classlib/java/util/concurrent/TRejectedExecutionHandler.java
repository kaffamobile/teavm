/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.teavm.classlib.java.util.concurrent;

/**
 * A handler for tasks that cannot be executed by a {@link TThreadPoolExecutor}.
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface TRejectedExecutionHandler {

    /**
     * Method that may be invoked by a {@link TThreadPoolExecutor} when
     * {@link TThreadPoolExecutor#execute execute} cannot accept a
     * task.  This may occur when no more threads or queue slots are
     * available because their bounds would be exceeded, or upon
     * shutdown of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link TRejectedExecutionException}, which will be
     * propagated to the caller of {@code execute}.
     *
     * @param r the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     * @throws TRejectedExecutionException if there is no remedy
     */

    void rejectedExecution(Runnable r, TThreadPoolExecutor executor);
}