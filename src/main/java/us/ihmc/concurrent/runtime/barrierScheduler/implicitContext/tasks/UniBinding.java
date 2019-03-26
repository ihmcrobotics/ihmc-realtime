package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.tasks;

/**
 * Implements a one-direction (hence, uni) binding between two {@link InPlaceCopyable} objects
 * of the same type.
 *
 * A Unibinding maintains three fields. The source and destination fields represent the one-way map,
 * with information being copied from the source to the destination during the {@link us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.BarrierScheduler}'s
 * sync step. The Unibinding also maintains an internal "buffer", which functions as an intermediate state or a local
 * version of the "master context". Thus, data flow goes from source -> buffer -> destination.
 *
 * @param <T>
 */
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

   /**
    * Update the local buffer from the source object.
    */
   public void updateBufferFromSource()
   {
      buffer.copyFrom(source);
   }

   /**
    * Update the destination object with whatever is currently in the internal buffer object.
    */
   public void updateDestinationFromBuffer()
   {
      destination.copyFrom(buffer);
   }

   /**
    * Creates a new instance of the {@link InPlaceCopyable} type used to parameterize the binding,
    * using the default/empty constructor. This is used to create the internal buffer.
    *
    * @return A new instance of the {@link InPlaceCopyable} type used to parameterize the binding,
    * or null if the type does not define an empty constructor.
    */
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
