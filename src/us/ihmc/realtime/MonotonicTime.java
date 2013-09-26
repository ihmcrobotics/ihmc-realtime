package us.ihmc.realtime;

public class MonotonicTime
{
   public static final long NANOSECONDS_PER_SECOND = 1000000000;
   
   private long seconds;
   private long nanoseconds;
   
   public static MonotonicTime getCurrentTime()
   {
      MonotonicTime monotonicTime = new MonotonicTime();
      monotonicTime.setToCurrentTime();
      return monotonicTime;
   }
   
   public MonotonicTime(long seconds, long nanoseconds)
   {
      this.seconds = seconds;
      this.nanoseconds = nanoseconds;
      
      normalize();
   }
   
   public MonotonicTime()
   {
      this.seconds = 0;
      this.nanoseconds = 0;
   }
   
   
   private void normalize()
   {      
      while(nanoseconds >= NANOSECONDS_PER_SECOND)
      {
         nanoseconds -= NANOSECONDS_PER_SECOND;
         seconds++;
      }
      
      while(nanoseconds < 0)
      {
         nanoseconds += NANOSECONDS_PER_SECOND;
         seconds--;
      }
      
   }
   
   public void add(MonotonicTime time)
   {
      seconds += time.seconds;
      nanoseconds += time.nanoseconds;
      
      normalize();
   }
   
   public void sub(MonotonicTime time)
   {
      seconds -= time.seconds;
      nanoseconds -= time.nanoseconds;
      
      normalize();
   }
   
   public void set(long seconds, long nanoseconds)
   {
      this.seconds = seconds;
      this.nanoseconds = nanoseconds;
      
      normalize();
   }
   
   public void set(MonotonicTime monotonicTime)
   {
      this.seconds = monotonicTime.seconds;
      this.nanoseconds = monotonicTime.nanoseconds;
   }
   
   public void setToCurrentTime()
   {
      this.seconds = 0;
      this.nanoseconds = RealtimeNative.getCurrentTimeNative();
      
      normalize();
   }

   long seconds()
   {
      return seconds;
   }
   
   long nanoseconds()
   {
      return nanoseconds;
   }
   
   @Override
   public String toString()
   {
      return seconds + "s " + nanoseconds + "ns";
   }
}
