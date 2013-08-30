package us.ihmc.realtime;



public class PeriodicParameters
{
   private final MonotonicTime startTime;
   private final MonotonicTime period;
   
   
   public PeriodicParameters(MonotonicTime period)
   {
      this.startTime = null;
      this.period = period;
   }
   
   public PeriodicParameters(MonotonicTime startTime, MonotonicTime period)
   {
      this.startTime = startTime;
      this.period = period;
   }

   public MonotonicTime getStartTime()
   {
      return startTime;
   }
   
   public MonotonicTime getPeriod()
   {
      return period;
   }
}
