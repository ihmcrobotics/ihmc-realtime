package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.tasks;

class UniBinding<T extends InPlaceCopyable<T>>
{
   private T source;
   private T destination;
   private T buffer;

   public UniBinding(T source, T destination)
   {
      this.source = source;
      this.destination = destination;
      this.buffer = newInstance();
   }

   public void updateBufferFromSource()
   {
      buffer.copyFrom(source);
   }

   public void updateDestinationFromBuffer()
   {
      destination.copyFrom(buffer);
   }

   @SuppressWarnings("unchecked")
   public T newInstance()
   {
      try
      {
         return (T) source.getClass().newInstance();
      }
      catch (InstantiationException | IllegalAccessException e)
      {
         e.printStackTrace();
      }

      return null;
   }
}
