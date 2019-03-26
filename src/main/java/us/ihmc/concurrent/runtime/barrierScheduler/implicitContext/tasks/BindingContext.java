package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * The BindingContext defines a contract for "binding" two {@link us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.Task} instances
 * that are parameterized over the same data type iff the data type implements {@link InPlaceCopyable}.
 *
 * <br /><br />
 *
 * Uni-directional bindings (implemented via {@link UniBinding}) are created within the context. Each binding pair maintains its own
 * internal "Master" context for thread safety, and an instance of a BindingContext can have as many bindings between tasks as needed.
 *
 * <br /><br />
 *
 * This class preserves the API and outward behavior of the normal barrier scheduler but allows for fine-grained control over
 * which tasks write to each other instead of enforcing that all tasks must flow through a single master context instance, which
 * can be useful for making sure that in scenarios with many threads that are parameterized over the same context that you can reduce the
 * chances of the threads clobbering each others data accidentally.
 */
public abstract class BindingContext
{
   private final List<UniBinding<?>> bindings = new ArrayList<>();

   /**
    * Create a {@link UniBinding} from Source to Destination.
    *
    * @param source the source object
    * @param destination the destination object
    * @param <T> The type over which the objects are parameterized
    */
   public <T extends InPlaceCopyable<T>> void bind(T source, T destination)
   {
      UniBinding<T> binding = new UniBinding<>(source, destination);
      bindings.add(binding);
   }

   /**
    * Called by the Barrier Scheduler during the stage when the global master context would be updated;
    * causes each binding pair to update its internal intermediate buffer.
    */
   public void updateMasterContext()
   {
      for (int i = 0; i < bindings.size(); i++)
      {
         UniBinding<?> binding = bindings.get(i);
         binding.updateBufferFromSource();
      }
   }

   /**
    * Called by the Barrier Scheduler during the stage when the task local contexts would be updated from the global master context;
    * causes each binding pair to update the destination object from the internal intermediate buffer.
    */
   public void updateLocalContext()
   {
      for (int i = 0; i < bindings.size(); i++)
      {
         UniBinding<?> binding = bindings.get(i);
         binding.updateDestinationFromBuffer();
      }
   }
}
