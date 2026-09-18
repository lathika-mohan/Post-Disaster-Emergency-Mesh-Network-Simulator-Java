# Progress Notes

Started from a console-only prototype at roughly 30–35% of the design document
(BFS/Dijkstra routing, distance-based graph, first-order energy model, CSV loading).

## What this pass built

- **Phase 1 (model on Java 21):** `Point` as a record, `Obstacle` as a sealed
  interface with four record implementations, pattern matching for switch with
  record patterns in `Obstacle.describe()`, `Scenario.clone()` for deep cloning,
  all four access specifiers deliberately exercised on `Node`, static factory
  `Node.spawn()`.
- **Phase 2 (exceptions & I/O):** full checked/unchecked hierarchy under
  `com.meshsim.exception`; character-stream CSV loader with per-line error
  reporting; a `SimulationLogger` using `StringBuffer` deliberately for
  concurrent-safe logging; `StateSerializer` for byte-stream `.mesh` snapshots.
- **Phase 3 (generics):** `Repository<T extends Identifiable>`,
  `SimulationEvent<T>` / `EventBus`, and a `TopK` generic method with a report
  note on type erasure baked into its Javadoc.
- **Phase 4 (collections):** `BatteryLeaderboard` (`SortedSet`/`TreeSet`),
  `MetricsAggregator` (`Arrays` utility class), `LinkedHashMap`/`ArrayDeque`/
  `LinkedHashSet` used inside the routers and `MeshNetwork`.
- **Phase 5 (routing):** `Router` / `MobileRouter` interfaces, `AbstractRouter`
  template method, `BFSRouter`, `DijkstraRouter`, `EnergyAwareDijkstraRouter`,
  `AODVRouter` (flood + route cache), `DSRRouter`, `EpidemicRouter`
  (store-carry-forward, the one `MobileRouter` implementation).
- **Phase 6 (concurrency):** `SimulationClock` (`extends Thread`), `NodeWorker`
  (`implements Runnable`, run on a virtual thread per node), `BatteryDrainer`
  (daemon), `MobilityEngine` (Random Waypoint, rebuilds the graph under a
  `ReentrantReadWriteLock`), `Simulator.raceAllProtocols()` using
  `StructuredTaskScope.ShutdownOnFailure` (a JDK 21 preview API — compiled and
  run with `--enable-preview`, wired into `pom.xml` and `run.sh`).
- **Phase 7 (radio/energy):** simplified log-distance `PathLossModel`,
  first-order `EnergyModel` with a deliberately plain `synchronized` method,
  `RadioProfile` constants, `FireRegion.grow()`.
- **Phase 8 (JDBC/DAO):** SQLite schema in `ConnectionFactory`, DAO interfaces
  plus JDBC implementations for `Scenario` and `SimRun`, transactional batch
  insert with rollback in `JdbcScenarioDao.save()`.
- **Phase 9 (Swing):** `MeshSimFrame`, `MapPanel` (custom `Graphics2D`
  painting, mouse selection, battery rings), `ControlPanel`, `NodeTableModel`;
  all cross-thread updates go through `SwingUtilities.invokeLater`.
- **Phase 11 (testing):** JUnit 5 tests for `BFSRouter`, `DijkstraRouter`, and
  all four `DatasetLoader` failure cases, on hand-built small graphs.

## What broke, and why a choice was made

- `java.awt.Point` collided with `com.meshsim.model.Point` inside the GUI
  package the moment both were in scope via wildcard imports — resolved with
  an explicit import of the model's `Point`, which Java resolves in favour of
  the wildcard.
- `StructuredTaskScope` is a **preview** API on JDK 21 (it graduates later),
  so both `javac` and the runtime need `--enable-preview` explicitly — this
  is called out in `pom.xml`, `build.sh`, and `run.sh` rather than silently
  assumed.
- The sandbox used to build this scaffold has no route to Maven Central, so
  `sqlite-jdbc` and JUnit were fetched from their GitHub release assets
  instead, purely to prove the whole tree (persistence included) compiles
  end-to-end. A normal dev machine will resolve both from Maven Central via
  `pom.xml` without any of that.
- Demo scenario node spacing initially exceeded the 60m default radio range,
  so nothing routed — tightened the demo coordinates so the CLI/GUI actually
  show a connected, multi-hop network out of the box.

## Honest state of this pass

This is a working, compiling scaffold covering the syllabus concepts in every
phase — not the full 5-day-polish Swing app (no animated packet interpolation,
no custom charts, no scenario editor yet) and not the full protocol nuance
(AODV/DSR are simplified relative to the spec). Concurrency and the DAO layer
are real and tested manually via the CLI, but do not yet have the seeded-RNG
determinism mode the execution plan's honest assessment calls for. Next real
increment: batch/metrics (Phase 10), the seeded-RNG deterministic mode, and
fleshing out the Swing charts and scenario editor.
