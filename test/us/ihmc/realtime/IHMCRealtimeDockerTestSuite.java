package us.ihmc.realtime;

import org.junit.runner.*;
import org.junit.runners.*;

import us.ihmc.utilities.test.JUnitTestSuiteConstructor;

@RunWith(Suite.class)
@Suite.SuiteClasses
({
   us.ihmc.realtime.concurrent.ConcurrentCopierTest.class,
   us.ihmc.realtime.concurrent.ConcurrentRingBufferTest.class,
   us.ihmc.realtime.TestCyclic.class,
   us.ihmc.realtime.TestJoin.class,
   us.ihmc.realtime.TestPriority.class
})

public class IHMCRealtimeDockerTestSuite
{
   public static void main(String[] args)
   {
      JUnitTestSuiteConstructor.generateTestSuite(IHMCRealtimeDockerTestSuite.class);
   }
}
