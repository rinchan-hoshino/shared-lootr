# Shared Lootr 1.3.3 validation

This release covers the six affected Fabric and NeoForge cells for Minecraft 1.21, 1.21.1, and 1.21.11. Builds passed: 6/6. Isolated two-phase server tests passed: 8/8. The standalone NeoForge production-JAR test also passed both phases.

| Minecraft | Loader | Build | Tested Lootr version IDs | Save and restart |
|---|---|---|---|---|
| 1.21 | fabric | Pass | cOrO8AXN | Pass |
| 1.21 | neoforge | Pass | q5IlMBVM | Pass |
| 1.21.1 | fabric | Pass | rNCVo4w9, nar56EsW | Pass |
| 1.21.1 | neoforge | Pass | Trh49IZt, SPOrIzX8 | Pass |
| 1.21.11 | fabric | Pass | efKMR8nl | Pass |
| 1.21.11 | neoforge | Pass | TBkxTN54 | Pass |

The tests use a physical Lootr chest, two server-side player objects, and a full process restart. They cover shared inventory identity, single generation, actual menu contents, item removal, persistence, player-scoped clear, and regeneration. The saved-data probe observes the real provider to check shared opened-state broadcasts. On the four-argument API, both boolean values are exercised.

The standalone production-JAR test loaded the published NeoForge 1.21.1 artifact with NeoForge 21.1.256 and Lootr 1.11.38.127. Its SHA-256 matches the release build. No network clients were connected; client rendering and tooltip presentation were not tested.

Before the production fix, Lootr SPOrIzX8 failed with an InvalidInjectionException because the three-argument handleProviderOpen target no longer existed. The original failure log is hashed in the JSON record.

The initial Minecraft 1.21 NeoForge run passed its gameplay assertions and saved the world, but a remaining non-daemon thread prevented process exit. Removing optional Jade did not resolve it. The separate NeoForge probe now waits for the normal server-stopped event and server thread termination, then exits the test JVM. Original failed runs are retained. Final tests use the corrected probe with Jade included. This lifecycle handling is absent from production JARs.

All 13 source-contract tests passed across the existing 56-cell source manifest. Other release cells were not rebuilt or published by this compatibility update. The previous all-cell validation belonged to an earlier source fingerprint; it is available in repository history.

Production source fingerprint: `4292e68ddd7b6037846aad29bee2ee9c5984ff73069b27e249e35fe9caa403c5`.
