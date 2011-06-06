package org.jruby.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;

public class ForkJoin implements Library {
    /*
     * To load, for now:
     * 
     * require 'jruby'
     * org.jruby.ext.ForkJoin.new.load(JRuby.runtime, false)
     */
    public void load(Ruby runtime, boolean wrap) throws IOException {
        RubyModule forkJoin = runtime.defineModule("ForkJoin");
        RubyClass task = forkJoin.defineClassUnder("Task", runtime.getObject(), new ObjectAllocator() {
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new Task(runtime, klazz);
            }
        });
        task.defineAnnotatedMethods(Task.class);
        
        RubyClass pool = forkJoin.defineClassUnder("Pool", runtime.getObject(), new ObjectAllocator() {
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new Pool(runtime, klazz);
            }
        });
        pool.defineAnnotatedMethods(Pool.class);
    }

    public static class Pool extends RubyObject {
        private volatile ForkJoinPool pool;
        private Pool(Ruby runtime, RubyClass poolClass) {
            super(runtime, poolClass);
        }
        
        @JRubyMethod
        public IRubyObject initialize(ThreadContext context) {
            pool = new ForkJoinPool();
            return context.nil;
        }
        
        @JRubyMethod
        public IRubyObject initialize(ThreadContext context, IRubyObject parallelism) {
            pool = new ForkJoinPool((int)parallelism.convertToInteger().getLongValue());
            return context.nil;
        }
        
        @JRubyMethod
        public IRubyObject await(ThreadContext context, IRubyObject _timeout) {
            long timeout = _timeout.convertToInteger().getLongValue();
            
            boolean result;
            try {
                result = pool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                throw context.runtime.newThreadError("interrupted while awaiting ForkJoin::Pool termination");
            }
            
            return context.runtime.newBoolean(result);
        }
        
        @JRubyMethod
        public IRubyObject execute(ThreadContext context, final Block block) {
            final Ruby runtime = context.runtime;
            pool.execute(new Runnable() {
                public void run() {
                    block.call(runtime.getCurrentContext());
                }
            });
            
            return context.nil;
        }
        
        @JRubyMethod
        public IRubyObject execute(ThreadContext context, final IRubyObject task, Block unused) {
            final Ruby runtime = context.runtime;
            if (task instanceof Task) {
                pool.execute(((Task)task).task);
            } else {
                pool.execute(new Runnable() {
                    public void run() {
                        task.callMethod(runtime.getCurrentContext(), "call");
                    }
                });
            }
            
            return context.nil;
        }
        
        @JRubyMethod
        public IRubyObject invoke(ThreadContext context, IRubyObject task) {
            if (!(task instanceof Task)) {
                throw context.runtime.newTypeError(task, "ForkJoin::Task");
            }
            
            Object result = pool.invoke(((Task)task).task);
            
            return JavaUtil.convertJavaToUsableRubyObject(context.runtime, result);
        }
        
        @JRubyMethod
        public IRubyObject invoke_all(ThreadContext context, IRubyObject _array) {
            final Ruby runtime = context.runtime;
            RubyArray array = _array.convertToArray();
            
            Collection<Callable<IRubyObject>> tmp = new ArrayList();
            for (int i = 0; i < array.size(); i++) {
                final IRubyObject task = array.eltOk(i);
                tmp.add(new Callable<IRubyObject>() {
                    public IRubyObject call() throws Exception {
                        return task.callMethod(runtime.getCurrentContext(), "call");
                    }
                });
            }
            
            List<Future<IRubyObject>> _results = pool.invokeAll(tmp);
            
            RubyArray results = RubyArray.newArray(runtime, _results.size());
            for (Future result : _results) results.add(result);
            
            return results;
        }
        
        @JRubyMethod
        public IRubyObject active_thread_count(ThreadContext context) {
            return context.runtime.newFixnum(pool.getActiveThreadCount());
        }
        
        @JRubyMethod
        public IRubyObject async_mode(ThreadContext context) {
            return context.runtime.newBoolean(pool.getAsyncMode());
        }
        
        @JRubyMethod
        public IRubyObject parallelism(ThreadContext context) {
            return context.runtime.newFixnum(pool.getParallelism());
        }
        
        @JRubyMethod
        public IRubyObject pool_size(ThreadContext context) {
            return context.runtime.newFixnum(pool.getPoolSize());
        }
        
        @JRubyMethod
        public IRubyObject queued_submission_count(ThreadContext context) {
            return context.runtime.newFixnum(pool.getQueuedSubmissionCount());
        }
        
        @JRubyMethod
        public IRubyObject queue_task_count(ThreadContext context) {
            return context.runtime.newFixnum(pool.getQueuedTaskCount());
        }
        
        @JRubyMethod
        public IRubyObject running_thread_count(ThreadContext context) {
            return context.runtime.newFixnum(pool.getRunningThreadCount());
        }
        
        @JRubyMethod
        public IRubyObject steal_count(ThreadContext context) {
            return context.runtime.newFixnum(pool.getStealCount());
        }
        
        @JRubyMethod(name = "queued_submissions?")
        public IRubyObject queued_submissions_p(ThreadContext context) {
            return context.runtime.newBoolean(pool.hasQueuedSubmissions());
        }
        
        @JRubyMethod(name = "quiescent?")
        public IRubyObject quiescent_p(ThreadContext context) {
            return context.runtime.newBoolean(pool.isQuiescent());
        }
        
        @JRubyMethod(name = "shutdown?")
        public IRubyObject shutdown_p(ThreadContext context) {
            return context.runtime.newBoolean(pool.isShutdown());
        }
        
        @JRubyMethod(name = "terminated?")
        public IRubyObject terminated_p(ThreadContext context) {
            return context.runtime.newBoolean(pool.isTerminated());
        }
        
        @JRubyMethod(name = "terminating?")
        public IRubyObject terminating_p(ThreadContext context) {
            return context.runtime.newBoolean(pool.isTerminating());
        }
    }

    public static class Task extends RubyObject {
        private volatile ForkJoinTask<IRubyObject> task;
        
        private Task(Ruby runtime, RubyClass taskClass) {
            super(runtime, taskClass);
            task = callTask(runtime, this);
        }
        
        private Task(Ruby runtime, RubyClass taskClass, ForkJoinTask task) {
            super(runtime, taskClass);
            this.task = task;
        }
        
        private Task(final Ruby runtime, RubyClass taskClass, final IRubyObject task) {
            super(runtime, taskClass);
            this.task = callTask(runtime, task);
        }
        
        private static ForkJoinTask<IRubyObject> callTask(final Ruby runtime, final IRubyObject task) {
            return new ForkJoinTask<IRubyObject>() {
                IRubyObject result;
                
                @Override
                public IRubyObject getRawResult() {
                    return result;
                }

                @Override
                protected void setRawResult(IRubyObject result) {
                    this.result = result;
                }

                @Override
                protected boolean exec() {
                    try {
                        result = task.callMethod(runtime.getCurrentContext(), "call");
                    } catch (RaiseException re) {
                        result = re.getException();
                        return false;
                    }
                    return true;
                }
            };
        }
        
        @JRubyMethod
        public IRubyObject result() {
            return task.getRawResult();
        }
        
        @JRubyMethod
        public IRubyObject join() {
            return task.join();
        }
        
        @JRubyMethod
        public IRubyObject fork(ThreadContext context) {
            return new Task(context.runtime, getMetaClass(), task.fork());
        }
    }
}
