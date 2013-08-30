package us.ihmc.realtime;


public class TestPriority implements Runnable
{
   public static void main(String[] args) throws InterruptedException
   {
      
      PriorityParameters priorityParameters = new PriorityParameters(90);
      RealtimeThread realtimeThread = new RealtimeThread(priorityParameters, new TestPriority());
      
      realtimeThread.start();
      
      Thread.sleep(1000);
   }

   public void run()
   {
      System.out.println("Thread scheduling algorithm: " + RealtimeThread.getCurrentThreadScheduler());
      System.out.println("Thread priority: " + RealtimeThread.getCurrentThreadPriority());
   }

}
