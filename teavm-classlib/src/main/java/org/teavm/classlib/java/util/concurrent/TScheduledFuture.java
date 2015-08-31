/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.teavm.classlib.java.util.concurrent;

/**
 * A delayed result-bearing action that can be cancelled.
 * Usually a scheduled future is the result of scheduling
 * a task with a {@link TScheduledExecutorService}.
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface TScheduledFuture extends TDelayed, TFuture {
}
