package com.meshsim.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Left-hand control rail: protocol choice, start/pause/step buttons. */
public final class ControlPanel extends JPanel {

    private final JComboBox<String> protocolBox =
            new JComboBox<>(new String[]{"BFS", "Dijkstra", "EnergyAwareDijkstra", "AODV", "DSR", "Epidemic"});
    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton sendButton = new JButton("Send message");

    public ControlPanel(Consumer<String> onStart, Runnable onPause, Runnable onSend) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(180, 0));

        add(new JLabel("Protocol"));
        protocolBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(protocolBox);
        add(Box.createVerticalStrut(10));

        startButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startButton.addActionListener(e -> onStart.accept((String) protocolBox.getSelectedItem()));
        add(startButton);

        pauseButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        pauseButton.addActionListener(e -> onPause.run());
        add(pauseButton);

        sendButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendButton.addActionListener(e -> onSend.run());
        add(Box.createVerticalStrut(10));
        add(sendButton);
    }

    public String selectedProtocol() {
        return (String) protocolBox.getSelectedItem();
    }
}
