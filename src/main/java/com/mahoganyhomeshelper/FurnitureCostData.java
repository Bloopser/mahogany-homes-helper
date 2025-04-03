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

// Java Standard Library
import java.util.Collections; // Added import
import java.util.HashMap;
import java.util.Map;

// Third-party Libraries
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Stores and provides access to Mahogany Homes furniture cost data and
 * GameObject ID mappings.
 * Costs are populated from wiki data and IDs are mapped based on collected
 * logs.
 */
@Slf4j
public class FurnitureCostData {

    /**
     * Represents the resource cost (planks and steel bars) for a specific piece of
     * furniture.
     */
    @Value
    public static class FurnitureCost {
        /** The normalized name of the furniture item. */
        String furnitureName;
        /** The number of planks required. */
        int plankCost;
        /** The number of steel bars required. */
        int steelBarCost;
    }

    // Structure: Map<CityName (lower), Map<NpcName (lower), Map<FurnitureName
    // (lower), FurnitureCost>>>
    private static final Map<String, Map<String, Map<String, FurnitureCost>>> furnitureCosts = new HashMap<>();

    /**
     * Helper method to populate the furnitureCosts map during static
     * initialization.
     * Normalizes city, NPC, and furniture names to lowercase and removes
     * "(1)"/"(2)"
     * suffixes from furniture names.
     *
     * @param city      The city name.
     * @param npc       The NPC name.
     * @param furniture The furniture name (potentially with suffixes).
     * @param planks    The plank cost.
     * @param bars      The steel bar cost.
     */
    private static void addCost(String city, String npc, String furniture, int planks, int bars) {
        // Remove " (1)" or " (2)" suffixes and trim whitespace
        String normalizedFurnitureName = furniture.replaceAll("\\s*\\(\\d+\\)$", "").trim();
        FurnitureCost cost = new FurnitureCost(normalizedFurnitureName, planks, bars);

        furnitureCosts
                .computeIfAbsent(city.toLowerCase(), k -> new HashMap<>())
                .computeIfAbsent(npc.toLowerCase(), k -> new HashMap<>())
                .put(normalizedFurnitureName.toLowerCase(), cost); // Use lowercase name as key for lookup consistency
    }

    // Static initializer block to populate the map
    static {
        // --- Ardougne ---
        // Jess
        addCost("ardougne", "jess", "Cabinet", 2, 0);
        addCost("ardougne", "jess", "Drawers", 2, 0);
        addCost("ardougne", "jess", "Bed", 3, 0);
        addCost("ardougne", "jess", "Bathtub", 0, 1);
        addCost("ardougne", "jess", "Grandfather Clock", 1, 0);
        addCost("ardougne", "jess", "Table", 3, 0);
        // Noella
        addCost("ardougne", "noella", "Cupboard", 2, 0);
        addCost("ardougne", "noella", "Dresser", 2, 0);
        addCost("ardougne", "noella", "Drawers", 2, 0);
        addCost("ardougne", "noella", "Hat Stand", 1, 0);
        addCost("ardougne", "noella", "Table", 3, 0);
        addCost("ardougne", "noella", "Grandfather clock", 1, 0);
        addCost("ardougne", "noella", "Mirror", 1, 0);
        // Ross
        addCost("ardougne", "ross", "Bed", 2, 0);
        addCost("ardougne", "ross", "Double Bed", 3, 0);
        addCost("ardougne", "ross", "Drawers", 2, 0);
        addCost("ardougne", "ross", "Hat Stand", 1, 0);
        addCost("ardougne", "ross", "Mirror", 1, 0);
        addCost("ardougne", "ross", "Range", 0, 1);

        // --- Falador ---
        // Larry
        addCost("falador", "larry", "Drawers", 2, 0);
        addCost("falador", "larry", "Grandfather Clock", 1, 0);
        addCost("falador", "larry", "Hat stand", 1, 0);
        addCost("falador", "larry", "Table", 3, 0);
        addCost("falador", "larry", "Range", 0, 1);
        // Norman
        addCost("falador", "norman", "Bookshelf", 2, 0);
        addCost("falador", "norman", "Double Bed", 3, 0);
        addCost("falador", "norman", "Drawers", 2, 0);
        addCost("falador", "norman", "Grandfather Clock", 1, 0);
        addCost("falador", "norman", "Small Table", 2, 0);
        addCost("falador", "norman", "Table", 3, 0);
        addCost("falador", "norman", "Range", 0, 1);
        // Tau
        addCost("falador", "tau", "Cupboard", 2, 0);
        addCost("falador", "tau", "Hat Stand", 1, 0);
        addCost("falador", "tau", "Shelves", 2, 0);
        addCost("falador", "tau", "Sink", 0, 1);
        addCost("falador", "tau", "Table", 3, 0);

        // --- Hosidius ---
        // Barbara
        addCost("hosidius", "barbara", "Bed", 2, 0);
        addCost("hosidius", "barbara", "Chair", 1, 0);
        addCost("hosidius", "barbara", "Drawers", 2, 0);
        addCost("hosidius", "barbara", "Table", 3, 0);
        addCost("hosidius", "barbara", "Range", 0, 1);
        addCost("hosidius", "barbara", "Grandfather Clock", 1, 0);
        // Leela
        addCost("hosidius", "leela", "Cupboard", 2, 0);
        addCost("hosidius", "leela", "Small Table", 2, 0);
        addCost("hosidius", "leela", "Double Bed", 3, 0);
        addCost("hosidius", "leela", "Mirror", 1, 0);
        addCost("hosidius", "leela", "Table", 3, 0);
        addCost("hosidius", "leela", "Sink", 0, 1);
        // Mariah
        addCost("hosidius", "mariah", "Bed", 2, 0);
        addCost("hosidius", "mariah", "Cupboard", 2, 0);
        addCost("hosidius", "mariah", "Hat Stand", 1, 0);
        addCost("hosidius", "mariah", "Shelves", 2, 0);
        addCost("hosidius", "mariah", "Sink", 0, 1);
        addCost("hosidius", "mariah", "Small Table", 2, 0);
        addCost("hosidius", "mariah", "Table", 3, 0);

        // --- Varrock ---
        // Bob
        addCost("varrock", "bob", "Large table", 4, 0);
        addCost("varrock", "bob", "Bookcase", 2, 0);
        addCost("varrock", "bob", "Cabinet", 2, 0);
        addCost("varrock", "bob", "Grandfather Clock", 1, 0);
        addCost("varrock", "bob", "Wardrobe", 2, 0);
        addCost("varrock", "bob", "Drawers", 2, 0);
        // Jeff
        addCost("varrock", "jeff", "Bookcase", 2, 0);
        addCost("varrock", "jeff", "Chair", 1, 0);
        addCost("varrock", "jeff", "Drawers", 2, 0);
        addCost("varrock", "jeff", "Dresser", 2, 0);
        addCost("varrock", "jeff", "Table", 3, 0);
        addCost("varrock", "jeff", "Shelves", 2, 0);
        addCost("varrock", "jeff", "Mirror", 1, 0);
        addCost("varrock", "jeff", "Bed", 3, 0);
        // Sarah
        addCost("varrock", "sarah", "Bed", 2, 0);
        addCost("varrock", "sarah", "Dresser", 2, 0);
        addCost("varrock", "sarah", "Shelves", 2, 0);
        addCost("varrock", "sarah", "Small table", 2, 0);
        addCost("varrock", "sarah", "Table", 3, 0);
        addCost("varrock", "sarah", "Range", 0, 1);
    }

    /**
     * Looks up the cost for a specific piece of furniture based on city, NPC, and
     * furniture name.
     * Performs case-insensitive lookups for city, NPC, and furniture name.
     *
     * @param city          The contract city name.
     * @param npc           The contract NPC name.
     * @param furnitureName The normalized name of the furniture.
     * @return The {@link FurnitureCost} object containing plank and steel bar
     *         costs, or {@code null} if no cost data is found for the given
     *         combination.
     */
    public static FurnitureCost getCost(String city, String npc, String furnitureName) {
        if (city == null || npc == null || furnitureName == null) {
            return null;
        }

        // Retrieve the map of furniture costs for the specific NPC, using lowercase
        // keys
        Map<String, FurnitureCost> npcCostsMap = furnitureCosts.getOrDefault(city.toLowerCase(), Collections.emptyMap())
                .get(npc.toLowerCase());

        if (npcCostsMap != null) {
            // Look up the cost directly using the lowercase furniture name
            FurnitureCost cost = npcCostsMap.get(furnitureName.toLowerCase());
            if (cost != null) {
                return cost;
            }
        }
        return null;
    }

    // --- GameObject ID to Furniture Name Mapping ---

    /**
     * Map linking GameObject IDs to their corresponding normalized furniture names.
     */
    private static final Map<Integer, String> gameObjectIdToNameMap = new HashMap<>();

    // Static initializer for the GameObject ID map
    static {
        // Populate map based on collected data (JSON provided by user)
        // Format: gameObjectIdToNameMap.put(GameObjectId, "Normalized Furniture Name");

        // Falador - Tau (IDs: 40083-40088)
        gameObjectIdToNameMap.put(40085, "Table");
        gameObjectIdToNameMap.put(40084, "Table");
        gameObjectIdToNameMap.put(40086, "Cupboard");
        gameObjectIdToNameMap.put(40087, "Shelves");
        gameObjectIdToNameMap.put(40088, "Shelves");
        gameObjectIdToNameMap.put(40083, "Sink");
        // Falador - Larry (IDs: 40095-40099, 40297-40298)
        gameObjectIdToNameMap.put(40298, "Hat stand");
        gameObjectIdToNameMap.put(40096, "Drawers");
        gameObjectIdToNameMap.put(40095, "Drawers");
        gameObjectIdToNameMap.put(40297, "Range");
        gameObjectIdToNameMap.put(40098, "Table");
        gameObjectIdToNameMap.put(40099, "Grandfather Clock");
        // Falador - Norman (IDs: 40089-40094, 40296)
        gameObjectIdToNameMap.put(40091, "Double Bed");
        gameObjectIdToNameMap.put(40089, "Grandfather Clock");
        gameObjectIdToNameMap.put(40094, "Small Table");
        gameObjectIdToNameMap.put(40090, "Table");
        gameObjectIdToNameMap.put(40296, "Range");
        gameObjectIdToNameMap.put(40092, "Bookshelf");
        gameObjectIdToNameMap.put(40093, "Drawers");

        // Ardougne - Ross
        gameObjectIdToNameMap.put(40166, "Drawers");
        gameObjectIdToNameMap.put(40165, "Drawers");
        gameObjectIdToNameMap.put(40164, "Range");
        gameObjectIdToNameMap.put(40168, "Hat Stand");
        gameObjectIdToNameMap.put(40167, "Double Bed");
        gameObjectIdToNameMap.put(40170, "Mirror");
        gameObjectIdToNameMap.put(40169, "Bed");
        // Ardougne - Noella
        gameObjectIdToNameMap.put(40159, "Mirror");
        gameObjectIdToNameMap.put(40157, "Cupboard");
        gameObjectIdToNameMap.put(40156, "Dresser");
        gameObjectIdToNameMap.put(40160, "Drawers");
        gameObjectIdToNameMap.put(40161, "Table");
        gameObjectIdToNameMap.put(40162, "Table");
        gameObjectIdToNameMap.put(40158, "Hat stand");
        gameObjectIdToNameMap.put(40163, "Grandfather clock");
        // Ardougne - Jess
        gameObjectIdToNameMap.put(40172, "Drawers");
        gameObjectIdToNameMap.put(40174, "Cabinet");
        gameObjectIdToNameMap.put(40173, "Cabinet");
        gameObjectIdToNameMap.put(40175, "Bed");
        gameObjectIdToNameMap.put(40177, "Grandfather Clock");
        gameObjectIdToNameMap.put(40171, "Drawers");
        gameObjectIdToNameMap.put(40299, "Bathtub");
        gameObjectIdToNameMap.put(40176, "Table");

        // Varrock - Sarah
        gameObjectIdToNameMap.put(39997, "Table");
        gameObjectIdToNameMap.put(40286, "Range");
        gameObjectIdToNameMap.put(40001, "Shelves");
        gameObjectIdToNameMap.put(40000, "Small table");
        gameObjectIdToNameMap.put(39998, "Bed");
        gameObjectIdToNameMap.put(39999, "Dresser");
        // Varrock - Jeff
        gameObjectIdToNameMap.put(39990, "Bookcase");
        gameObjectIdToNameMap.put(39989, "Table");
        gameObjectIdToNameMap.put(39991, "Shelves");
        gameObjectIdToNameMap.put(39996, "Chair");
        gameObjectIdToNameMap.put(39994, "Dresser");
        gameObjectIdToNameMap.put(39993, "Drawers");
        gameObjectIdToNameMap.put(39992, "Bed");
        gameObjectIdToNameMap.put(39995, "Mirror");
        // Varrock - Bob
        gameObjectIdToNameMap.put(39981, "Large table");
        gameObjectIdToNameMap.put(39982, "Grandfather Clock");
        gameObjectIdToNameMap.put(39985, "Bookcase");
        gameObjectIdToNameMap.put(39983, "Cabinet");
        gameObjectIdToNameMap.put(39984, "Cabinet");
        gameObjectIdToNameMap.put(39986, "Bookcase");
        gameObjectIdToNameMap.put(39987, "Wardrobe");
        gameObjectIdToNameMap.put(39988, "Drawers");

        // Hosidius - Mariah
        gameObjectIdToNameMap.put(40002, "Table");
        gameObjectIdToNameMap.put(40289, "Hat stand");
        gameObjectIdToNameMap.put(40287, "Sink");
        gameObjectIdToNameMap.put(40288, "Cupboard");
        gameObjectIdToNameMap.put(40003, "Shelves");
        gameObjectIdToNameMap.put(40004, "Bed");
        gameObjectIdToNameMap.put(40005, "Table");
        gameObjectIdToNameMap.put(40006, "Small Table");
        // Hosidius - Barbara
        gameObjectIdToNameMap.put(40294, "Drawers");
        gameObjectIdToNameMap.put(40012, "Table");
        gameObjectIdToNameMap.put(40014, "Chair");
        gameObjectIdToNameMap.put(40015, "Chair");
        gameObjectIdToNameMap.put(40293, "Range");
        gameObjectIdToNameMap.put(40013, "Bed");
        gameObjectIdToNameMap.put(40011, "Grandfather Clock");
        // Hosidius - Leela
        gameObjectIdToNameMap.put(40008, "Small Table");
        gameObjectIdToNameMap.put(40291, "Double Bed");
        gameObjectIdToNameMap.put(40007, "Small Table");
        gameObjectIdToNameMap.put(40290, "Sink");
        gameObjectIdToNameMap.put(40292, "Cupboard");
        gameObjectIdToNameMap.put(40010, "Mirror");
        gameObjectIdToNameMap.put(40009, "Table");
    }

    /**
     * Gets the normalized furniture name for a given GameObject ID.
     *
     * @param gameObjectId The ID of the GameObject.
     * @return The normalized furniture name, or null if the ID is not mapped.
     */
    public static String getFurnitureNameForGameObjectId(int gameObjectId) {
        String name = gameObjectIdToNameMap.get(gameObjectId);
        return name;
    }
}
