# Post-Disaster Emergency Mesh Network Simulator (ResQNet)

CS5304 Java Programming · PBL Project

A post-disaster emergency mesh network simulator written in Java 21: nodes (survivors, rescue teams, static relays, base stations) route emergency packets across a disaster area obstructed by flood zones, fire fronts, rubble fields, and collapsed buildings.

Six routing strategies (BFS, Dijkstra, EnergyAwareDijkstra, AODV, DSR, and Epidemic store-carry-forward) run over a shared simulation core. Features live thread-based movement & energy drain, SQLite persistence, Swing GUI, CLI front-end, and an automated headless `--batch` runner.

---

## Key Features

- **Java 21 Stack & Features:** Records, sealed interfaces (`Obstacle`), pattern matching for switch, virtual threads (`Thread.ofVirtual()`), and structured concurrency (`StructuredTaskScope.ShutdownOnFailure`).
- **Six Routing Protocols:**
  - BFS (shortest hop count)
  - Dijkstra (quality-weighted shortest path)
  - EnergyAwareDijkstra (steers around low-battery nodes)
  - AODV (reactive route discovery with caching)
  - DSR (source-routed path discovery)
  - Epidemic (store-carry-forward for partitioned networks)
- **Front-Ends:**
  - **Swing GUI:** Map panel with live link quality, obstacle overlays, cyan route highlighting, node table inspector, visible scrolling event log, pause/resume control, and file exports.
  - **Console Menu CLI:** Interactive CLI with live simulation mode, CSV loading, DB persistence, and census summaries.
  - **Headless Batch Mode:** Automated multi-run evaluation across protocols, scenarios, and random seeds with summary tables and CSV export.
- **Persistence & Datasets:** SQLite DB persistence via JDBC DAOs (`ScenarioDao`, `SimRunDao`), CSV loader (`DatasetLoader`), report generator (`ReportWriter`), and CSV exporter (`CsvExporter`).

---

## Build & Execution

### Prerequisites
- JDK 21 (LTS) — javac and java must support Java 21 with `--enable-preview`.
- Apache Maven 3.9+ (or use local JDK javac command).

### Command Line Options

```bash
# Build fat runnable JAR
./build.sh   # or mvn package

# Launch Swing GUI (Default)
java --enable-preview -jar target/mesh-sim.jar --gui

# Launch Console CLI
java --enable-preview -jar target/mesh-sim.jar --cli

# Launch Automated Batch Mode (N runs per combo)
java --enable-preview -jar target/mesh-sim.jar --batch src/main/resources 5

# Run with Deterministic Seed
java --enable-preview -jar target/mesh-sim.jar --seed=12345 --batch src/main/resources 5
```

---

## Unit Test Suite

Run unit tests via Maven:
```bash
mvn test
```

### Test Suite Results (12 / 12 Passing)
- `DatasetLoaderTest` (5 tests): Bad column count, unknown type, non-numeric values, negative energy, well-formed CSV loading.
- `BFSRouterTest` (2 tests): 5-hop chain geometry (50m spacing) and out-of-range unreachable node handling.
- `DijkstraRouterTest` (1 test): Detour path `S -> M -> D` around RubbleField obstacle.
- `PathLossModelTest` (2 tests): Deterministic seed matching and shadowing variations.
- `PartitionedNetworkTest` (2 tests): End-to-end router failure across FireRegion wall vs Epidemic store-carry-forward delivery upon carrier mobility.

---

## Viva Demo Script (5–10 Minute Walkthrough)

1. **Launch GUI:** Run `java --enable-preview -jar target/mesh-sim.jar --gui`.
2. **Inspect Initial Topology:** Observe 6 nodes, obstacle overlays (RubbleField & FloodZone), node table inspector, and status bar.
3. **Start Simulation:** Click **Start**. Note tick counter advancing in status bar and start event in scrolling event log.
4. **Pause & Resume:** Click **Pause** (status bar shows "Paused"), then **Resume**.
5. **Send Message & Route Highlighting:**
   - Select node **N1** on map panel.
   - Click **Send Message**.
   - Choose destination **N6** and protocol **EnergyAwareDijkstra**.
   - Observe cyan highlighted path on map panel and `FORWARD` / `DELIVERED` logs firing as virtual node threads process inboxes.
6. **File Persistence & Export:**
   - Go to **File -> Save scenario to DB**.
   - Click **File -> Export report...** to save plain-text run summary.
   - Click **File -> Export CSV...** to save node table data.
7. **Batch Mode Demonstration:**
   - Run `java --enable-preview -jar target/mesh-sim.jar --batch src/main/resources 5`.
   - Inspect console output comparison matrix and generated `batch_comparison.csv`.
