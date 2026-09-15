# Shared Lootr 1.3.3 validation

Builds passed: 56/56. Local server tests passed: 56/56.

Tests use a physical chest in an isolated local server world, two server-side player objects, and a full server restart. They cover shared inventory identity, single generation, visible item removal, persistence, and player-scoped clear. No network clients are connected.

| Minecraft | Loader | Build | Local server |
|---|---|---|---|
| 26.2 | neoforge | Pass | Pass |
| 26.2 | fabric | Pass | Pass |
| 26.1.2 | neoforge | Pass | Pass |
| 26.1.2 | fabric | Pass | Pass |
| 26.1.1 | neoforge | Pass | Pass |
| 26.1.1 | fabric | Pass | Pass |
| 26.1 | neoforge | Pass | Pass |
| 26.1 | fabric | Pass | Pass |
| 22w24a | fabric | Pass | Pass |
| 1.21.9 | neoforge | Pass | Pass |
| 1.21.9 | fabric | Pass | Pass |
| 1.21.8 | neoforge | Pass | Pass |
| 1.21.8 | fabric | Pass | Pass |
| 1.21.7 | neoforge | Pass | Pass |
| 1.21.7 | fabric | Pass | Pass |
| 1.21.6 | neoforge | Pass | Pass |
| 1.21.6 | fabric | Pass | Pass |
| 1.21.5 | neoforge | Pass | Pass |
| 1.21.5 | fabric | Pass | Pass |
| 1.21.4 | neoforge | Pass | Pass |
| 1.21.4 | fabric | Pass | Pass |
| 1.21.3 | neoforge | Pass | Pass |
| 1.21.3 | fabric | Pass | Pass |
| 1.21.2 | neoforge | Pass | Pass |
| 1.21.2 | fabric | Pass | Pass |
| 1.21.11 | neoforge | Pass | Pass |
| 1.21.11 | fabric | Pass | Pass |
| 1.21.10 | neoforge | Pass | Pass |
| 1.21.10 | fabric | Pass | Pass |
| 1.21.1 | neoforge | Pass | Pass |
| 1.21.1 | fabric | Pass | Pass |
| 1.21 | neoforge | Pass | Pass |
| 1.21 | fabric | Pass | Pass |
| 1.20.4 | neoforge | Pass | Pass |
| 1.20.4 | fabric | Pass | Pass |
| 1.20.1 | forge | Pass | Pass |
| 1.20.1 | fabric | Pass | Pass |
| 1.20 | forge | Pass | Pass |
| 1.20 | fabric | Pass | Pass |
| 1.19.4 | fabric | Pass | Pass |
| 1.19.3 | fabric | Pass | Pass |
| 1.19.2 | forge | Pass | Pass |
| 1.19.2 | fabric | Pass | Pass |
| 1.19.1 | forge | Pass | Pass |
| 1.19.1 | fabric | Pass | Pass |
| 1.19 | forge | Pass | Pass |
| 1.19 | fabric | Pass | Pass |
| 1.18.2 | fabric | Pass | Pass |
| 1.18.1 | forge | Pass | Pass |
| 1.18.1 | fabric | Pass | Pass |
| 1.18 | forge | Pass | Pass |
| 1.18 | fabric | Pass | Pass |
| 1.17.1 | forge | Pass | Pass |
| 1.16.5 | forge | Pass | Pass |
| 1.12.2 | forge | Pass | Pass |
| 1.18.2 | forge | Pass | Pass |

1.21.2 Jade 16.0.x attempts to load ClientLevel on a dedicated server. Required-dependency tests for both loaders pass without Jade; the original failures are retained.
