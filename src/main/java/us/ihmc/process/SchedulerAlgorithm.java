package us.ihmc.process;

public enum SchedulerAlgorithm
{
   SCHED_OTHER(0),
   SCHED_FIFO(1),
   SCHED_RR(2),
   SCHED_BATCH(3),
   SCHED_ISO(4),
   SCHED_IDLE(5),
   SCHED_DEADLINE(6);
   
   private static final SchedulerAlgorithm[] values = values();

   private final int cOrdinal;
   SchedulerAlgorithm(int cOrdinal)
   {
      this.cOrdinal = cOrdinal;
   }
   
   public int getCOrdinal()
   {
      return cOrdinal;
   }
   
   public static SchedulerAlgorithm fromCOrdinal(int cOrdinal)
   {
      for(SchedulerAlgorithm algorithm :  values)
      {
         if(algorithm.cOrdinal == cOrdinal)
         {
            return algorithm;
         }
      }
      
      return null;
   }
}