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

import java.util.Objects;

/**
 * Represents information about a screen marker for selection UI.
 * Includes marker ID, name, source plugin key, and potentially its group name.
 */
// Removed @Value annotation
public class MarkerInfo {
    private final long id;
    private final String name;
    private final String groupName; // Can be null or empty if not part of a group
    private final String sourcePluginKey; // e.g., "markers" or "markerGroups"

    // Constructor
    public MarkerInfo(long id, String name, String groupName, String sourcePluginKey) { // Changed id to long
        this.id = id;
        this.name = name;
        this.groupName = groupName;
        this.sourcePluginKey = sourcePluginKey;
    }

    // --- Getters ---
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getSourcePluginKey() {
        return sourcePluginKey;
    }
    // --- End Getters ---

    /**
     * Provides a display name for the UI, including the group if available.
     * 
     * @return Formatted display name.
     */
    public String getDisplayName() {
        if (groupName != null && !groupName.isEmpty()) {
            return String.format("%s (%s)", name, groupName);
        }
        return name;
    }

    @Override
    public String toString() {
        // Override toString to use the display name in ComboBoxes
        return getDisplayName();
    }

    // --- Custom equals() and hashCode() ---
    // Based ONLY on id, groupName, and sourcePluginKey for reliable comparison
    // in ComboBoxes, even if 'name' is null during parsing/lookup.

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MarkerInfo that = (MarkerInfo) o;
        // Compare long id directly
        return id == that.id &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(sourcePluginKey, that.sourcePluginKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupName, sourcePluginKey);
    }
}
