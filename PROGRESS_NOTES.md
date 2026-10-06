# ResQNet Progress & Refactoring Notes

This log documents the incremental transition of ResQNet from a half-wired prototype into a 100% complete, fully tested, demo-ready project.

---

## Phase 1 — Build, Tests & Layout (P0)
- **Deleted Prototype Files:** Removed 7 leftover prototype files calling obsolete APIs (`DatasetLoader.java`, `Environment.java`, `Metrics.java`, `ScenarioBuilder.java`, `Simulator.java`, `ConsoleMenu.java`, `Display.java`).
- **Fixed BFS Test Geometry:** Adjusted `BFSRouterTest` spacing from `25m` to `50m` (over 350m scenario width) so adjacent nodes link (`50m <= 60m`) while skip-one pairs do not (`100m > 60m`), restoring a true 5-hop chain test.
- **Rewrote Dijkstra Detour Test:** Configured geometry `S(0,0)`, `D(59,0)`, `M(30,40)` with `RubbleField(30,0, radius=8, density=0.9)` on segment S–D. Verified exact detour hop list `["S", "M", "D"]`.
- **Fixed GUI Event Log Layout:** Replaced colliding `BorderLayout.SOUTH` / `PAGE_END` calls in `MeshSimFrame` with a nested `JPanel` containing `logScroll` in `CENTER` and `statusBar` in `SOUTH`. Bounded `protocolBox` max size in `ControlPanel`.
- **Hygiene:** Cleaned git index by removing cached `out/` class files, regenerated `sources.txt`, and added `.gitignore`.

---

## Phase 2 — End-to-End System Wiring (P1)
- **Real Message Delivery:** Added `Simulator.sendMessage(srcId, dstId, router)` walking hop lists on virtual threads (`Thread.ofVirtual()`), putting `Message` instances into `PacketQueue` inboxes with 150ms delays. `NodeWorker` threads process messages and publish `FORWARD` and `DELIVERED` events.
- **GUI Send & Route Highlighting:** Wired **Send Message** button with node dropdowns (preselecting map-clicked node), router creation for all 6 protocols, cyan route highlighting on `MapPanel`, and exception dialogs on failure.
- **GUI Pause & File Exports:** Wired **Pause** button to toggle `SimulationClock.pauseClock()`/`resumeClock()`. Implemented File menu actions for CSV loading (`DatasetLoader`), DB save/load (`JdbcScenarioDao`), report export (`ReportWriter`), and CSV export (`CsvExporter`).
- **CLI Upgrades:** Upgraded `ConsoleMenu` with live mode execution, CSV loading, DB persistence, node census formatting (`MetricsAggregator`), and weakest battery summaries.
- **Shared Demo Factory:** Consolidated demo scenarios into `DemoScenarios.demo()`.

---

## Phase 3 — Syllabus Evidence Alignment (P2)
- **EnumMap & EnumSet:** Integrated `EnumMap<NodeType, Integer>` census and `EnumSet<NodeType>` mobile type filtering in `MetricsAggregator`.
- **ThreadGroup Management:** Passed `coreGroup` ThreadGroup from `Simulator` to `SimulationClock`, `MobilityEngine`, and `BatteryDrainer` constructors (`super(group, name)`), shutting down all core threads via `coreGroup.interrupt()`.
- **Radio Realism:** Extended `PathLossModel` with configurable terrain exponent, seeded static `Random`, and 4 dB Gaussian shadowing (`setSeed(long)` and `setShadowingEnabled(boolean)`).
- **Partitioned Network Test:** Built `PartitionedNetworkTest` demonstrating end-to-end router failure across a `FireRegion` wall (`attenuation 0.0`) versus `EpidemicRouter` store-carry-forward delivery upon carrier mobility.

---

## Phase 4 — Batch Runner & Network Partition Analysis
- **BatchRunner:** Implemented headless automated experiment runner evaluating all 6 protocols across scenario CSVs and random seeds, recording run metrics to SQLite (`SimRunRecord`) and exporting `batch_comparison.csv`.
- **Partition Detector & Relay Placement:** Added `NetworkPartitionDetector` for connected components and `RelayPlacementAdvisor` for greedy relay marker suggestions drawn on `MapPanel`.
- **CLI & Fat Jar Integration:** Exposed `--batch <scenarios-dir> <runs>` and `--seed=<value>` options in `Main.java`.

---

## Phase 5 — Documentation, Screenshots & Verification
- **Javadoc & Report Corrections:** Verified clean javadoc build and generated `REPORT_CORRECTIONS.md` for PDF report updates.
- **Screenshots:** Rendered 8 high-resolution screenshot PNG files in `./screenshots/` capturing real Swing frame states.
- **Verification:** Verified `mvn test` (12/12 passing) and `./build.sh` (producing `target/mesh-sim.jar`).
