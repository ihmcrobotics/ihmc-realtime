package us.ihmc.realtime;

import org.junit.runner.*;
import org.junit.runners.*;

//import us.ihmc.utilities.code.unitTesting.JUnitTestSuiteGenerator;

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
      //JUnitTestSuiteGenerator.generateTestSuite(IHMCRealtimeDockerTestSuite.class);
   }
}

