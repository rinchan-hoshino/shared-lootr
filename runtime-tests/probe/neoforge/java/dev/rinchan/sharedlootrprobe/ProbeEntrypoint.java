package dev.rinchan.sharedlootrprobe;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
@Mod("shared_lootr_probe")
public final class ProbeEntrypoint {
 public ProbeEntrypoint() {
  NeoForge.EVENT_BUS.addListener(this::started);
  NeoForge.EVENT_BUS.addListener(this::stopped);
 }
 private void started(ServerStartedEvent event) { new RuntimeProbe().run(event.getServer()); }
 private void stopped(ServerStoppedEvent event) {
  // The world is already saved. Wait for the server thread before ending this test process.
  Thread exit = new Thread(() -> {
   try {
    event.getServer().getRunningThread().join();
    System.out.println("SHARED_LOOTR_SERVER_STOPPED");
    System.exit(0);
   } catch (InterruptedException error) {
    Thread.currentThread().interrupt();
    System.out.println("SHARED_LOOTR_RUNTIME_FAIL " + error);
   }
  }, "shared-lootr-probe-exit");
  exit.setDaemon(true);
  exit.start();
 }
}
