package us.ihmc.util;

public class RealtimeTools
{
   
   public static int nextPowerOfTwo(int v)
   {
      // Algorithm from http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
      v--;
      v |= v >> 1;
      v |= v >> 2;
      v |= v >> 4;
      v |= v >> 8;
      v |= v >> 16;
      return ++v;
   }

}
