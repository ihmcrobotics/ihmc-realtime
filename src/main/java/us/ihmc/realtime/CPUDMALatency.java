package us.ihmc.realtime;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Helper class to configure the DMA latency using /dev/cpu_dma_latency on linux systems.
 * 
 * This significantly reduces jitter in realtime loops.
 * 
 * @author Jesper Smith
 *
 */
public class CPUDMALatency
{
   private final FileOutputStream dev;

   private final byte[] converterData = new byte[4];
   private final ByteBuffer converter = ByteBuffer.wrap(converterData);

   private CPUDMALatency(int latency) throws IOException
   {
      dev = new FileOutputStream("/dev/cpu_dma_latency");
      converter.order(ByteOrder.nativeOrder());

      converter.putInt(0, latency);
      dev.write(converterData);
   }

   private void close() throws IOException
   {
      dev.close();
   }

   private static CPUDMALatency cpudmaLatency = null;

   
   /**
    * Set the DMA latency. Recommend value is 0
    * 
    * @param latencyInUs
    * @return False if /dev/cpu_dma_latency cannot be written
    */
   public static synchronized boolean setLatency(int latencyInUs)
   {
      if (cpudmaLatency == null)
      {
         try
         {
            cpudmaLatency = new CPUDMALatency(latencyInUs);
         }
         catch (IOException e)
         {
            System.err.println("Cannot set desired CPU DMA latency");
            return false;
         }
         
         return true;
      }
      else
      {
         throw new RuntimeException("CPU DMA Latency has already been set");
      }
   }
   
   /**
    * Unregistert the DMA latency requirement.
    */
   public static synchronized void unsetLatency()
   {
      if(cpudmaLatency != null)
      {
         try
         {
            cpudmaLatency.close();
         }
         catch (IOException e)
         {
         }
         cpudmaLatency = null;
      }
      else
      {
         throw new RuntimeException("CPU DMA Latency has not been set");
      }
      
         
   }
}
