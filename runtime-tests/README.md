# Local server tests

`run_server.py` starts an isolated dedicated server, places a real Lootr chest in its world, runs two player inventory accesses in one server tick, and stops cleanly. A second server process opens the same saved world and verifies the persisted mutation and player-scoped clear behavior.

The test counts calls to a deterministic loot filler. Two server-side player objects must receive the same inventory; removing a diamond through one must change the other view. No network clients are connected. The modern probes also open Lootr through its normal server handler and verify the menu slot contents. Client rendering, mouse clicks, and Jade tooltip presentation require separate client checks.

The probe is compiled into its own source set and loaded only by the explicit Gradle init script. Production JARs contain no probe classes. Test results are written to each instance directory, and a Gradle success exit alone does not count as a passing test: both explicit phase markers are required.

```bash
python3 runtime-tests/run_server.py --profile 1.21.1 --loader neoforge
python3 runtime-tests/run_server.py --profile 26.2 --loader fabric --probe inventory-store
```

Use a fresh `--output` directory for a new run. `--port` can select a different loopback port when another test server is running. The runner retains logs, world files, and its result JSON.

Use `--without-jade` to test the required dependencies without optional Jade; the result records whether Jade was excluded. Some historical Jade builds fail on a dedicated server before Shared Lootr can be exercised.

The runner selects the Lootr API probe and player implementation from the release target. `--probe` is available for probe development.

## Minecraft 1.12.2

Use Java 8 and an isolated Forge 14.23.5.2847 dedicated server. Install the production Shared Lootr JAR, Lootr 0.6.2, RinLib 0.2.0+1.12.2, and MixinBooter 11.17. Compile the separate probe:

```bash
versions/legacy-1.12.2/gradlew -p versions/legacy-1.12.2 reobfRuntimeProbeJar \
  -I "$PWD/runtime-tests/probe/legacy.init.gradle" \
  -Dprobe.sources="$PWD/runtime-tests/probe/legacy-1.12"
```

Copy `shared-lootr-local-probe-1.0.0.jar` from `build/profiles/1.12.2/root/libs/` into that server's `mods` directory. Start the server with `java -Xmx2G -jar forge-1.12.2-14.23.5.2847-universal.jar nogui` twice, preserving the world between starts. Require `SHARED_LOOTR_PHASE1_PASS` in the first log and `SHARED_LOOTR_PHASE2_PASS` in the second. The probe stops the server after each phase and is never included in the production JAR.
