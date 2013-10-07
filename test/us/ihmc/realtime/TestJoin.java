package us.ihmc.realtime;

public class TestJoin
{
   private static volatile int tester = 0;
   public static void main(String[] args)
   {
      
      RealtimeThread thread = new RealtimeThread(new PriorityParameters(50))
      {
         @Override
         public void run()
         {
            try
            {
               Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            tester = 10;
         }
      };
      
      thread.start();
      tester = 8;
      thread.join();
      
      System.out.println(tester);
   }
}
