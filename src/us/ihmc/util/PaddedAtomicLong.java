package us.ihmc.util;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Padding atomic long class
 * 
 * @see http://mechanical-sympathy.blogspot.com/2011/07/false-sharing.html
 * @see http://mechanical-sympathy.blogspot.com/2011/08/false-sharing-java-7.html
 *  
 *  
 * @author Jesper Smith
 *
 */
public class PaddedAtomicLong extends AtomicLong 
{
   public volatile long p1, p2, p3, p4, p5, p6, p7 = 8L;
   
   public PaddedAtomicLong()
   {
      super();
   }
   
   public PaddedAtomicLong(long initialValue)
   {
      super(initialValue);
   }
   
   /**
    * Public function to avoid removal of padding
    * 
    * @return sum of p
    * 
    */
   public long avoidPaddingRemoval()
   {
      return p1 + p2 + p3 + p4 + p5 + p6 + p7;
   }
}
