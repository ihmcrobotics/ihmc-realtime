package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A single threaded version of the {@link BarrierScheduler}. This allows running a set of tasks in
 * a single thread sequentially using the same {@link Task} API as the multi-threaded version. The
 * {@link #run()} method needs to be invoked every scheduler tick but no task threads need to be
 * started. When a task is executed the {@link #run()} method blocks until completion of the task.
 *
 * @param <C> the context type
 */
public class SingleThreadedScheduler<C> implements Runnable
{
   private final List<? extends Task<C>> tasks;

   private final C masterContext;

   private final boolean[] tasksInitialized;

   private long tick;

   public SingleThreadedScheduler(Collection<? extends Task<C>> tasks, C masterContext)
   {
      this.tasks = new ArrayList<>(tasks);
      this.masterContext = masterContext;
      this.tasksInitialized = new boolean[tasks.size()];
   }

   @Override
   public void run()
   {
      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);
         if (task.isPending(tick))
            task.updateMasterContext(masterContext);
      }

      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);
         if (task.isPending(tick))
            task.updateLocalContext(masterContext);
      }

      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);
         if (task.isPending(tick))
         {
            if (!tasksInitialized[i])
               tasksInitialized[i] = task.initialize();
            if (tasksInitialized[i])
               task.execute();
         }
      }

      tick++;
   }

   public void shutdown()
   {
      for (int i = 0; i < tasks.size(); i++)
      {
         tasks.get(i).cleanup();
      }
   }
}
