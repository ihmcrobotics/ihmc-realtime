package us.ihmc.realtime;

public class PriorityParameters
{
   private static int maximumPriority = RealtimeNative.getMaximumPriorityNative();
   private static int minimumPriority = RealtimeNative.getMinimumPriorityNative();
   
   private final int priority;
   
   public PriorityParameters(int priority)
   {
      if(minimumPriority > priority || priority > maximumPriority)
      {
         throw new RuntimeException("Unsupported priority, requested " + priority + ", minimum: " + minimumPriority + "; maximum: " + maximumPriority);
      }
      
      this.priority = priority;
   }
   
   public int getPriority()
   {
      return this.priority;
   }
   
   
   public static int getMaximumPriority()
   {
      return maximumPriority;
   }
   
   public static int getMinimumPriority()
   {
      return minimumPriority;
   }
}
