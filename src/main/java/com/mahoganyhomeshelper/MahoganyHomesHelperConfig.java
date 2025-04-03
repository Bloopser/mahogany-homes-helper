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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Configuration interface for the Mahogany Homes Helper plugin.
 * Defines settings managed via the Plugin Panel. Most items are hidden
 * from the standard RuneLite config menu.
 */
@ConfigGroup("mahoganyhomeshelper")
public interface MahoganyHomesHelperConfig extends Config {

	/**
	 * Stores the selected plank type used for Mahogany Homes contracts.
	 * Managed via the Plugin Panel.
	 * 
	 * @return The selected PlankType.
	 */
	@ConfigItem(keyName = "plankType", name = "", description = "Selected plank type for contracts.", hidden = true)
	default PlankType plankType() {
		return PlankType.PLANK; // Default to regular planks
	}

	// --- Stored Marker Info (Managed by panel) ---
	// Stores composite string: "configKey|groupName|markerId"
	// e.g., "markers|null|123456" or "markerGroups|MyGroup|789012"
	// Default "" indicates none selected.

	/**
	 * Stores composite marker info string for Varrock. Format: "key|group|id".
	 * Managed via the Plugin Panel.
	 * 
	 * @return The composite string for the Varrock marker, or "" if none selected.
	 */
	@ConfigItem(keyName = "varrockMarkerInfo", name = "", description = "Selected marker for Varrock contracts.", hidden = true)
	default String varrockMarkerInfo() {
		return "";
	}

	/**
	 * Stores composite marker info string for Falador. Format: "key|group|id".
	 * Managed via the Plugin Panel.
	 * 
	 * @return The composite string for the Falador marker, or "" if none selected.
	 */
	@ConfigItem(keyName = "faladorMarkerInfo", name = "", description = "Selected marker for Falador contracts.", hidden = true)
	default String faladorMarkerInfo() {
		return "";
	}

	/**
	 * Stores composite marker info string for Ardougne. Format: "key|group|id".
	 * Managed via the Plugin Panel.
	 * 
	 * @return The composite string for the Ardougne marker, or "" if none selected.
	 */
	@ConfigItem(keyName = "ardougneMarkerInfo", name = "", description = "Selected marker for Ardougne contracts.", hidden = true)
	default String ardougneMarkerInfo() {
		return "";
	}

	/**
	 * Stores composite marker info string for Hosidius. Format: "key|group|id".
	 * Managed via the Plugin Panel.
	 * 
	 * @return The composite string for the Hosidius marker, or "" if none selected.
	 */
	@ConfigItem(keyName = "hosidiusMarkerInfo", name = "", description = "Selected marker for Hosidius contracts.", hidden = true)
	default String hosidiusMarkerInfo() {
		return "";
	}

	/**
	 * Stores composite marker info string for Low Planks warning. Format:
	 * "key|group|id".
	 * Managed via the Plugin Panel.
	 * 
	 * @return The composite string for the low planks marker, or "" if none
	 *         selected.
	 */
	@ConfigItem(keyName = "lowPlanksMarkerInfo", name = "", description = "Selected marker for low plank warning.", hidden = true)
	default String lowPlanksMarkerInfo() {
		return "";
	}

	/**
	 * Stores composite marker info string for Low Steel Bars warning. Format:
	 * "key|group|id".
	 * Managed via the Plugin Panel.
	 * 
	 * @return The composite string for the low steel bars marker, or "" if none
	 *         selected.
	 */
	@ConfigItem(keyName = "lowSteelMarkerInfo", name = "", description = "Selected marker for low steel bar warning.", hidden = true)
	default String lowSteelMarkerInfo() {
		return "";
	}

	/**
	 * Determines whether to use dynamic minimum supply thresholds based on the
	 * current contract NPC.
	 * Managed via the Plugin Panel.
	 * 
	 * @return true if dynamic minimums are enabled, false otherwise.
	 */
	@ConfigItem(keyName = "dynamicMinimums", name = "Dynamic Minimums", description = "Automatically set minimums based on current contract instead of manual input.", position = 10, hidden = true)
	default boolean dynamicMinimums() {
		return false; // Default to off
	}

	// --- Supply Thresholds (Managed by panel when dynamicMinimums is false) ---

	/**
	 * The minimum number of planks (inventory + estimated sack count) required
	 * before the low planks marker is activated.
	 * Only used when dynamicMinimums is false. Managed via the Plugin Panel.
	 * 
	 * @return The minimum plank threshold.
	 */
	@ConfigItem(keyName = "minPlanks", name = "", description = "Minimum plank threshold (static).", hidden = true)
	default int minPlanks() {
		return 10; // Default threshold
	}

	/**
	 * The minimum number of steel bars (inventory) required before the low steel
	 * bars marker is activated.
	 * Only used when dynamicMinimums is false. Managed via the Plugin Panel.
	 * 
	 * @return The minimum steel bar threshold.
	 */
	@ConfigItem(keyName = "minSteelBars", name = "", description = "Minimum steel bar threshold (static).", hidden = true)
	default int minSteelBars() {
		return 1; // Default threshold
	}

	// --- Plank Sack Count (Hidden, managed internally) ---
	/** Config group specifically for storing the estimated plank sack count. */
	String SACK_CONFIG_GROUP = "mahoganyhomessack"; // Separate group to avoid conflict if user also has PlankSackPlugin
	/** Config key for storing the estimated plank sack count. */
	String SACK_KEY = "plankcount";

	/**
	 * Stores the internally estimated number of planks in the player's plank sack.
	 * -1 indicates the count is unknown. Managed internally by the plugin's
	 * estimation logic.
	 * 
	 * @return The estimated plank count, or -1 if unknown.
	 */
	@ConfigItem(keyName = SACK_KEY, name = "", description = "Estimated plank sack count.", hidden = true)
	default int estimatedPlankCount() {
		return -1; // Default to unknown
	}

	/**
	 * Setter for the estimated plank sack count. Used internally by the plugin.
	 * 
	 * @param count The new estimated count.
	 */
	@ConfigItem(keyName = SACK_KEY, name = "", description = "", hidden = true)
	void setEstimatedPlankCount(int count);
}
