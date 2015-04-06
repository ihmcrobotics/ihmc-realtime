package us.ihmc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Helper class that unpacks and optionally loads native libraries
 * 
 * @author Jesper Smith
 *
 */
public class NativeLibraryLoader
{
   private static final String PACKAGE_NAME = "us.ihmc.realtime.lib";
   private static final String LIBRARY_NAME = "RealtimeNative";

   public final static String LIBRARY_LOCATION = new File(System.getProperty("user.home"), ".ihmc" + File.separator + "lib").getAbsolutePath();
   
   private static boolean loaded = false;
   
   

   private NativeLibraryLoader()
   {
      // Disallow construction
   }

   
   public static synchronized void loadLibrary()
   {
      if (loaded)
      {
         return;
      }
      String prefix = createPackagePrefix(PACKAGE_NAME);
      String library = System.mapLibraryName(LIBRARY_NAME);
      URL libraryURL = NativeLibraryLoader.class.getClassLoader().getResource(prefix + library);
      
      if (libraryURL == null)
      {
         throw new UnsatisfiedLinkError("Cannot load library " + prefix + library);
      }

      // Try to load the library directly. If not possible, fall trough and unpack to temp directory
      if ("file".equals(libraryURL.getProtocol()))
      {
         try
         {
            File libraryFile = new File(libraryURL.toURI());
            if (libraryFile.canRead())
            {
               System.load(libraryFile.getAbsolutePath());
               loaded = true;
               return;
            }
         }
         catch (URISyntaxException e)
         {
         }
      }
      
      File directory = new File(LIBRARY_LOCATION + "/" + prefix);
      if (!directory.exists())
      {
         boolean res = directory.mkdirs();
         if (!res) { throw new UnsatisfiedLinkError("Cannot create directory: " + directory.getAbsolutePath()); }
      }

      File lib = new File(directory, library);
      InputStream stream = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(prefix + library);
      if (stream == null)
      {
         throw new UnsatisfiedLinkError("Cannot load library " + prefix + library);
      }
      writeStreamToFile(stream, lib);

      try
      {
         stream.close();
      }
      catch (IOException e)
      {
      }

      System.load(lib.getAbsolutePath());
      loaded = true;
   }

   private static String createPackagePrefix(String packageName)
   {
      packageName = packageName.trim().replace('.', '/');
      if (packageName.length() > 0)
      {
         packageName = packageName + '/';
      }
      return packageName;
   }

   private static void writeStreamToFile(InputStream stream, File file)
   {
      try
      {
         FileOutputStream out = new FileOutputStream(file);
         byte[] buf = new byte[1024];
         int len;
         while ((len = stream.read(buf)) > 0)
         {
            out.write(buf, 0, len);
         }

         out.close();

      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }
}
