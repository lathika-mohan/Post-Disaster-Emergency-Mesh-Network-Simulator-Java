# ResQNet Project Report Corrections

This document details all precise corrections required for the separate PDF project report to align with the final, fully-wired codebase.

---

### 1. File & Architecture Counts (§1 & Appendix A.1)
* **Correction:** Update file counts to reflect 67 main Java source files, 5 unit test source files, 1 pom.xml, and 8 committed screenshots.
* **Appendix A.1 Package Tree:** Update tree structure to include:
  - `com.meshsim.model.DemoScenarios` (Shared factory)
  - `com.meshsim.network.NetworkPartitionDetector` & `RelayPlacementAdvisor`
  - `com.meshsim.simulation.BatchRunner`
  - `com.meshsim.network.PathLossModelTest` & `PartitionedNetworkTest`

---

### 2. Protocol Terminology Standardization (§2 & §5)
* **Correction:** Standardize protocol count across all sections as **six strategies** (five end-to-end routing protocols: BFS, Dijkstra, EnergyAwareDijkstra, AODV, DSR + one store-carry-forward protocol: Epidemic).

---

### 3. §4.5 GUI Layout Narrative Fix (§4.5)
* **Correction:** Correct the §4.5 narrative regarding the event log visibility issue.
  - **Old text:** Claimed JComboBox layout sizing was the sole root cause.
  - **New text:** The true root cause was a double-add collision in `MeshSimFrame` where `add(logScroll, BorderLayout.SOUTH)` was overwritten by `add(statusBar, BorderLayout.PAGE_END)` (which resolves to `BorderLayout.SOUTH` in left-to-right orientation), collapsing the scroll pane height to 0×0. The fix nests `logScroll` and `statusBar` inside a `JPanel` with `BorderLayout`:
    ```java
    JPanel south = new JPanel(new BorderLayout());
    south.add(logScroll, BorderLayout.CENTER);
    south.add(statusBar, BorderLayout.SOUTH);
    add(south, BorderLayout.SOUTH);
    ```

---

### 4. Code Snippets Update (§5.2)
* **Correction:** Update code snippets in §5.2 to paste exact final source code for:
  - `Simulator.java` (Virtual thread creation, `sendMessage`, `pause`/`resume`, and `coreGroup.interrupt()`)
  - `EpidemicRouter.java` (Store-carry-forward handling and `onNodeMoved` contact trigger)
  - `EnergyAwareDijkstraRouter.java` (Battery-penalized link weighting)

---

### 5. Table 3.1 Week 11 Batch Mode Status (§3.1)
* **Correction:** Update Table 3.1 Week 11 status from "Pending" to **Completed**. Batch execution mode is fully implemented via `BatchRunner.java` and accessible via CLI option 8 or `java -jar target/mesh-sim.jar --batch <scenarios-dir> <runs>`.

---

### 6. Table 6.2 Test Suite Results (§6.2)
* **Correction:** Update Table 6.2 to reflect **12 passing unit tests out of 12 (100% pass rate)** across 5 test classes:
  - `DatasetLoaderTest`: 5/5 passed
  - `BFSRouterTest`: 2/2 passed (50m hop geometry verified)
  - `DijkstraRouterTest`: 1/1 passed (RubbleField detour path S->M->D verified)
  - `PathLossModelTest`: 2/2 passed (Deterministic seed & shadowing verified)
  - `PartitionedNetworkTest`: 2/2 passed (FireRegion partition failure vs Epidemic store-carry-forward delivery verified)

---

### 7. Pending Author Items (§7 & Appendix A)
* **Figure 7.1:** Physical network testbed topology diagram (still required from authors).
* **Table A.1:** Field trial empirical packet reception percentages (still required from authors).
