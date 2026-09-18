# Post-Disaster Emergency Mesh Network Simulator — Final Execution Plan

**CS5304 Java Programming · CO1–CO5**

From the current console prototype to a complete Java 21 application covering every syllabus concept: OOP, inheritance & interfaces, exceptions & I/O, threads & generics, collections & JDBC, and Swing.

- **Repository:** [lathika-mohan/Post-Disaster-Emergency-Mesh-Network-Simulator-Java](https://github.com/lathika-mohan/Post-Disaster-Emergency-Mesh-Network-Simulator-Java)
- **Stack:** Pure Java — JDK standard library, plus one required JDBC driver jar

## Contents

1. [Where the project stands](#1--where-the-project-stands)
2. [Phase 0 — Setup](#2--phase-0--environment--project-setup)
3. [Phase 1 — Model refactor on Java 21](#phase-1)
4. [Phase 2 — Exceptions & I/O](#phase-2)
5. [Phase 3 — Generics](#phase-3)
6. [Phase 4 — Collections framework](#phase-4)
7. [Phase 5 — Five routing protocols](#phase-5)
8. [Phase 6 — Threads, mobility, virtual threads](#phase-6)
9. [Phase 7 — Radio & energy realism](#phase-7)
10. [Phase 8 — JDBC & DAO layer](#phase-8)
11. [Phase 9 — The Swing application](#phase-9)
12. [Phase 10 — Metrics, experiments, export](#phase-10)
13. [Phase 11 — Testing, docs, submission](#phase-11)
14. [Complete syllabus coverage checklist](#14--complete-syllabus-coverage-checklist)
15. [Optional features beyond the syllabus](#15--optional-features-beyond-the-syllabus)
16. [Final file tree](#16--final-file-tree)
17. [Six-week schedule](#17--six-week-schedule)
18. [Demo script & viva questions](#18--demo-script-and-likely-viva-questions)
19. [Honest engineering assessment](#19--honest-engineering-assessment)

---

## 1 · Where the project stands

The current build is a console-only simulator: node/obstacle model, distance-based graph with line-of-sight blocking, BFS and Dijkstra routing, a first-order energy model with self-healing, CSV loading and metrics — roughly 30–35% of its own design document. That is solid Unit 1–3 material, but it exercises none of Unit 4 (threads, generics), none of Unit 5's JDBC/DAO or collections depth, and nothing of CO5 — the Swing outcome, the only K6 "create" outcome in the course and therefore the highest-weighted one.

| Syllabus unit | Outcome | In the repo now | What this plan adds |
|---|---|---|---|
| 1 · OOP & fundamentals | CO1 | Classes, enums, encapsulation, arrays | Javadoc, deliberate access specifiers, static factories, StringBuilder report builder |
| 2 · Inheritance & interfaces, Java 21 | CO2 | Router interface, two implementations | Abstract `AbstractRouter`, sealed `Obstacle` hierarchy, records, pattern matching, record patterns, cloning, inner/anonymous classes |
| 3 · Exceptions & I/O | CO3 | CSV read, some try/catch | Custom exception hierarchy, byte + character streams, file logger, JSON/CSV export, try-with-resources |
| 4 · Threads & generics | CO4 | Nothing — single-threaded loop | Simulation clock, one virtual thread per node, blocking queues, synchronized graph rebuild, daemon thread, thread groups, structured concurrency, generic repository/event bus |
| 5 · Collections & JDBC | CO5 (partly) | List/Map basics | EnumMap, TreeSet, PriorityQueue, LinkedHashSet, explicit Iterator, SQLite via JDBC, full DAO layer |
| 5 · Swing | CO5 (K6) | Console only | Full Swing desktop app: live map, animated packets, control rail, tables, charts, editor |

> Do not throw away the console app. Keep `ConsoleMenu` working and add the GUI beside it — a single `Main` that takes `--cli` or `--gui`. Two front-ends over one simulation core is the cleanest possible demonstration of interface-driven design, and it gives a fallback if a projector fails.

---

## 2 · Phase 0 — Environment & project setup

*Half a day. Everything after this assumes it is done.*

1. **Install JDK 21 (LTS).** Virtual threads, record patterns and pattern matching for switch are all final in 21 and named explicitly in the syllabus. Verify with `java -version` and `javac -version` — both must say 21.
2. **Move to Maven.** Create `pom.xml` with `maven.compiler.release` 21, dependency `org.xerial:sqlite-jdbc`, and `org.junit.jupiter:junit-jupiter` in test scope. Keep `build.sh` / `run.sh` as thin wrappers so the README instructions still hold — and also keep one manual `javac -d out @sources.txt` path documented, so "compilation and execution process" (a named Unit 1 bullet) is demonstrated without a build tool hiding it.
3. **Branch per phase.** `git checkout -b phase-02-exceptions` and so on, merging to `main` when the phase's acceptance test passes. The commit history becomes the project report.
4. **Create the three new packages now:** `com.meshsim.persistence`, `com.meshsim.gui`, `com.meshsim.concurrent`.
5. **Turn on javadoc generation** (`mvn javadoc:javadoc`). Unit 1 lists javadoc comments explicitly.
6. **Update the README** from "no external dependencies" to "JDK standard library plus a JDBC driver" — JDBC requires a driver jar; the JDK's embedded database (JavaDB/Derby) was removed in JDK 9, so this is unavoidable and worth stating honestly rather than glossing over.

---

<a id="phase-1"></a>
## Phase 1 — Rebuild the model on Java 21 language features
`CO1` `CO2` · Unit 1–2 · ~2 days

The model layer is where the "Features of Java 21" bullets in Units 2 and 5 get demonstrated. Rewrite it before anything else depends on it.

**What changes**

- `Point` becomes a record: `record Point(double x, double y)` with a `distanceTo` method.
- `Obstacle` becomes a sealed interface permitting `FloodZone`, `FireRegion`, `RubbleField`, `CollapsedBuilding` — each a record carrying its own geometry and signal-attenuation factor. Sealing forces the compiler to demand every switch handle every type.
- Line-of-sight blocking moves into a pattern-matching switch over that sealed type, using record patterns to destructure.
- `Node` stays mutable (battery state changes) but gets a `NodeSnapshot` record for safe hand-off to the GUI and database.
- `Scenario` implements `Cloneable` with a deep `clone()` — clone a scenario before each experiment so comparisons start from identical state.
- On `Node`, deliberately exercise all four access specifiers: private fields, a package-private helper only the network package calls, a protected method subclasses override, and a public API — with a comment explaining each choice.
- Add a static factory `Node.spawn(...)` backed by a private `static int nextId` counter, and move magic numbers in `PathLossModel` into `public static final` constants — the Unit 1 "static members" bullet, done deliberately rather than incidentally.
- Override `toString()`, `equals()`, `hashCode()` on `Node` and `Link` with a comment noting these override `java.lang.Object` — the syllabus names "the Object Class" as its own bullet under inheritance.

```java
public sealed interface Obstacle
    permits FloodZone, FireRegion, RubbleField, CollapsedBuilding {
    boolean blocks(Point a, Point b);
    double attenuationFactor();
}

public record FloodZone(Point centre, double radius, double depthMetres)
    implements Obstacle { /* ... */ }

// Pattern matching for switch + record patterns (Unit 2 / Unit 5)
static String describe(Obstacle o) {
    return switch (o) {
        case FloodZone(Point c, double r, double d) when d > 2.0 ->
            "Deep flood r=" + r + " at " + c;
        case FloodZone f -> "Shallow flood, radius " + f.radius();
        case FireRegion f -> "Fire front, spread " + f.spreadRate();
        case RubbleField r -> "Rubble, density " + r.density();
        case CollapsedBuilding b -> "Collapse, " + b.floors() + " floors";
    }; // no default needed - the interface is sealed
}
```

**Acceptance test**
Add a fifth obstacle type to the `permits` clause and confirm the project fails to compile until every switch handles it. That compile error is the proof the design works.

---

<a id="phase-2"></a>
## Phase 2 — Exception hierarchy and the I/O layer
`CO3` · Unit 3 · ~2 days

Unit 3 wants a user-defined exception hierarchy, not one catch-all class. Model failures the way a real network stack does.

```
com.meshsim.exception
├── MeshSimulationException (checked, root)
│   ├── NodeUnreachableException        + sourceId, destId, attemptedHops
│   ├── NetworkPartitionedException     + the two component sizes
│   ├── DatasetFormatException          + line number, offending token
│   └── PersistenceException            wraps SQLException
└── InvalidTopologyException (unchecked, extends IllegalStateException)
    └── DuplicateNodeIdException
```

Rule of thumb for the report: recoverable and caused by the environment → checked; a programming mistake → unchecked. A route that cannot be found is a legitimate simulation outcome, so it is checked. Two nodes sharing an ID is a bug, so it is not.

**I/O work in this phase**

- **Character streams** — `BufferedReader` / `PrintWriter` for the CSV loader and report writer, all inside try-with-resources.
- **Byte streams** — `ObjectOutputStream` over `FileOutputStream` to save/restore a whole simulation state as a `.mesh` binary snapshot.
- **Console I/O** — keep the existing `Scanner` menu.
- **A file logger** — `SimulationLogger` appending timestamped events to `logs/run-<timestamp>.log`; use a synchronized `StringBuffer` here specifically (not `StringBuilder`) since multiple node threads (Phase 6) may log concurrently — a deliberate, defensible use of the syllabus's named `StringBuffer` class.
- Use Java 21's newer `String` methods (`strip`, `repeat`, `lines`, `formatted`, `isBlank`) in the console box-drawing and report formatter — a named Unit 3 "Enhancements of Java 21" bullet.
- Validate the CSV strictly: wrong column count, non-numeric coordinates, unknown node type, negative energy each throw `DatasetFormatException` naming the line number.

**Acceptance test**
Feed the loader a deliberately corrupted CSV with an error on line 7. The program must report "line 7" and the bad token, and must not crash.

---

<a id="phase-3"></a>
## Phase 3 — Generic classes, generic methods, bounded types
`CO4` · Unit 4 · ~1 day

Generics are easy marks only if they are real — used with two or three different types, not just one.

- `Repository<T extends Identifiable>` — an in-memory store keyed by ID, used for nodes, obstacles and messages. The bound is the "bounded types" example.
- `SimulationEvent<T>` and `EventBus<E extends SimulationEvent<?>>` — publish/subscribe so the GUI, logger and metrics collector all listen to the same event stream without knowing about each other.
- A generic method `<T extends Comparable<T>> List<T> topK(Collection<T> c, int k)` for "five weakest batteries", "three busiest relays".
- Write one report note on type erasure — why `new T[]` is illegal — since "restrictions and limitations" is a named Unit 4 bullet and a likely viva question.

```java
public class Repository<T extends Identifiable> implements Iterable<T> {
    private final Map<String, T> items = new LinkedHashMap<>();

    public void add(T item) {
        if (items.putIfAbsent(item.id(), item) != null)
            throw new DuplicateNodeIdException(item.id());
    }

    public Optional<T> find(String id) { return Optional.ofNullable(items.get(id)); }

    @Override public Iterator<T> iterator() { return items.values().iterator(); }
}
```

---

<a id="phase-4"></a>
## Phase 4 — Use the collections framework deliberately
`CO5` · Unit 5 · ~1 day

Unit 5 names specific classes. Give each one a job where it is the right choice, so it can be defended rather than recited.

| Class | Where it goes | Why it's right |
|---|---|---|
| `LinkedHashMap` | Node repository | Insertion order preserved, so map and rendered map agree |
| `PriorityQueue` | Dijkstra frontier; message queue | Comparator on cost, then triage priority |
| `EnumMap` | Per-`NodeType` counters and colours | Array-backed, keys are a known enum |
| `TreeSet` (declared as `SortedSet`) | Battery leaderboard | Always-sorted; programming to the interface, not the implementation |
| `LinkedHashSet` | Visited set in BFS | O(1) contains, deterministic replay order |
| `ArrayDeque` | BFS frontier | Faster than `LinkedList` as a queue |
| `LinkedList` | Per-node packet history buffer | Frequent head/tail operations |
| `EnumSet` | Active obstacle kinds; enabled protocols in GUI | Compact bitset for enum members |
| `Arrays` utility | Metrics aggregator | `sort`, `binarySearch`, `fill`, `copyOf` for medians/percentiles |

Implement `equals` / `hashCode` / `compareTo` on `Node` and `Link` — without it a `TreeSet` silently misbehaves. Keep one explicit `Iterator` loop with `it.remove()` (sweeping dead nodes) to contrast with for-each, which throws `ConcurrentModificationException` there.

---

<a id="phase-5"></a>
## Phase 5 — Five routing protocols behind one interface
`CO2` · Unit 2 · ~3 days

The intellectual core of the project. `AbstractRouter` holds shared machinery (hop accounting, energy callbacks, timing) as a final template method; each subclass implements only its search. Each subclass's constructor calls `super(net)` explicitly.

1. **BFS** — done. Minimum hop count, ignores link quality.
2. **Dijkstra** — done. Weight each link by `1 / linkQuality` or transmission energy cost, not distance alone.
3. **AODV (reactive)** — flood a route request, first RREQ to reach the destination wins, RREP walks back the reverse path, cache the route with a lifetime.
4. **DSR (source routing)** — the full path travels in the packet header. Larger packets, no per-node state.
5. **Epidemic / store-carry-forward** — when no end-to-end path exists, a node holds the message and forwards it when a mobile node wanders into range. This is what makes a partitioned network still deliver, and it is the demo highlight.

Add an energy-aware Dijkstra variant where link weight includes the receiving node's remaining battery, routing around nodes about to die. Comparing it against plain Dijkstra on network-lifetime is a real result to present.

```java
public abstract class AbstractRouter implements Router {
    protected final MeshNetwork network; // protected member (Unit 1)
    protected AbstractRouter(MeshNetwork network) { this.network = network; }

    @Override
    public final Route findRoute(String src, String dst) throws NodeUnreachableException {
        long t0 = System.nanoTime();
        List<String> hops = search(src, dst);
        if (hops.isEmpty()) throw new NodeUnreachableException(src, dst);
        return new Route(hops, protocolName(), System.nanoTime() - t0);
    }

    protected abstract List<String> search(String src, String dst);
    public abstract String protocolName();
}

public final class AODVRouter extends AbstractRouter {
    public AODVRouter(MeshNetwork net) { super(net); } // explicit super() call
    // ...
}
```

Also implement `interface MobileRouter extends Router` adding `onNodeMoved()`, implemented only by the epidemic router — a real use of the "extending interfaces" bullet.

**Acceptance test**
Build a scenario with two clusters separated by a flood zone and no bridging node. BFS, Dijkstra, AODV and DSR must all fail with `NetworkPartitionedException`; epidemic routing must deliver once a mobile relay crosses the gap.

---

<a id="phase-6"></a>
## Phase 6 — Make the simulation live: threads, mobility, virtual threads
`CO4` · Unit 4 · ~4 days — the hardest phase

Turn the menu-driven simulator into a running world with a clock. This is also what makes the GUI worth building.

| Thread | Kind | Job | Unit 4 bullet |
|---|---|---|---|
| `SimulationClock` | Platform (`extends Thread`) | Advances ticks, fires `TickEvent` | Thread lifecycle, sleep, interrupt |
| One per node | Virtual (`Runnable`) | Receives from inbox queue, forwards, drains battery | Virtual threads |
| `BatteryDrainer` | Daemon | Idle-power drain every tick; dies with the app | Daemon threads |
| `MobilityEngine` | Platform | Random Waypoint movement, triggers graph rebuild | Thread groups |
| Route discovery fan-out | `StructuredTaskScope` | Runs all five protocols concurrently, compares results | Structured concurrency |

Use both thread-creation styles the syllabus implies: `SimulationClock extends Thread` directly, while `NodeWorker implements Runnable` — deliberately, and say so in the report. Group the three named platform threads (`SimulationClock`, `MobilityEngine`, `BatteryDrainer`) under a `ThreadGroup("simulation-core")` and use it once, to interrupt all three together on shutdown — note in the report that `ThreadGroup` is largely superseded by structured concurrency for anything more complex, so this is a small, deliberate use rather than the backbone of the design.

**Three concurrency problems to solve — and document**

- **Shared graph mutation.** The mobility engine rewrites the adjacency list while routers read it. Guard with `ReentrantReadWriteLock`, or swap an immutable graph reference atomically. Also use plain `synchronized` deliberately in one simple, low-contention spot — `EnergyModel.drain(Node n)` — and explain in the report why the lock was chosen for the graph (finer-grained read/write contention) while `synchronized` sufficed for energy draining (simple mutual exclusion, low contention).
- **Inter-thread communication.** Each node owns a `BlockingQueue<Packet>` inbox; forwarding is `put`, the node thread blocks on `take`. Implement one small `wait`/`notify` handshake too (the pause/resume control is ideal), so both the classic and the modern mechanism are demonstrated.
- **Clean shutdown.** Interrupt the clock, let node threads observe the interrupt and drain, join with a timeout. Note in the report that `Thread.stop()` is a removed/deprecated API — the reason shutdown must use cooperative interruption instead.

```java
// One virtual thread per node - 500 blocked nodes cost almost nothing,
// where 500 platform threads would not.
for (Node n : nodes) {
    Thread.ofVirtual().name("node-" + n.getId()).start(() -> {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Packet p = n.inbox().take();
                energyModel.onReceive(n, p);
                if (!n.getId().equals(p.destination())) forward(n, p);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    });
}

// Structured concurrency: race all protocols, compare fairly
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var bfs  = scope.fork(() -> new BFSRouter(net).findRoute(s, d));
    var dij  = scope.fork(() -> new DijkstraRouter(net).findRoute(s, d));
    var aodv = scope.fork(() -> new AODVRouter(net).findRoute(s, d));
    scope.join().throwIfFailed();
    metrics.compare(bfs.get(), dij.get(), aodv.get());
}
```

**Mobility model**
Random Waypoint: each mobile node picks a random destination and speed, moves toward it each tick, pauses on arrival, repeats. Rescue teams move faster, survivors slower, dropped relays are static. Recompute links after every movement step — the network will visibly heal, split and reform on its own.

**Acceptance test**
Run 200 nodes for 1000 ticks with no `ConcurrentModificationException` and no deadlock, then press pause, resume, and stop and confirm every thread exits (check with a thread dump).

---

<a id="phase-7"></a>
## Phase 7 — Realistic radio and energy
`CO1` · ~1.5 days

Replace the binary in-range/out-of-range test with a log-distance path loss model:

```
PL(d) = PL(d0) + 10·n·log10(d/d0) + X
```

where `n` is the path-loss exponent (2.0 open ground, 2.7 light rubble, 3.5 dense rubble, 4.0+ collapsed structure) and `X` is a Gaussian shadowing term. A link exists when received power exceeds the receiver sensitivity threshold; link quality is the margin above threshold, and that quality becomes the Dijkstra weight.

Energy: first-order radio model — transmit cost scales with distance² (or distance⁴ beyond a crossover), receive cost is flat, idle listening drains slowly, sleep drains barely at all. Then add:

- Solar-recharging relays that regain charge on a day/night cycle.
- Duty cycling — nodes sleep part of each tick, saving power at the cost of latency. Sweep the duty cycle and plot lifetime against delivery ratio — a real, presentable result.
- Fire spread — the fire obstacle grows each tick, progressively killing links and nodes in its path.

---

<a id="phase-8"></a>
## Phase 8 — JDBC and the DAO layer
`CO5` · Unit 5 · ~2 days

Use SQLite (single `mesh.db` file, zero install, one jar). This is the one dependency in the whole project that cannot be pure JDK — the syllabus asks for JDBC and DAO, and no JVM-standard embedded database exists since JavaDB was dropped in JDK 9.

```sql
CREATE TABLE scenario (id INTEGER PRIMARY KEY, name TEXT, width REAL, height REAL, created_at TEXT);

CREATE TABLE node (id INTEGER PRIMARY KEY, scenario_id INT, node_key TEXT, x REAL, y REAL,
    type TEXT, range REAL, energy REAL,
    FOREIGN KEY(scenario_id) REFERENCES scenario(id));

CREATE TABLE obstacle (id INTEGER PRIMARY KEY, scenario_id INT, kind TEXT, geometry TEXT, severity REAL);

CREATE TABLE sim_run (id INTEGER PRIMARY KEY, scenario_id INT, protocol TEXT, started_at TEXT,
    ticks INT, pdr REAL, avg_hops REAL, energy_used REAL, lifetime_ticks INT);

CREATE TABLE message_log (id INTEGER PRIMARY KEY, run_id INT, tick INT, src TEXT, dst TEXT,
    priority TEXT, delivered INT, hops INT, latency_ms REAL);
```

**DAO layer**, one interface per aggregate, one JDBC implementation each: `ScenarioDao`, `NodeDao`, `ObstacleDao`, `SimRunDao`, `MessageLogDao` — each with `save`, `findById`, `findAll`, `delete`.

- `ConnectionFactory` holding the single connection or a small pool.
- Always `PreparedStatement`, never string concatenation — note SQL injection risk explicitly in the report.
- Saving a scenario with its nodes is one transaction: `setAutoCommit(false)`, batch inserts with `addBatch`/`executeBatch`, commit, roll back in the catch. Wrap any `SQLException` in `PersistenceException`.

What this unlocks as a feature, not just a checkbox: a run history browser. Every simulation writes a row to `sim_run`; the GUI lists past runs in a table, and two rows can be selected and compared on identical scenarios.

**Acceptance test**
Save a 30-node scenario, close the app, reopen, reload the scenario from the database and confirm every node position and battery level matches. Force a failure mid-batch and confirm the rollback leaves no partial scenario behind.

---

<a id="phase-9"></a>
## Phase 9 — The Swing application
`CO5` `K6` · Unit 5 · ~5 days — the highest-value phase

This is the outcome the syllabus grades at "create" level, so it deserves the most time. The GUI is a thin view over the simulation core, subscribing to the event bus from Phase 3 and containing no simulation logic itself.

**Window layout**

```
┌─────────────────────────────────────────────────────────────┐
│ Menu: File  Scenario  Simulation  Protocol  View  Help       │
├───────────────┬─────────────────────────────┬───────────────┤
│ Control rail  │                             │ Node inspector│
│               │         MapPanel            │               │
│ ▸ Scenario ▾  │   Graphics2D canvas         │ Live metrics  │
│ ▸ Protocol ▾  │   • nodes coloured by type  │  ▸ PDR 0.92   │
│ ▸ Speed ───── │   • links, thickness = SNR  │  ▸ Hops 3.4   │
│ ▸ [Start]     │   • obstacles, translucent  │  ▸ Alive 27/30│
│ ▸ [Pause]     │   • packet dot in flight    │               │
│ ▸ [Step]      │   • battery ring per node   │ Battery chart │
│ ▸ [Add node]  │                             │ (custom paint)│
│ ▸ [Add flood] │                             │               │
├───────────────┴─────────────────────────────┴───────────────┤
│ Event log (JTextArea, auto-scroll) | status bar: tick 428    │
└─────────────────────────────────────────────────────────────┘
```

**Component checklist**

- `JFrame` with `BorderLayout`; `JSplitPane` between map and inspector for resizing.
- `MapPanel extends JPanel`, overriding `paintComponent(Graphics g)`. Cast to `Graphics2D`, enable antialiasing, apply an `AffineTransform` for pan/zoom. Draw order: obstacles → links → packet trail → nodes → labels.
- `MouseListener` / `MouseMotionListener`: click selects a node, drag moves it, shift-drag draws a new obstacle, scroll wheel zooms.
- `javax.swing.Timer` at ~30 fps calling `repaint()` — fires on the EDT, exactly where painting must happen.
- Simulation runs on its own threads (Phase 6). Never touch Swing components from those threads — marshal every update with `SwingUtilities.invokeLater`, use `SwingWorker` for long batch jobs, publish progress to a `JProgressBar`. This EDT rule is the most likely GUI viva question.
- `JTable` with custom `AbstractTableModel` for nodes and run history. `JComboBox`, `JSlider`, `JToolBar`, `JMenuBar` with mnemonics and accelerators.
- `JFileChooser` with `FileNameExtensionFilter` for CSV import/report export. `JOptionPane` for confirmations and caught-exception messages.
- Custom-painted charts (battery histogram, delivery-ratio-over-time) with `Graphics2D` — no charting library, so this stays inside "pure Java."
- Anonymous inner classes and lambdas for listeners — the Unit 2 "inner classes" bullet paying off directly.

```java
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    g2.transform(viewTransform);

    for (Obstacle o : snapshot.obstacles()) drawObstacle(g2, o);
    for (Link l : snapshot.links()) {
        g2.setStroke(new BasicStroke((float) (0.5 + 2.5 * l.quality())));
        g2.setColor(linkColour(l.quality()));
        g2.draw(new Line2D.Double(l.a().pos(), l.b().pos()));
    }
    for (NodeSnapshot n : snapshot.nodes()) drawNode(g2, n);
    if (packetInFlight != null) drawPacket(g2, packetInFlight);
    g2.dispose();
}
```

When a route is found, interpolate the packet's position along each hop over ~400 ms, repainting each frame. When a hop fails because a node just died, flash it red and show the reroute — this is what turns a correct project into a memorable one.

**Acceptance test**
Resize the window, zoom, drag a node into a flood zone and watch its links break, send a message across nine hops, and pause mid-flight. No frozen UI at any point.

---

<a id="phase-10"></a>
## Phase 10 — Metrics, experiments and export
`CO3` `CO5` · ~2 days

- **Batch mode** — every protocol × every scenario × N repetitions, no GUI, writing to the database. Virtual threads make 500 runs finish in seconds.
- **Comparison table** — protocol × metric, mean and standard deviation, in a `JTable`, exported to CSV.
- **Report export** — a `StringBuilder`-assembled text/HTML report per run: scenario description, topology summary, per-message log, metric table, conclusions.
- **Partition detection** — connected-components analysis each tick, reporting isolated islands and unreachable survivors.
- **Relay placement suggestion** — greedily pick the position that reconnects the most isolated survivors, drawn on the map as a "drop a relay here" marker.

---

<a id="phase-11"></a>
## Phase 11 — Testing, documentation, submission
All COs · ~2 days

- JUnit 5 tests for the parts with a right answer: graph construction with/without obstacles, each router on a hand-drawn 6-node graph with a known shortest path, energy arithmetic, CSV parsing including all four failure cases, each DAO against an in-memory database.
- Javadoc on every public type and method — `@param`, `@return`, `@throws`. Generate the HTML and commit it.
- Rewrite the README: architecture diagram, build/run for both front-ends, GUI screenshots, metric definitions, and a table mapping each syllabus unit to the classes that demonstrate it — evaluators look for exactly that table.
- Update `PROGRESS_NOTES.md` from "30–35%" to a completed checklist.
- Package it: `mvn package` into a runnable fat jar so `java -jar mesh-sim.jar` starts the GUI with no setup.

---

## 14 · Complete syllabus coverage checklist

Every remaining named syllabus bullet not already assigned to a phase above, with its exact slot. After this table, every line item in the syllabus docx maps to a real line of code or a specific paragraph in the report — not a name-check.

### Unit 1

| Bullet | Slot |
|---|---|
| Characteristics of Java / the Java environment | One report paragraph: the fat jar running unmodified on any evaluator's OS is the WORA demonstration |
| Compilation & execution process | `BUILD.md` section: `javac` → `.class` bytecode → classloading → JVM execution, plus the manual `javac` path kept alongside Maven |
| Operators, control flow | switch expression with `yield` for node-type colour mapping; one bitwise flag field (`int capabilities`, combined with `\|` / `&`) for node features |

### Unit 2

| Bullet | Slot |
|---|---|
| Constructors in subclasses | Explicit `super(net)` call in each router subclass (Phase 5) |
| Differences between classes and interfaces | Report paragraph: `Router` interface vs `AbstractRouter` abstract class, and why both are needed |
| Final methods and classes | `Route` and record types are `final`; `findRoute()` is `final` in `AbstractRouter` (template method) |

### Unit 3

| Bullet | Slot |
|---|---|
| New Java 21 String methods | `strip`, `repeat`, `lines`, `formatted`, `isBlank` in report/console formatting (Phase 2) |
| Deprecated / removed APIs | Report paragraph on `Thread.stop()` removal as the reason shutdown uses interruption (Phase 6) |
| Foreign Function & Memory API | Optional stretch: load a binary terrain grid with `MemorySegment`/`Arena` instead of CSV — flag as optional, not required for full marks |

### Unit 4

| Bullet | Slot |
|---|---|
| `synchronized` keyword | Deliberate use on `EnergyModel.drain()`, alongside the read/write lock on the graph (Phase 6) |
| Thread groups | `ThreadGroup("simulation-core")` for the three named platform threads, used once for coordinated shutdown (Phase 6) |
| Multitasking vs multithreading | Report paragraph: process-level (optional socket mode, two JVMs) vs thread-level (everything inside one JVM) |
| Both thread-creation styles | `SimulationClock extends Thread`; `NodeWorker implements Runnable` — used deliberately, not interchangeably (Phase 6) |

### Unit 5

| Bullet | Slot |
|---|---|
| Autoboxing, for-each | Already pervasive — call it out explicitly in the report rather than leaving it implicit |
| `EnumSet` | Active obstacle kinds; enabled protocols in the GUI (Phase 4) |
| `LinkedList` | Per-node packet history buffer (Phase 4) |
| `SortedSet` interface | Battery leaderboard declared as `SortedSet<Node>`, backed by `TreeSet` — programming to the interface (Phase 4) |
| `Arrays` utility class | `sort`, `binarySearch`, `fill`, `copyOf` in the metrics aggregator (Phase 4) |

**Honest caveat.** Hitting every checkbox is not automatically the same as the project being well-designed. Pairing `ThreadGroup` and raw `synchronized` next to a `ReentrantReadWriteLock` for a comparable job is a slightly contrived combination — a senior reviewer will correctly read it as checklist coverage unless the report explicitly frames it as a deliberate, small-scope demonstration (as the notes above do). That framing is what makes it honest rather than padded.

---

## 15 · Optional features beyond the syllabus

All pure Java, all real, all buildable — but none map to a specific syllabus concept, so treat them as stretch goals. Recommended picks are marked ★.

- **★ Message TTL and duplicate suppression** — packets expire after N hops; nodes remember recently-seen message IDs, preventing epidemic routing from flooding indefinitely.
- **★ Triage priority queue** — medical emergency beats trapped-person beats status ping, with priority inheritance under backpressure.
- **★ Multi-message concurrent traffic** — dozens of messages in flight with congestion and queue-drop metrics, so PDR numbers are not trivially 1.0.
- Coverage heat map — colour the map background by signal reachability, pure Graphics2D.
- Scenario editor with undo/redo — Command pattern over a `Deque`.
- Simulation replay — scrub backwards/forwards through a completed run using the database log and a `JSlider`.
- Night/day cycle affecting solar recharge and rescue-team speed.
- Sound alerts on node death via `javax.sound.sampled` — five lines, standard library, effective in a demo.
- Socket-based distributed mode — two JVM instances over TCP, each simulating half the mesh. Directly answers the syllabus's own Use-case 5 ("network-based and client-server applications") using `java.net`. The most ambitious item here; document as future work unless Phase 9 finishes with two weeks to spare.

---

## 16 · Final file tree

```
src/main/java/com/meshsim/
├── Main.java                --cli | --gui | --batch
├── model/                   Point, Node, NodeType, NodeSnapshot, Obstacle(sealed),
│                             FloodZone, FireRegion, RubbleField, CollapsedBuilding,
│                             Environment, Scenario(Cloneable), Message, Priority
├── network/                 Link, MeshNetwork, TopologyBuilder, PathLossModel
├── routing/                 Router, MobileRouter, AbstractRouter, BFSRouter,
│                             DijkstraRouter, AODVRouter, DSRRouter, EpidemicRouter,
│                             EnergyAwareRouter, Route, RouteCache
├── energy/                  EnergyModel, RadioProfile, SolarRecharger, DutyCycle
├── concurrent/               SimulationClock, NodeWorker, MobilityEngine,
│                             EventBus, SimulationEvent, PacketQueue
├── simulation/               Simulator, ScenarioBuilder, Metrics, MetricsCollector,
│                             BatchRunner, PartitionAnalyzer, RelayPlanner
├── persistence/              ConnectionFactory, ScenarioDao, NodeDao, ObstacleDao,
│                             SimRunDao, MessageLogDao, + Jdbc*Impl for each
├── io/                       DatasetLoader, StateSerializer, SimulationLogger,
│                             ReportWriter, CsvExporter
├── exception/                MeshSimulationException + the hierarchy in Phase 2
├── util/                     Repository<T>, Identifiable, Geometry, TopK
└── gui/                      MeshSimFrame, MapPanel, ControlPanel, InspectorPanel,
                              MetricsPanel, BatteryChart, DeliveryChart,
                              NodeTableModel, RunHistoryTableModel, ScenarioEditor,
                              Theme, SwingDispatcher
```

Roughly 70 classes. Most are 40–80 lines; the structure is what earns the design marks.

---

## 17 · Six-week schedule

| Week | Phases | You should be able to… |
|---|---|---|
| 1 | 0, 1, 2 | Load a CSV, get precise errors on bad input, see obstacle types handled by a sealed switch |
| 2 | 3, 4, 5 | Route a message five different ways and see the protocols disagree |
| 3 | 6 | Watch nodes move and the network heal itself while the clock runs |
| 4 | 7, 8 | Save and reload scenarios; run history persists across restarts |
| 5 | 9 | Demo the whole thing with a mouse |
| 6 | 10, 11, checklist | Produce comparison results, tests, docs, jar, and the completed syllabus-mapping table |

If time runs short, cut in this order: DSR, solar recharge, the charts, batch mode, the Unit 1/3 stretch items in the checklist. **Never cut the Swing phase or the DAO phase** — those are the two the syllabus grades directly.

---

## 18 · Demo script and likely viva questions

**A seven-minute demo that lands**

1. Open the GUI on the 30-node earthquake scenario. Point out survivors, rescue teams, relays, the rubble field.
2. Send a medical-priority message across the map. Let the animation run.
3. Kill the node in the middle of that path. Show the alert, the link rebuild, the automatic reroute.
4. Drop a flood zone across the map to split the network in two. Show BFS failing with a partition exception.
5. Switch to epidemic routing and start the mobility engine. Show the message carried across by a moving rescue node.
6. Open the run history table and compare protocols on delivery ratio and network lifetime.
7. Export the report and open the generated file.

**Questions to have answers ready for**

- Why virtual threads rather than a thread pool here?
- What exactly does the EDT rule require, and where does the code honour it?
- Why is `NodeUnreachableException` checked but `DuplicateNodeIdException` unchecked?
- Why a sealed interface for obstacles instead of an abstract class?
- What does the DAO pattern buy that direct JDBC calls would not?
- Where did type erasure constrain the generic code?
- Dijkstra over BFS — what is the weight, and why is it the right weight for a disaster network?
- How is `ConcurrentModificationException` avoided when the mobility engine rebuilds links mid-route?
- Why `synchronized` in one place and a `ReentrantReadWriteLock` in another, for seemingly similar problems?

One habit worth keeping for all six weeks: at the end of each phase, write three sentences in `PROGRESS_NOTES.md` about what was built, what broke, and why that choice was made. When the report is due, it is already written — and those sentences are what turn a working program into a defensible one.

---

## 19 · Honest engineering assessment

Judged purely against syllabus completeness, the plan above reaches full coverage — every named bullet in the docx maps to a specific class or report paragraph. That is a fair 10/10 on that specific axis.

Judged as production software, three gaps remain and are worth stating rather than hiding:

- **Testing is scheduled too late.** JUnit belongs threaded through every phase, not bolted on in Phase 11 — a simulator's entire value is that its numbers are trustworthy.
- **No seeded-RNG discipline.** Random Waypoint plus Gaussian shadowing plus concurrent threads means two "identical" runs can diverge. A deterministic, single-threaded, seeded mode should exist alongside the live concurrent one so protocol comparisons are reproducible.
- **Concurrency is the highest-risk phase.** One virtual thread per node sharing a mutable graph is easy to describe and easy to get subtly wrong. A plausible-looking but slightly racy simulator is worse than a correct single-threaded one — budget Phase 6 honestly and validate its output against the deterministic mode above.

None of this changes what to build. It changes the order: get a correct, deterministic core first, prove it with tests, and only then layer concurrency and the GUI on top of something already known to be right.
