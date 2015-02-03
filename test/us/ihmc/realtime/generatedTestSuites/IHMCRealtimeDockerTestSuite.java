package us.ihmc.realtime.generatedTestSuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//import us.ihmc.utilities.code.unitTesting.runner.JUnitTestSuiteRunner;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
   us.ihmc.realtime.concurrent.ConcurrentCopierTest.class,
   us.ihmc.realtime.concurrent.ConcurrentRingBufferTest.class
})

public class IHMCRealtimeDockerTestSuite
{
   public static void main(String[] args)
   {
      //new JUnitTestSuiteRunner(IHMCRealtimeDockerTestSuite.class);
   }
}

