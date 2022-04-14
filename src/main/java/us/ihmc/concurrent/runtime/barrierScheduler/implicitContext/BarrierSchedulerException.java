package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext;

public class BarrierSchedulerException extends RuntimeException
{
   private static final long serialVersionUID = -4850880477560714442L;

   public BarrierSchedulerException()
   {
      super();
   }

   public BarrierSchedulerException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public BarrierSchedulerException(String message)
   {
      super(message);
   }

   public BarrierSchedulerException(Throwable cause)
   {
      super(cause);
   }
}
