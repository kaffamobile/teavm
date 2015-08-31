/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.teavm.classlib.java.util.concurrent;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.teavm.classlib.java.util.concurrent.helpers.*;

/**
 * Provides default implementations of {@link TExecutorService}
 * execution methods. This class implements the <tt>submit</tt>,
 * <tt>invokeAny</tt> and <tt>invokeAll</tt> methods using a
 * {@link TRunnableFuture} returned by <tt>newTaskFor</tt>, which defaults
 * to the {@link TFutureTask} class provided in this package.  For example,
 * the implementation of <tt>submit(Runnable)</tt> creates an
 * associated <tt>RunnableFuture</tt> that is executed and
 * returned. Subclasses may override the <tt>newTaskFor</tt> methods
 * to return <tt>RunnableFuture</tt> implementations other than
 * <tt>FutureTask</tt>.
 *
 * <p> <b>Extension example</b>. Here is a sketch of a class
 * that customizes {@link TThreadPoolExecutor} to use
 * a <tt>CustomTask</tt> class instead of the default <tt>FutureTask</tt>:
 * <pre>
 * public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
 *
 *   static class CustomTask&lt;V&gt; implements RunnableFuture&lt;V&gt; {...}
 *
 *   protected &lt;V&gt; RunnableFuture&lt;V&gt; newTaskFor(Callable&lt;V&gt; c) {
 *       return new CustomTask&lt;V&gt;(c);
 *   }
 *   protected &lt;V&gt; RunnableFuture&lt;V&gt; newTaskFor(Runnable r, V v) {
 *       return new CustomTask&lt;V&gt;(r, v);
 *   }
 *   // ... add constructors, etc.
 * }
 * </pre>
 * @since 1.5
 * @author Doug Lea
 */
public abstract class TAbstractExecutorService implements TExecutorService {

    /**
     * Returns a <tt>RunnableFuture</tt> for the given runnable and default
     * value.
     *
     * @param runnable the runnable task being wrapped
     * @param value the default value for the returned future
     * @return a <tt>RunnableFuture</tt> which when run will run the
     * underlying runnable and which, as a <tt>Future</tt>, will yield
     * the given value as its result and provide for cancellation of
     * the underlying task.
     * @since 1.6
     */
    protected TRunnableFuture newTaskFor(Runnable runnable, Object value) {
        return new TFutureTask(runnable, value);
    }

    /**
     * Returns a <tt>RunnableFuture</tt> for the given callable task.
     *
     * @param callable the callable task being wrapped
     * @return a <tt>RunnableFuture</tt> which when run will call the
     * underlying callable and which, as a <tt>Future</tt>, will yield
     * the callable's result as its result and provide for
     * cancellation of the underlying task.
     * @since 1.6
     */
    protected TRunnableFuture newTaskFor(TCallable callable) {
        return new TFutureTask(callable);
    }

    /**
     * @throws TRejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public TFuture submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        TRunnableFuture ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    /**
     * @throws TRejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public TFuture submit(Runnable task, Object result) {
        if (task == null) throw new NullPointerException();
        TRunnableFuture ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    /**
     * @throws TRejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public TFuture submit(TCallable task) {
        if (task == null) throw new NullPointerException();
        TRunnableFuture ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    /**
     * the main mechanics of invokeAny.
     */
    private Object doInvokeAny(Collection tasks,
                               boolean timed, long nanos)
        throws InterruptedException, TExecutionException, TTimeoutException {
        if (tasks == null)
            throw new NullPointerException();
        int ntasks = tasks.size();
        if (ntasks == 0)
            throw new IllegalArgumentException();
        List futures= new ArrayList(ntasks);
        TExecutorCompletionService ecs =
            new TExecutorCompletionService(this);

        // For efficiency, especially in executors with limited
        // parallelism, check to see if previously submitted tasks are
        // done before submitting more of them. This interleaving
        // plus the exception mechanics account for messiness of main
        // loop.

        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            TExecutionException ee = null;
            long lastTime = (timed)? System.nanoTime() : 0;
            Iterator it = tasks.iterator();

            // Start one task for sure; the rest incrementally
            futures.add(ecs.submit((TCallable)it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                TFuture f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit((TCallable)it.next()));
                        ++active;
                    }
                    else if (active == 0)
                        break;
                    else if (timed) {
                        f = ecs.poll(nanos, TTimeUnit.NANOSECONDS);
                        if (f == null)
                            throw new TTimeoutException();
                        long now = System.nanoTime();
                        nanos -= now - lastTime;
                        lastTime = now;
                    }
                    else
                        f = ecs.take();
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch (InterruptedException ie) {
                        throw ie;
                    } catch (TExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new TExecutionException(rex);
                    }
                }
            }

            if (ee == null)
                ee = new TExecutionException();
            throw ee;

        } finally {
            for (Iterator f = futures.iterator(); f.hasNext();)
                ((TFuture)f.next()).cancel(true);
        }
    }

    public Object invokeAny(Collection tasks)
        throws InterruptedException, TExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TTimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }

    public Object invokeAny(Collection tasks,
                            long timeout, TTimeUnit unit)
        throws InterruptedException, TExecutionException, TTimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    public List invokeAll(Collection tasks) throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        List futures = new ArrayList(tasks.size());
        boolean done = false;
        try {
            for (Iterator t = tasks.iterator(); t.hasNext();) {
                TRunnableFuture f = newTaskFor((TCallable)t.next());
                futures.add(f);
                execute(f);
            }
            for (Iterator i = futures.iterator(); i.hasNext();) {
                TFuture f = (TFuture) i.next();
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (TCancellationException ignore) {
                    } catch (TExecutionException ignore) {
                    }
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done)
                for (Iterator i = futures.iterator(); i.hasNext();) {
                    TFuture f = (TFuture) i.next();
                    f.cancel(true);
                }
        }
    }

    public List invokeAll(Collection tasks,
                          long timeout, TTimeUnit unit)
        throws InterruptedException {
        if (tasks == null || unit == null)
            throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        List futures = new ArrayList(tasks.size());
        boolean done = false;
        try {
            for (Iterator t = tasks.iterator(); t.hasNext();)
                futures.add(newTaskFor((TCallable)t.next()));

            long lastTime = System.nanoTime();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            Iterator it = futures.iterator();
            while (it.hasNext()) {
                execute((Runnable)(it.next()));
                long now = System.nanoTime();
                nanos -= (now - lastTime);
                lastTime = now;
                if (nanos <= 0)
                    return futures;
            }

            for (Iterator i = futures.iterator(); i.hasNext();) {
                TFuture f = (TFuture)i.next();
                if (!f.isDone()) {
                    if (nanos <= 0)
                        return futures;
                    try {
                        f.get(nanos, TTimeUnit.NANOSECONDS);
                    } catch (TCancellationException ignore) {
                    } catch (TExecutionException ignore) {
                    } catch (TTimeoutException toe) {
                        return futures;
                    }
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done)
                for (Iterator i = futures.iterator(); i.hasNext();) {
                    TFuture f = (TFuture) i.next();
                    f.cancel(true);
                }
        }
    }

}
