package us.ihmc.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple utility to describe processes on a Linux system with /proc support.
 * 
 * Static functions are used to get a list of all processes or a list filtered by name
 * 
 * @author Jesper Smith
 *
 */
public class LinuxProcess
{
   private final int pid;
   private final String comm;
   private final String commandLine;

   private LinuxProcess(int pid, String comm, String commandLine)
   {
      this.pid = pid;
      this.comm = comm;
      this.commandLine = commandLine;
   }

   @Override
   public String toString()
   {
      return "LinuxProcess [pid=" + pid + ", comm=" + comm + ", commandLine=" + commandLine + "]";
   }

   /**
    * 
    * @return the process ID
    */
   public int getPid()
   {
      return pid;
   }

   /**
    * 
    * @return The process's comm value, which is the command name associated with the process.
    */
   public String getComm()
   {
      return comm;
   }

   /**
    * 
    * @return The complete command line for the process, unless the process is a zombie.
    */
   public String getCommandLine()
   {
      return commandLine;
   }

   /**
    * Get a list of all processes currently run on this PC
    * 
    * @return List or processes on this PC
    * @throws IOException If the proc file system is not supported.
    */
   public static List<LinuxProcess> getProcesses() throws IOException
   {
      Path procFS = Paths.get("/proc");
      if (!Files.exists(procFS))
      {
         throw new IOException("/proc not found. This utility requires support for the /proc file system");
      }

      Pattern zero = Pattern.compile("\\u0000");
      Pattern unprintable = Pattern.compile("\\p{C}");

      ArrayList<LinuxProcess> processes = new ArrayList<>();
      for (int i = 1; i <= 32168; i++)
      {
         Path processDir = procFS.resolve(String.valueOf(i));
         if (Files.isDirectory(processDir))
         {
            int pid = i;
            String comm = null;
            String commandLine = null;

            Path commFile = processDir.resolve("comm");
            if (Files.exists(commFile))
            {
               List<String> commLines = Files.readAllLines(commFile);
               comm = commLines.get(0);
            }

            Path commandLineFile = processDir.resolve("cmdline");
            if (Files.exists(commandLineFile))
            {
               List<String> commandLines = Files.readAllLines(commandLineFile);
               if (commandLines.size() > 0)
               {
                  commandLine = zero.matcher(commandLines.get(0)).replaceAll(" ");
                  commandLine = unprintable.matcher(commandLine).replaceAll("?");
               }
            }

            LinuxProcess process = new LinuxProcess(pid, comm, commandLine);
            processes.add(process);
         }
      }
      return processes;
   }

   /**
    * Returns a list of all processes matching a given pattern.
    * 
    * @param regex Regular expression to match
    * @return List of process matching regex
    * @throws IOException If the proc file system is not supported.
    */
   public static List<LinuxProcess> getProcessesByPattern(String regex) throws IOException
   {
      Pattern p = Pattern.compile(regex);

      List<LinuxProcess> processes = getProcesses();
      List<LinuxProcess> matched = new ArrayList<>();
      for (LinuxProcess process : processes)
      {
         if (p.matcher(process.getComm()).matches())
         {
            matched.add(process);
         }
      }
      return matched;
   }

   public static void main(String[] args) throws IOException
   {
      System.out.println(getProcessesByPattern("java"));
   }
}
