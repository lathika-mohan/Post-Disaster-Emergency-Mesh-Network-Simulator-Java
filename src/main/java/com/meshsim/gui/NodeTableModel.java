package com.meshsim.gui;

import com.meshsim.model.Node;
import com.meshsim.model.Scenario;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Backs the node inspector JTable. */
public final class NodeTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"ID", "Type", "X", "Y", "Energy", "Alive"};
    private List<Node> rows = new ArrayList<>();

    public void setScenario(Scenario scenario) {
        rows = new ArrayList<>();
        for (Node n : scenario.nodes()) rows.add(n);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Node n = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> n.id();
            case 1 -> n.type();
            case 2 -> "%.1f".formatted(n.position().x());
            case 3 -> "%.1f".formatted(n.position().y());
            case 4 -> "%.0f%%".formatted(n.energy() * 100);
            case 5 -> n.isAlive();
            default -> "";
        };
    }
}
