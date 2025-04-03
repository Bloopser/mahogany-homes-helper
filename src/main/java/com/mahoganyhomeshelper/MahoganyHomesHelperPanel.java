/*
 * Copyright (c) 2025, Bloopser <https://github.com/Bloopser>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mahoganyhomeshelper;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.callback.ClientThread;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.Objects;
// import lombok.extern.slf4j.Slf4j; // Removed Slf4j import

// Removed @Slf4j annotation
public class MahoganyHomesHelperPanel extends PluginPanel {

    private static final String CONFIG_GROUP = "mahoganyhomeshelper";
    private static final MarkerInfo NONE_MARKER = new MarkerInfo(-1L, "None", null, null);
    private static final String INFO_SEPARATOR = "|";

    private final MahoganyHomesHelperPlugin plugin;
    private final MahoganyHomesHelperConfig config;
    private final ConfigManager configManager;
    private final ClientThread clientThread;

    private JComboBox<PlankType> plankTypeComboBox;
    private JCheckBox dynamicMinimumsCheckbox;
    private JSpinner minPlanksSpinner;
    private JSpinner minSteelBarsSpinner;

    private JComboBox<MarkerInfo> varrockCombo;
    private JComboBox<MarkerInfo> faladorCombo;
    private JComboBox<MarkerInfo> ardougneCombo;
    private JComboBox<MarkerInfo> hosidiusCombo;
    private JComboBox<MarkerInfo> lowPlanksCombo;
    private JComboBox<MarkerInfo> lowSteelCombo;

    private List<MarkerInfo> availableMarkers;

    public MahoganyHomesHelperPanel(MahoganyHomesHelperPlugin plugin, MahoganyHomesHelperConfig config,
            ConfigManager configManager, ClientThread clientThread) {
        super(false);

        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        this.clientThread = clientThread;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        clientThread.invokeLater(() -> {
            this.availableMarkers = plugin.getAllAvailableMarkers();
            SwingUtilities.invokeLater(() -> {
                buildPanel();
                loadConfiguration();
            });
        });
    }

    /**
     * Parses the composite marker info string format ("key|group|id") into a
     * MarkerInfo object.
     * The 'name' field of the returned MarkerInfo will be null as it's not stored
     * in the string.
     *
     * @param info The composite string from the configuration.
     * @return A MarkerInfo object representing the stored marker, or NONE_MARKER if
     *         parsing fails or the string is empty/invalid.
     */
    private MarkerInfo parseMarkerInfoString(String info) {
        if (info == null || info.isEmpty()) {
            return NONE_MARKER;
        }
        String[] parts = info.split("\\" + INFO_SEPARATOR, 3);
        if (parts.length != 3) {
            return NONE_MARKER;
        }
        try {
            String key = parts[0];
            String group = "null".equalsIgnoreCase(parts[1]) ? null : parts[1];
            long id = Long.parseLong(parts[2]);
            if (id < 0) {
                return NONE_MARKER;
            }
            return new MarkerInfo(id, null, group, key);
        } catch (NumberFormatException e) {
            return NONE_MARKER;
        }
    }

    /**
     * Creates the composite marker info string ("key|group|id") from a MarkerInfo
     * object for storage.
     *
     * @param markerInfo The MarkerInfo object to serialize.
     * @return The composite string representation, or an empty string if the input
     *         is null or represents NONE_MARKER.
     */
    private String createMarkerInfoString(MarkerInfo markerInfo) {
        if (markerInfo == null || markerInfo.getId() < 0) {
            return "";
        }
        String groupString = markerInfo.getGroupName() == null ? "null" : markerInfo.getGroupName();
        return String.join(INFO_SEPARATOR,
                markerInfo.getSourcePluginKey(),
                groupString,
                String.valueOf(markerInfo.getId()));
    }

    /**
     * Constructs the main panel layout and components.
     * This method should be called on the Swing Event Dispatch Thread.
     */
    private void buildPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        mainPanel.add(new JLabel("Plank Type:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        plankTypeComboBox = new JComboBox<>(PlankType.values());
        plankTypeComboBox.setPreferredSize(new Dimension(120, plankTypeComboBox.getPreferredSize().height));
        plankTypeComboBox.setFocusable(false);
        plankTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                clientThread.invokeLater(() -> configManager.setConfiguration(CONFIG_GROUP, "plankType",
                        plankTypeComboBox.getSelectedItem()));
            }
        });
        mainPanel.add(plankTypeComboBox, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Dynamic Minimums:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        dynamicMinimumsCheckbox = new JCheckBox();
        dynamicMinimumsCheckbox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        dynamicMinimumsCheckbox.addItemListener(e -> {
            boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;
            clientThread.invokeLater(() -> configManager.setConfiguration(CONFIG_GROUP, "dynamicMinimums", isSelected));
            minPlanksSpinner.setEnabled(!isSelected);
            minSteelBarsSpinner.setEnabled(!isSelected);
            if (isSelected) {
                plugin.fetchAndUpdateDynamicSpinners();
            }
        });
        mainPanel.add(dynamicMinimumsCheckbox, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Min Planks:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        minPlanksSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        minPlanksSpinner.setPreferredSize(new Dimension(120, minPlanksSpinner.getPreferredSize().height));
        minPlanksSpinner.addChangeListener(e -> {
            clientThread.invokeLater(() -> configManager.setConfiguration(CONFIG_GROUP, "minPlanks",
                    minPlanksSpinner.getValue()));
        });
        mainPanel.add(minPlanksSpinner, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Min Steel Bars:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        minSteelBarsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        minSteelBarsSpinner.setPreferredSize(new Dimension(120, minSteelBarsSpinner.getPreferredSize().height));
        minSteelBarsSpinner.addChangeListener(e -> {
            clientThread.invokeLater(() -> configManager.setConfiguration(CONFIG_GROUP, "minSteelBars",
                    minSteelBarsSpinner.getValue()));
        });
        mainPanel.add(minSteelBarsSpinner, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(10, 0, 5, 0);
        mainPanel.add(new JSeparator(), c);
        c.gridy++;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 5, 5);

        if (this.availableMarkers == null) {
            this.availableMarkers = Collections.emptyList();
        }

        varrockCombo = createMarkerComboBox(this.availableMarkers, config.varrockMarkerInfo(), "varrockMarkerInfo");
        faladorCombo = createMarkerComboBox(this.availableMarkers, config.faladorMarkerInfo(), "faladorMarkerInfo");
        ardougneCombo = createMarkerComboBox(this.availableMarkers, config.ardougneMarkerInfo(), "ardougneMarkerInfo");
        hosidiusCombo = createMarkerComboBox(this.availableMarkers, config.hosidiusMarkerInfo(), "hosidiusMarkerInfo");
        lowPlanksCombo = createMarkerComboBox(this.availableMarkers, config.lowPlanksMarkerInfo(),
                "lowPlanksMarkerInfo");
        lowSteelCombo = createMarkerComboBox(this.availableMarkers, config.lowSteelMarkerInfo(), "lowSteelMarkerInfo");

        Dimension comboSize = new Dimension(120, varrockCombo.getPreferredSize().height);
        varrockCombo.setPreferredSize(comboSize);
        faladorCombo.setPreferredSize(comboSize);
        ardougneCombo.setPreferredSize(comboSize);
        hosidiusCombo.setPreferredSize(comboSize);
        lowPlanksCombo.setPreferredSize(comboSize);
        lowSteelCombo.setPreferredSize(comboSize);

        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        mainPanel.add(new JLabel("Varrock Marker:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        mainPanel.add(varrockCombo, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Falador Marker:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        mainPanel.add(faladorCombo, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Ardougne Marker:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        mainPanel.add(ardougneCombo, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Hosidius Marker:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        mainPanel.add(hosidiusCombo, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Low Planks Marker:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        mainPanel.add(lowPlanksCombo, c);
        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        mainPanel.add(new JLabel("Low Steel Bars Marker:"), c);
        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        mainPanel.add(lowSteelCombo, c);
        c.gridy++;
        c.gridx = 0;

        add(mainPanel, BorderLayout.NORTH);

        JPanel filler = new JPanel();
        filler.setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(filler, BorderLayout.CENTER);
    }

    /**
     * Creates and configures a JComboBox for selecting a screen marker.
     * Populates the combo box with available markers, adds a "None" option,
     * sets the initial selection based on current configuration, and adds a
     * listener to save changes back to the config.
     *
     * @param availableMarkers        The list of currently available MarkerInfo
     *                                objects.
     * @param currentMarkerInfoString The composite string ("key|group|id") from the
     *                                config for the current selection.
     * @param configKey               The specific configuration key this combo box
     *                                manages (e.g., "varrockMarkerInfo").
     * @return A configured JComboBox instance.
     */
    private JComboBox<MarkerInfo> createMarkerComboBox(List<MarkerInfo> availableMarkers,
            String currentMarkerInfoString, String configKey) {
        Vector<MarkerInfo> comboBoxModel = new Vector<>(availableMarkers);
        comboBoxModel.insertElementAt(NONE_MARKER, 0);

        JComboBox<MarkerInfo> comboBox = new JComboBox<>(comboBoxModel);

        MarkerInfo currentSelectionTarget = parseMarkerInfoString(currentMarkerInfoString);
        MarkerInfo actualSelection = NONE_MARKER;

        if (currentSelectionTarget.getId() >= 0) {
            for (MarkerInfo available : availableMarkers) {
                if (available.getId() == currentSelectionTarget.getId() &&
                        Objects.equals(available.getSourcePluginKey(), currentSelectionTarget.getSourcePluginKey()) &&
                        Objects.equals(available.getGroupName(), currentSelectionTarget.getGroupName())) {
                    actualSelection = available;
                    break;
                }
            }
        }

        comboBox.setSelectedItem(actualSelection);

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MarkerInfo) {
                    setText(((MarkerInfo) value).getDisplayName());
                } else {
                    setText(value != null ? value.toString() : "null");
                }
                return this;
            }
        });

        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                MarkerInfo selected = (MarkerInfo) e.getItem();
                String infoString = createMarkerInfoString(selected);
                clientThread.invokeLater(() -> {
                    try {
                        configManager.setConfiguration(CONFIG_GROUP, configKey, infoString);
                    } catch (Exception ex) {
                    }
                });
            }
        });

        return comboBox;
    }

    /**
     * Loads the current configuration values from ConfigManager and updates the UI
     * components accordingly.
     * Should be called on the client thread.
     */
    private void loadConfiguration() {
        clientThread.invokeLater(() -> {
            plankTypeComboBox.setSelectedItem(config.plankType());

            boolean dynamicEnabled = config.dynamicMinimums();
            dynamicMinimumsCheckbox.setSelected(dynamicEnabled);
            minPlanksSpinner.setEnabled(!dynamicEnabled);
            minSteelBarsSpinner.setEnabled(!dynamicEnabled);

            minPlanksSpinner.setValue(config.minPlanks());
            minSteelBarsSpinner.setValue(config.minSteelBars());
        });
    }

    /**
     * Updates the minimum plank and steel bar spinners with the provided values.
     * Typically called when dynamic minimums are enabled and a new contract is
     * received
     * or the plank type changes.
     * Ensures the update happens on the Swing Event Dispatch Thread.
     *
     * @param planks The dynamic minimum plank count.
     * @param bars   The dynamic minimum steel bar count.
     */
    public void updateDynamicMinimumSpinners(int planks, int bars) {
        SwingUtilities.invokeLater(() -> {
            if (minPlanksSpinner != null) {
                minPlanksSpinner.setValue(planks);
            }
            if (minSteelBarsSpinner != null) {
                minSteelBarsSpinner.setValue(bars);
            }
        });
    }
}
