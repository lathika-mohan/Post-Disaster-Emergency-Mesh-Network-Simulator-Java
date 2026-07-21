package com.meshsim.data;

import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.network.MeshNetwork;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads a node-deployment dataset from CSV.
 * Expected columns: id,x,y,type,range,energy
 * type must be one of SURVIVOR, RESCUE, RELAY.
 */
public class DatasetLoader {

    public static int loadIntoNetwork(String csvPath, MeshNetwork network) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                if (parts.length < 6) continue;
                int id = Integer.parseInt(parts[0].trim());
                double x = Double.parseDouble(parts[1].trim());
                double y = Double.parseDouble(parts[2].trim());
                NodeType type = NodeType.valueOf(parts[3].trim().toUpperCase());
                double range = Double.parseDouble(parts[4].trim());
                double energy = Double.parseDouble(parts[5].trim());
                network.addNode(new Node(id, x, y, type, range, energy));
                count++;
            }
        }
        network.rebuildAllLinks();
        return count;
    }
}
