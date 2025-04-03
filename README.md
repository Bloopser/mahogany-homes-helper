# Mahogany Homes Helper RuneLite Plugin

## Overview

The Mahogany Homes Helper is a RuneLite plugin designed to streamline the Mahogany Homes construction training activity. It automatically activates and deactivates relevant screen markers based on your current contract city (Varrock, Falador, Ardougne, Hosidius) and supply levels (planks, steel bars).

This helps you quickly teleports (items), providing a smoother and more focused gameplay experience.

## Features

*   **Automatic City Marker Activation:** Highlights the marker you've assigned for the current contract city.
*   **Supply Level Markers:** Activates markers you've assigned when your plank or steel bar levels fall below configured thresholds.
*   **Dynamic Minimums:** Optionally sets supply thresholds automatically based on the maximum materials needed for the current contract NPC and selected plank type.
*   **Plank Sack Estimation:** Tracks estimated planks remaining in your plank sack based on game messages and build/repair actions.
*   **Plugin Panel Configuration:** All settings are managed through a dedicated panel in the RuneLite sidebar.
*   **Supports Screen Marker & Screen Marker Groups:** Works with markers created by both the base Screen Marker plugin and the Screen Marker Groups plugin.

## Setup & Usage

1.  **Install Marker Plugin(s):** Ensure you have either the "Screen Marker" plugin or the "Screen Marker Groups" plugin (or both) enabled, as this plugin relies on them to function.
2.  **Create Markers:** Use the Screen Marker or Screen Marker Groups plugin to create markers for the locations you want highlighted (e.g., markers for each contract city map, markers near bank chests, markers indicating low supplies). Give them recognizable names.
3.  **Open the Panel:** Click the Mahogany Homes Helper icon (currently a white square) in the RuneLite sidebar to open the configuration panel.
4.  **Configure Plank Type:** Select the type of planks you are using for your contracts (Planks, Oak, Teak, Mahogany).
5.  **Configure Supply Thresholds:**
    *   **Static:** Leave "Dynamic Minimums" unchecked and set the minimum number of planks (inventory + sack estimate) and steel bars (inventory) you want before the corresponding "Low Supply" markers activate.
    *   **Dynamic:** Check the "Dynamic Minimums" box. The plugin will automatically use the maximum planks/bars needed for the current contract as the threshold. The manual spinners will be disabled.
6.  **Assign Markers:** For each category (Varrock, Falador, Ardougne, Hosidius, Low Planks, Low Steel Bars), use the dropdown menus to select the screen marker you created in step 3. Select "None" if you don't want a marker for a specific category.
    *   Markers from both the base Screen Marker plugin and any groups you've created in Screen Marker Groups should appear in the list. The group name will be shown in parentheses if applicable.
7.  **Start Training:** The plugin will now automatically toggle the visibility of your selected markers based on game events (contract assignments, inventory changes, build actions).

## Notes

*   The plank sack count is an *estimation* based on game messages and observed actions. While generally accurate, discrepancies can occasionally occur.
*   Ensure the marker names you create are clear so you can easily identify them in the configuration panel dropdowns.
