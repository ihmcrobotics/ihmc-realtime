package us.ihmc.affinity;

public class Processor
{
   private final int id;

   public Processor(int cpuID)
   {
      this.id = cpuID;
   }
   
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("\t\t\tProcessor: ");
      builder.append(id);
      builder.append("\n");
      return builder.toString();
   }

   public int getId()
   {
      return id;
   }

}
