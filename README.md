# Post-Disaster Emergency Mesh Network Simulator

CS5304 Java Programming · CO1–CO5

A disaster-response mesh network simulator: nodes (survivors, rescue teams,
relays, base stations) route messages across a field scattered with
obstacles (floods, fires, rubble, collapsed buildings) whose signal
attenuation and line-of-sight blocking change what routes are possible.
Five routing protocols run behind one interface, threads model a live,
moving network, and both a console and a Swing front-end sit over the same
simulation core.

See `EXECUTION_PLAN.md` for the full phase-by-phase design and rationale, and
`PROGRESS_NOTES.md` for what has actually been built so far and why certain
choices were made.

## Requirements

- JDK 21 (LTS) — `java -version` and `javac -version` must both say 21.
- Maven (recommended) — resolves `org.xerial:sqlite-jdbc` and JUnit 5 automatically.

## Build & run

```bash
./build.sh              # mvn package -> target/mesh-sim.jar
./run.sh --gui          # launches the Swing app (default)
./run.sh --cli          # launches the original console menu
```

`StructuredTaskScope` (used to race routing protocols concurrently) is a
**preview API** in JDK 21, so both the build and the run step pass
`--enable-preview` — already wired into `pom.xml`, `build.sh`, and `run.sh`.

### Manual compilation path (no Maven)

The syllabus separately asks for the compilation/execution process to be
demonstrable without a build tool hiding it:

```bash
mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac -d out --release 21 --enable-preview -cp lib/sqlite-jdbc.jar @sources.txt
java --enable-preview -cp "out:lib/sqlite-jdbc.jar" com.meshsim.Main --cli
```

(This requires `sqlite-jdbc.jar` to already be on disk under `lib/` — Maven
fetches it automatically, this path does not.)

## Architecture

```
src/main/java/com/meshsim/
├── Main.java            --cli | --gui entry point over one shared simulation core
├── model/                Point, Node, NodeType, Obstacle (sealed) + 4 obstacle records,
│                         Scenario (Cloneable), Message, Priority
├── network/               Link, MeshNetwork, PathLossModel
├── routing/               Router, MobileRouter, AbstractRouter, BFSRouter, DijkstraRouter,
│                         EnergyAwareDijkstraRouter, AODVRouter, DSRRouter, EpidemicRouter
├── energy/                 EnergyModel, RadioProfile
├── concurrent/             SimulationClock, NodeWorker, BatteryDrainer, MobilityEngine,
│                         EventBus, SimulationEvent, PacketQueue, Simulator
├── io/                     DatasetLoader, SimulationLogger, ReportWriter, CsvExporter, StateSerializer
├── exception/              MeshSimulationException hierarchy (checked + unchecked)
├── util/                   Repository<T>, Identifiable, TopK, BatteryLeaderboard, MetricsAggregator
├── persistence/            ConnectionFactory, ScenarioDao/JdbcScenarioDao, SimRunDao/JdbcSimRunDao
├── gui/                    MeshSimFrame, MapPanel, ControlPanel, NodeTableModel
└── cli/                    ConsoleMenu (the original console front-end, kept working)
```

## Syllabus-to-code map

| Unit | Concept | Where |
|---|---|---|
| 1 | Static members, access specifiers, Object overrides | `model/Node.java` |
| 2 | Sealed interfaces, records, pattern matching for switch | `model/Obstacle.java` and its 4 record implementations |
| 2 | Abstract classes vs interfaces, `final` template method | `routing/Router.java`, `routing/AbstractRouter.java` |
| 2 | Extending interfaces | `routing/MobileRouter.java` (implemented only by `EpidemicRouter`) |
| 3 | Custom checked/unchecked exception hierarchy | `exception/` |
| 3 | Character/byte streams, try-with-resources | `io/DatasetLoader.java`, `io/StateSerializer.java` |
| 3 | `StringBuffer` (deliberate, concurrent-safe) | `io/SimulationLogger.java` |
| 4 | Generics, bounded types, type erasure note | `util/Repository.java`, `util/TopK.java` |
| 4 | Both thread-creation styles, daemon threads, thread groups | `concurrent/SimulationClock.java` (extends Thread), `concurrent/NodeWorker.java` (Runnable), `concurrent/BatteryDrainer.java`, `concurrent/Simulator.java` |
| 4 | Virtual threads, structured concurrency | `concurrent/Simulator.java` |
| 4 | `synchronized` vs `ReentrantReadWriteLock` | `energy/EnergyModel.java` vs `network/MeshNetwork.java` |
| 5 | `TreeSet`/`SortedSet`, `Arrays` utility class | `util/BatteryLeaderboard.java`, `util/MetricsAggregator.java` |
| 5 | JDBC + DAO, transactions | `persistence/` |
| 5 (K6) | Swing desktop app | `gui/` |

## Sample data

`src/main/resources/sample-scenario.csv` is a small, well-formed dataset for
`DatasetLoader`. Corrupt copies of it (bad column count, unknown type,
non-numeric coordinate, negative energy) are exercised directly in
`DatasetLoaderTest`.

## Tests

```bash
mvn test
```

Covers `BFSRouter` and `DijkstraRouter` on hand-built graphs, and all four
named `DatasetLoader` failure cases.
