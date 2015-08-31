/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.teavm.classlib.java.util.concurrent;
import org.teavm.classlib.java.util.concurrent.*;

/**
 * A {@link TCompletionService} that uses a supplied {@link TExecutor}
 * to execute tasks.  This class arranges that submitted tasks are,
 * upon completion, placed on a queue accessible using <tt>take</tt>.
 * The class is lightweight enough to be suitable for transient use
 * when processing groups of tasks.
 *
 * <p>
 *
 * <b>Usage Examples.</b>
 *
 * Suppose you have a set of solvers for a certain problem, each
 * returning a value of some type <tt>Result</tt>, and would like to
 * run them concurrently, processing the results of each of them that
 * return a non-null value, in some method <tt>use(Result r)</tt>. You
 * could write this as:
 *
 * <pre>
 *   void solve(Executor e,
 *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)
 *     throws InterruptedException, ExecutionException {
 *       CompletionService&lt;Result&gt; ecs
 *           = new ExecutorCompletionService&lt;Result&gt;(e);
 *       for (Callable&lt;Result&gt; s : solvers)
 *           ecs.submit(s);
 *       int n = solvers.size();
 *       for (int i = 0; i &lt; n; ++i) {
 *           Result r = ecs.take().get();
 *           if (r != null)
 *               use(r);
 *       }
 *   }
 * </pre>
 *
 * Suppose instead that you would like to use the first non-null result
 * of the set of tasks, ignoring any that encounter exceptions,
 * and cancelling all other tasks when the first one is ready:
 *
 * <pre>
 *   void solve(Executor e,
 *              Collection&lt;Callable&lt;Result&gt;&gt; solvers)
 *     throws InterruptedException {
 *       CompletionService&lt;Result&gt; ecs
 *           = new ExecutorCompletionService&lt;Result&gt;(e);
 *       int n = solvers.size();
 *       List&lt;Future&lt;Result&gt;&gt; futures
 *           = new ArrayList&lt;Future&lt;Result&gt;&gt;(n);
 *       Result result = null;
 *       try {
 *           for (Callable&lt;Result&gt; s : solvers)
 *               futures.add(ecs.submit(s));
 *           for (int i = 0; i &lt; n; ++i) {
 *               try {
 *                   Result r = ecs.take().get();
 *                   if (r != null) {
 *                       result = r;
 *                       break;
 *                   }
 *               } catch (ExecutionException ignore) {}
 *           }
 *       }
 *       finally {
 *           for (Future&lt;Result&gt; f : futures)
 *               f.cancel(true);
 *       }
 *
 *       if (result != null)
 *           use(result);
 *   }
 * </pre>
 */
public class TExecutorCompletionService implements TCompletionService {
    private final TExecutor executor;
    private final TAbstractExecutorService aes;
    private final TBlockingQueue completionQueue;

    /**
     * FutureTask extension to enqueue upon completion
     */
    private class QueueingFuture extends TFutureTask {
        QueueingFuture(TRunnableFuture task) {
            super(task, null);
            this.task = task;
        }
        protected void done() { completionQueue.add(task); }
        private final TFuture task;
    }

    private TRunnableFuture newTaskFor(TCallable task) {
        if (aes == null)
            return new TFutureTask(task);
        else
            return aes.newTaskFor(task);
    }

    private TRunnableFuture newTaskFor(Runnable task, Object result) {
        if (aes == null)
            return new TFutureTask(task, result);
        else
            return aes.newTaskFor(task, result);
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and a
     * {@link TLinkedBlockingQueue} as a completion queue.
     *
     * @param executor the executor to use
     * @throws NullPointerException if executor is <tt>null</tt>
     */
    public TExecutorCompletionService(TExecutor executor) {
        if (executor == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof TAbstractExecutorService) ?
            (TAbstractExecutorService) executor : null;
        this.completionQueue = new TLinkedBlockingQueue();
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and the supplied queue as its
     * completion queue.
     *
     * @param executor the executor to use
     * @param completionQueue the queue to use as the completion queue
     * normally one dedicated for use by this service. This queue is
     * treated as unbounded -- failed attempted <tt>Queue.add</tt>
     * operations for completed taskes cause them not to be
     * retrievable.
     * @throws NullPointerException if executor or completionQueue are <tt>null</tt>
     */
    public TExecutorCompletionService(TExecutor executor,
                                     TBlockingQueue completionQueue) {
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof TAbstractExecutorService) ?
            (TAbstractExecutorService) executor : null;
        this.completionQueue = completionQueue;
    }

    public TFuture submit(TCallable task) {
        if (task == null) throw new NullPointerException();
        TRunnableFuture f = newTaskFor(task);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    public TFuture submit(Runnable task, Object result) {
        if (task == null) throw new NullPointerException();
        TRunnableFuture f = newTaskFor(task, result);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    public TFuture take() throws InterruptedException {
        return (TFuture)completionQueue.take();
    }

    public TFuture poll() {
        return (TFuture)completionQueue.poll();
    }

    public TFuture poll(long timeout, TTimeUnit unit) throws InterruptedException {
        return (TFuture)completionQueue.poll(timeout, unit);
    }

}
