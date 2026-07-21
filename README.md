# Post-Disaster Emergency Mesh Network Simulator (Java, Console Edition)

A graph-theoretic simulator of a decentralized mesh network used for
communication during disaster response, when centralized infrastructure
(cell towers, fiber, internet backbone) has failed.

This is a **pure console (CLI) Java application** — no GUI, no external
dependencies, JDK standard library only.

## Concept

* **Nodes** are survivor devices, rescue-team devices, or dropped relays,
  each with a position, transmission range, and battery energy.
* **Links** form automatically between any two active nodes within radio
  range of each other, *unless* a disaster obstacle (flood, fire, rubble,
  building collapse) blocks the line of sight between them.
* **Routing protocols** (BFS and Dijkstra) find multi-hop paths for
  emergency messages across the resulting graph.
* **Energy model** deducts battery for every transmission/reception; nodes
  that hit 0 energy go offline and the network automatically rebuilds its
  links (self-healing).
* **Metrics** track Packet Delivery Ratio, average hop count, coverage
  ratio, network lifetime, route-recovery time, and energy efficiency —
  the same metrics defined in the project's design document.

## Project Structure

```
mesh-sim/
├── src/main/java/com/meshsim/
│   ├── Main.java                     Entry point
│   ├── model/                        Environment Layer + Node Layer
│   │   ├── Node.java
│   │   ├── NodeType.java
│   │   ├── Obstacle.java
│   │   └── Environment.java
│   ├── network/                      Communication Layer
│   │   ├── Link.java
│   │   └── MeshNetwork.java
│   ├── routing/                      Routing Layer
│   │   ├── Router.java
│   │   ├── BFSRouter.java
│   │   ├── DijkstraRouter.java
│   │   └── Route.java
│   ├── energy/                       Energy Layer
│   │   └── EnergyModel.java
│   ├── simulation/                   Orchestration + Metrics
│   │   ├── Simulator.java
│   │   ├── Metrics.java
│   │   └── ScenarioBuilder.java
│   ├── data/                         Dataset loading
│   │   └── DatasetLoader.java
│   └── ui/                           Console (Visualization Layer, text-based)
│       ├── ConsoleMenu.java
│       └── Display.java              Boxed reports, progress bars, alert banners
├── data/
│   └── sample_nodes.csv              Sample 30-node deployment dataset
├── docs/
│   └── PROGRESS_NOTES.md             Implementation status / next steps
├── build.sh
└── run.sh
```

This mirrors the six-layer architecture from the design document
(Environment, Node, Communication, Routing, Energy, Visualization — the
Visualization Layer here is the text console instead of graphics).

## Build & Run

Requires JDK 11+ (works fine with JDK 17/21).

```bash
./build.sh      # compiles everything into out/
./run.sh        # launches the interactive console menu
```

Or manually:

```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out com.meshsim.Main
```

## Using the console menu

1. **Load predefined scenario** — pick one of the four experimental
   scenarios from the design document: Isolated Survivor, Relay
   Formation, Flood Barrier, or Self-Healing Network.
2. **Load node dataset from CSV** — load `data/sample_nodes.csv` (format:
   `id,x,y,type,range,energy`, type ∈ SURVIVOR/RESCUE/RELAY).
3. **View network / node status** — lists every node's position, battery
   percentage, and active/down state, plus current obstacles.
4. **Send emergency message** — pick a source and destination node and a
   routing protocol (BFS or Dijkstra); the simulator prints the resulting
   path and consumes energy hop-by-hop.
5. **Simulate node failure** — manually take a node offline (simulating
   a destroyed device or dead battery) and watch the network rebuild its
   links.
6. **View performance metrics** — Packet Delivery Ratio, average hop
   count, energy consumed/efficiency, network lifetime, recovery time.
7. **Compute coverage ratio** — reachable survivors / total survivors
   from a chosen rescue node.
8. **Add obstacle** — drop a flood zone / fire region / rubble field /
   building collapse anywhere on the map and see which links break.

## What's implemented so far (~30–35% of the full design)

Implemented: node/environment/obstacle model, distance-based graph
construction with obstacle-aware link blocking, BFS and Dijkstra routing,
a first-order radio energy model with automatic node failure, network
self-healing via graph rebuild, the four experimental scenarios, CSV
dataset loading, and full performance-metrics tracking — all driven from
a console menu.

Not yet implemented (see `docs/PROGRESS_NOTES.md`): DSR/AODV protocols,
Random Waypoint mobility, GUI/graphical visualization, log-distance path
loss per-obstacle exponent wiring into the energy model, and automated
report/CSV export of metrics.
