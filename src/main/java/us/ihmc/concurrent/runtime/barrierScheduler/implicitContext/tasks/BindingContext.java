package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.tasks;

import java.util.ArrayList;
import java.util.List;

public abstract class BindingContext
{
   private final List<UniBinding<?>> bindings = new ArrayList<>();

   public <T extends InPlaceCopyable<T>> void bind(T source, T destination)
   {
      UniBinding<T> binding = new UniBinding<>(source, destination);
      bindings.add(binding);
   }

   public void updateMasterContext()
   {
      for (int i = 0; i < bindings.size(); i++)
      {
         UniBinding<?> binding = bindings.get(i);
         binding.updateBufferFromSource();
      }
   }

   public void updateLocalContext()
   {
      for (int i = 0; i < bindings.size(); i++)
      {
         UniBinding<?> binding = bindings.get(i);
         binding.updateDestinationFromBuffer();
      }
   }
}
