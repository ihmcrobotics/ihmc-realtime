package us.ihmc.realtime;


public class TestCyclic
{
   public static void main(String[] args) throws InterruptedException
   {
      final int periodInNS = 1000000;
      final int iterations = 10000;
      
      PriorityParameters priorityParameters = new PriorityParameters(99);
      PeriodicParameters periodicParameters = new PeriodicParameters(new MonotonicTime(0, periodInNS));
      
      
      
      RealtimeThread periodicRealtimeThread = new RealtimeThread(priorityParameters, periodicParameters, null)
      {
         @Override 
         public void run()
         {
            long previousTime = 0;
            
            long averageJitter = 0;
            long maxJitter = 0;
            for(int i = -1; i < iterations; i++)
            {
               super.waitForNextPeriod();
               if(i < 0)
               {
                  previousTime = System.nanoTime();
                  continue;
               }
               long newTime = System.nanoTime();
               
               long jitter = Math.abs(newTime - previousTime - periodInNS);
               averageJitter += jitter;
               
               if(jitter > maxJitter) 
               {
                  maxJitter = jitter;
               }
               previousTime = newTime;
            }
            
            System.out.println("Average jitter: " + (((double) averageJitter)/((double)iterations))/1e6 + "ms");
            System.out.println("Max jitter: " + ((double)maxJitter)/(1e6) + "ms");
            
         }
      };
      periodicRealtimeThread.start();
      
      Thread.sleep(1000);
   }


}
