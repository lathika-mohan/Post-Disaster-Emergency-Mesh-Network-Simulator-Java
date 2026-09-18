# Screenshots — Post-Disaster Emergency Mesh Network Simulator (Swing GUI)

Captured by actually running the built jar (`java --enable-preview -cp out com.meshsim.Main --gui`)
against a virtual display (Xvfb) and driving it with real mouse clicks (xdotool) —
not mockups.

| File | What it shows |
|---|---|
| `01-initial-state-6-nodes-2-obstacles.png` | App launches with the demo scenario: 6 nodes (base station, 2 rescue teams, 2 survivors, 1 relay), a rubble field, and a flood zone. |
| `02-start-clicked-tick6.png` | After clicking **Start** — the simulation clock is running (see "Tick 6" in the status bar). |
| `03-mobility-engine-running-tick56.png` | A few seconds later (tick 56) — nodes have visibly moved under Random Waypoint mobility. |
| `04-mobility-engine-running-tick-later.png` | Later still — continued node drift. |
| `05-protocol-dropdown-and-live-link-forming.png` | The protocol dropdown open, showing all 6 routing protocols (BFS, Dijkstra, EnergyAwareDijkstra, AODV, DSR, Epidemic). Also shows a **live green link line** between N4 and N5 — mobility brought them into radio range of each other. |
| `06-event-log-and-node-proximity.png` | The event log panel populated with simulation messages, N4/N5 now overlapping after continued movement. |

## Bugs found and fixed while capturing these

Two real Swing layout bugs surfaced only once the app was actually run and
screenshotted — worth knowing if you're extending this code:

1. **Event log was invisible.** `BorderLayout.SOUTH` and `BorderLayout.PAGE_END`
   are the *same* layout slot in a container with default orientation, so
   `add(logScroll, BorderLayout.SOUTH)` followed by
   `add(statusBar, BorderLayout.PAGE_END)` silently dropped the log panel.
   Fixed by nesting both in their own sub-panel.
2. **Protocol dropdown stretched to fill the whole control rail.** A `JComboBox`
   inside a `BoxLayout.Y_AXIS` column has an unbounded default maximum size, so
   it absorbed all extra vertical space and pushed the buttons to the bottom
   of the window. Fixed with `protocolBox.setMaximumSize(protocolBox.getPreferredSize())`
   and a trailing `Box.createVerticalGlue()`.

Both fixes are already included in the latest `mesh-sim-execution.zip` source
drop — see `gui/MeshSimFrame.java` and `gui/ControlPanel.java`.
