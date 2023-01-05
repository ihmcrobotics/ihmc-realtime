plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.6"
   id("us.ihmc.ihmc-cd") version "1.23"
}

ihmc {
   group = "us.ihmc"
   version = "1.5.1"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-realtime"
   openSource = true

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("us.ihmc:ihmc-native-library-loader:2.0.2")
}

app.entrypoint("ihmc-realtime", "us.ihmc.realtime.TestBarrierSchedulerCyclic")

val appDirectory = "/home/shadylady/IHMCRealtime"

tasks.create("deploy")
{
   dependsOn("installDist")

   doLast {
      remote.session("rt", "shadylady")
      {
         exec("mkdir -p $appDirectory")

         exec("rm -rf $appDirectory/bin")
         exec("rm -rf $appDirectory/lib")

         put(file("build/install/ihmc-realtime/bin").toString(), "$appDirectory/bin")
         put(file("build/install/ihmc-realtime/lib").toString(), "$appDirectory/lib")

         exec("chmod +x $appDirectory/bin/ihmc-realtime")
      }
   }
}
