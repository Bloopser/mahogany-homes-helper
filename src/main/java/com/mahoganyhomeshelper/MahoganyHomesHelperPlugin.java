/*
 * Copyright (c) 2025, Bloopser <https://github.com/Bloopser>
 * Copyright (c) 2021, Hydrox6 <ikada@protonmail.ch>
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;

/**
 * RuneLite plugin to assist with the Mahogany Homes minigame by automatically
 * managing screen markers based on contract details and supply levels.
 * Configuration is handled via a dedicated Plugin Panel.
 */
@Slf4j
@PluginDescriptor(name = "Mahogany Homes Helper", description = "Activates screen markers based on Mahogany Homes contract city and supply levels via Plugin Panel config.", tags = {
		"mahogany homes", "construction", "skilling", "screen marker", "helper", "mahogany", "homes" })
public class MahoganyHomesHelperPlugin extends Plugin {
	private static final String SCREEN_MARKER_CONFIG_GROUP = "screenmarkers";
	private static final String SCREEN_MARKER_MARKERS_KEY = "markers";
	private static final String SCREEN_MARKER_GROUPS_CONFIG_GROUP = "screenmarkergroups";
	private static final String SCREEN_MARKER_GROUPS_KEY = "markerGroups";

	private static final Pattern CONTRACT_PATTERN = Pattern.compile(
			".*?see <col=ff0000>([^<]+?)\\s*</col>.*?<col=ff0000>(Varrock|Falador|Ardougne(?: market)?|Hosidius)</col>.*",
			Pattern.CASE_INSENSITIVE);

	private static final List<Integer> PLANKS = Arrays.asList(ItemID.PLANK, ItemID.OAK_PLANK, ItemID.TEAK_PLANK,
			ItemID.MAHOGANY_PLANK);
	private static final List<String> PLANK_NAMES = Arrays.asList("Plank", "Oak plank", "Teak plank",
			"Mahogany plank");
	private static final int CONSTRUCTION_WIDGET_GROUP = 458;
	private static final int CONSTRUCTION_WIDGET_BUILD_IDX_START = 4;
	private static final int CONSTRUCTION_SUBWIDGET_MATERIALS = 3;
	private static final int CONSTRUCTION_SUBWIDGET_CANT_BUILD = 5;

	private static final Map<String, Map<String, Map<PlankType, Point>>> NPC_REQUIREMENTS;

	static {
		Map<String, Map<String, Map<PlankType, Point>>> reqMap = new HashMap<>();
		Map<String, Map<PlankType, Point>> ardougneNpcs = new HashMap<>();
		ardougneNpcs.put("jess", Map.of(PlankType.PLANK, new Point(11, 1), PlankType.OAK_PLANK, new Point(11, 1),
				PlankType.TEAK_PLANK, new Point(15, 1), PlankType.MAHOGANY_PLANK, new Point(15, 1)));
		ardougneNpcs.put("noella", Map.of(PlankType.PLANK, new Point(12, 0), PlankType.OAK_PLANK, new Point(12, 0),
				PlankType.TEAK_PLANK, new Point(15, 0), PlankType.MAHOGANY_PLANK, new Point(15, 0)));
		ardougneNpcs.put("ross", Map.of(PlankType.PLANK, new Point(11, 1), PlankType.OAK_PLANK, new Point(11, 1),
				PlankType.TEAK_PLANK, new Point(11, 1), PlankType.MAHOGANY_PLANK, new Point(11, 1)));
		reqMap.put("ardougne", Collections.unmodifiableMap(ardougneNpcs));
		Map<String, Map<PlankType, Point>> faladorNpcs = new HashMap<>();
		faladorNpcs.put("larry", Map.of(PlankType.PLANK, new Point(12, 1), PlankType.OAK_PLANK, new Point(12, 1),
				PlankType.TEAK_PLANK, new Point(12, 1), PlankType.MAHOGANY_PLANK, new Point(12, 1)));
		faladorNpcs.put("norman", Map.of(PlankType.PLANK, new Point(11, 1), PlankType.OAK_PLANK, new Point(11, 1),
				PlankType.TEAK_PLANK, new Point(13, 1), PlankType.MAHOGANY_PLANK, new Point(13, 1)));
		faladorNpcs.put("tau", Map.of(PlankType.PLANK, new Point(12, 1), PlankType.OAK_PLANK, new Point(12, 1),
				PlankType.TEAK_PLANK, new Point(13, 1), PlankType.MAHOGANY_PLANK, new Point(13, 1)));
		reqMap.put("falador", Collections.unmodifiableMap(faladorNpcs));
		Map<String, Map<PlankType, Point>> hosidiusNpcs = new HashMap<>();
		hosidiusNpcs.put("barbara", Map.of(PlankType.PLANK, new Point(8, 1), PlankType.OAK_PLANK, new Point(8, 1),
				PlankType.TEAK_PLANK, new Point(10, 1), PlankType.MAHOGANY_PLANK, new Point(10, 1)));
		hosidiusNpcs.put("leela", Map.of(PlankType.PLANK, new Point(9, 1), PlankType.OAK_PLANK, new Point(9, 1),
				PlankType.TEAK_PLANK, new Point(10, 1), PlankType.MAHOGANY_PLANK, new Point(13, 1)));
		hosidiusNpcs.put("mariah", Map.of(PlankType.PLANK, new Point(11, 1), PlankType.OAK_PLANK, new Point(11, 1),
				PlankType.TEAK_PLANK, new Point(14, 1), PlankType.MAHOGANY_PLANK, new Point(14, 1)));
		reqMap.put("hosidius", Collections.unmodifiableMap(hosidiusNpcs));
		Map<String, Map<PlankType, Point>> varrockNpcs = new HashMap<>();
		varrockNpcs.put("bob", Map.of(PlankType.PLANK, new Point(14, 0), PlankType.OAK_PLANK, new Point(14, 0),
				PlankType.TEAK_PLANK, new Point(17, 0), PlankType.MAHOGANY_PLANK, new Point(17, 0)));
		varrockNpcs.put("jeff", Map.of(PlankType.PLANK, new Point(13, 0), PlankType.OAK_PLANK, new Point(13, 0),
				PlankType.TEAK_PLANK, new Point(16, 0), PlankType.MAHOGANY_PLANK, new Point(16, 0)));
		varrockNpcs.put("sarah", Map.of(PlankType.PLANK, new Point(11, 1), PlankType.OAK_PLANK, new Point(11, 1),
				PlankType.TEAK_PLANK, new Point(11, 1), PlankType.MAHOGANY_PLANK, new Point(11, 1)));
		reqMap.put("varrock", Collections.unmodifiableMap(varrockNpcs));
		NPC_REQUIREMENTS = Collections.unmodifiableMap(reqMap);
	}

	private static class MarkerPojo {
		long id;
		String name;
		boolean visible;
		Long importedId;
		@SuppressWarnings("unused") // Field required by ScreenMarkerPlugin to avoid data loss on serialization
		int borderThickness;
		@SuppressWarnings("unused") // Field required by ScreenMarkerPlugin to avoid data loss on serialization
		Color color;
		@SuppressWarnings("unused") // Field required by ScreenMarkerPlugin to avoid data loss on serialization
		Color fill;
		@SuppressWarnings("unused") // Field required by ScreenMarkerPlugin to avoid data loss on serialization
		boolean labelled;
	}

	@Data
	private static class BuildMenuItem {
		private final Item[] planks;
		private final boolean canBuild;
	}

	@Inject
	private Client client;
	@Inject
	private MahoganyHomesHelperConfig config;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ConfigManager configManager;
	@Inject
	private Gson gson;
	@Inject
	private EventBus eventBus;
	@Inject
	private PluginManager pluginManager;

	private NavigationButton navButton;
	private MahoganyHomesHelperPanel panel;

	private MarkerInfo currentCityMarker = null;
	private String currentContractCity = null;
	private String currentContractNpc = null;

	private static final String INFO_SEPARATOR = "|";

	private int estimatedPlankCount = -1;
	private Multiset<Integer> inventorySnapshot;
	private boolean checkForUpdate = false;
	private int menuItemsToCheck = 0;
	private final List<BuildMenuItem> buildMenuItems = new ArrayList<>();
	private int lastClickedGameObjectId = -1;

	@Override
	protected void startUp() throws Exception {
		panel = new MahoganyHomesHelperPanel(this, config, configManager, clientThread);

		final BufferedImage icon = createPlaceholderIcon(16, 16, Color.WHITE);

		navButton = NavigationButton.builder()
				.tooltip("Mahogany Homes Helper")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);

		clientThread.invokeLater(this::checkSupplies);

		clientThread.invokeLater(() -> {
			Integer count = (Integer) configManager.getRSProfileConfiguration(
					MahoganyHomesHelperConfig.SACK_CONFIG_GROUP,
					MahoganyHomesHelperConfig.SACK_KEY, int.class);
			estimatedPlankCount = Optional.ofNullable(count).orElse(-1);
		});
	}

	@Override
	protected void shutDown() throws Exception {
		disableAllHelperMarkers();
		if (navButton != null) {
			clientToolbar.removeNavigation(navButton);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			Integer count = (Integer) configManager.getRSProfileConfiguration(
					MahoganyHomesHelperConfig.SACK_CONFIG_GROUP,
					MahoganyHomesHelperConfig.SACK_KEY, int.class);
			estimatedPlankCount = Optional.ofNullable(count).orElse(-1);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() != InventoryID.INVENTORY.getId()) {
			return;
		}

		if (checkForUpdate) {
			checkForUpdate = false;
			Multiset<Integer> currentInventory = createSnapshot(event.getItemContainer());
			if (inventorySnapshot != null && currentInventory != null) {
				Multiset<Integer> deltaMinus = Multisets.difference(currentInventory, inventorySnapshot);
				Multiset<Integer> deltaPlus = Multisets.difference(inventorySnapshot, currentInventory);
				int countChange = 0;
				for (Multiset.Entry<Integer> entry : deltaPlus.entrySet()) {
					countChange += entry.getCount();
				}
				for (Multiset.Entry<Integer> entry : deltaMinus.entrySet()) {
					countChange -= entry.getCount();
				}

				if (estimatedPlankCount != -1) {
					setEstimatedPlankCount(estimatedPlankCount + countChange);
				}
			}
		}

		checkSupplies();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if ((event.getItemId() == ItemID.PLANK_SACK
				&& (event.getMenuOption().equals("Fill") || event.getMenuOption().equals("Empty")))
				|| (event.getMenuTarget().equals("<col=ff9040>Plank sack</col>")
						&& event.getMenuOption().equals("Use"))) {
			inventorySnapshot = createSnapshot(client.getItemContainer(InventoryID.INVENTORY));
			checkForUpdate = true;
		} else if (event.getMenuOption().equals("Use") && event.getParam1() == 9
				&& event.getMenuAction() == MenuAction.CC_OP) {
			ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
			if (inventory != null) {
				Item[] items = inventory.getItems();
				int idx = event.getParam0();
				if (idx >= 0 && idx < items.length && items[idx].getId() == ItemID.PLANK_SACK) {
					inventorySnapshot = createSnapshot(client.getItemContainer(InventoryID.INVENTORY));
					checkForUpdate = true;
				}
			}
		} else if (event.getMenuOption().equals("Use") && event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET &&
				(event.getItemId() == ItemID.PLANK_SACK || PLANKS.contains(event.getItemId()))) {
			ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
			if (inventory != null) {
				Widget selectedWidget = client.getSelectedWidget();
				if (selectedWidget != null) {
					int selectedItemID = selectedWidget.getItemId();
					if ((selectedItemID == ItemID.PLANK_SACK && PLANKS.contains(event.getItemId()))
							|| (PLANKS.contains(selectedItemID) && event.getItemId() == ItemID.PLANK_SACK)) {
						inventorySnapshot = createSnapshot(client.getItemContainer(InventoryID.INVENTORY));
						checkForUpdate = true;
					}
				}
			}
		} else if (event.getMenuTarget().equals("<col=ff9040>Plank sack</col>") &&
				(event.getMenuOption().equals("Fill from inventory")
						|| event.getMenuOption().equals("Empty to inventory"))) {
			inventorySnapshot = createSnapshot(client.getItemContainer(InventoryID.INVENTORY));
			checkForUpdate = true;
		} else if (event.getMenuOption().equals("Repair") || event.getMenuOption().equals("Build")) {
			try {
				int gameObjectId = event.getId();

				if (gameObjectId > 0 && gameObjectId == lastClickedGameObjectId) {
					return; // Debounce
				}

				PlankType currentPlankType = config.plankType();

				if (gameObjectId <= 0 || currentContractCity == null || currentContractNpc == null
						|| currentPlankType == null) {
					return;
				}

				String furnitureName = FurnitureCostData.getFurnitureNameForGameObjectId(gameObjectId);
				if (furnitureName == null) {
					return;
				}

				FurnitureCostData.FurnitureCost cost = FurnitureCostData.getCost(currentContractCity,
						currentContractNpc, furnitureName);
				if (cost == null) {
					return;
				}

				int plankCost = cost.getPlankCost();
				if (plankCost <= 0) {
					return; // No planks to deduct
				}

				Multiset<Integer> invSnapshot = createSnapshot(client.getItemContainer(InventoryID.INVENTORY));
				int planksInInventory = (invSnapshot != null) ? invSnapshot.count(currentPlankType.getItemId()) : 0;
				int currentSackCount = estimatedPlankCount;

				if (currentSackCount == -1) {
					return; // Cannot reliably deduct if sack count is unknown
				}

				int planksNeededFromSack = Math.max(0, plankCost - planksInInventory);

				if (planksNeededFromSack > 0) {
					if (currentSackCount >= planksNeededFromSack) {
						int newSackCount = currentSackCount - planksNeededFromSack;
						setEstimatedPlankCount(newSackCount);
					} else {
						setEstimatedPlankCount(0);
					}
				}

			} catch (Exception e) {
			} finally {
				if (event.getId() > 0) {
					lastClickedGameObjectId = event.getId();
				}
			}
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event) {
		if (event.getScriptId() != 1405 && event.getScriptId() != 1632) {
			return;
		}

		Widget widget = event.getScriptEvent().getSource();
		if (widget == null)
			return;

		if (widget.getParentId() != CONSTRUCTION_WIDGET_GROUP
				|| widget.getIndex() < CONSTRUCTION_WIDGET_BUILD_IDX_START) {
			return;
		}

		int idx = widget.getIndex() - CONSTRUCTION_WIDGET_BUILD_IDX_START;
		if (idx >= buildMenuItems.size()) {
			return;
		}

		BuildMenuItem item = buildMenuItems.get(idx);
		if (item != null && item.canBuild && estimatedPlankCount != -1) {
			Multiset<Integer> currentInvSnapshot = createSnapshot(client.getItemContainer(InventoryID.INVENTORY));
			if (currentInvSnapshot != null) {
				int planksToDeduct = 0;
				for (Item i : item.planks) {
					if (!currentInvSnapshot.contains(i.getId())) {
						planksToDeduct += i.getQuantity();
					} else if (currentInvSnapshot.count(i.getId()) < i.getQuantity()) {
						planksToDeduct += i.getQuantity() - currentInvSnapshot.count(i.getId());
					}
				}
				if (planksToDeduct > 0) {
					setEstimatedPlankCount(estimatedPlankCount - planksToDeduct);
				}
			}
		}

		buildMenuItems.clear();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() != 1404) {
			return;
		}
		menuItemsToCheck += 1;
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (menuItemsToCheck > 0) {
			buildMenuItems.clear();
			for (int i = 0; i < menuItemsToCheck; i++) {
				int idx = CONSTRUCTION_WIDGET_BUILD_IDX_START + i;
				Widget widget = client.getWidget(CONSTRUCTION_WIDGET_GROUP, idx);
				if (widget != null) {
					Widget[] dynamicChildren = widget.getDynamicChildren();
					if (dynamicChildren != null && dynamicChildren.length > CONSTRUCTION_SUBWIDGET_CANT_BUILD
							&& dynamicChildren.length > CONSTRUCTION_SUBWIDGET_MATERIALS) {
						Widget cantBuildWidget = dynamicChildren[CONSTRUCTION_SUBWIDGET_CANT_BUILD];
						Widget materialWidget = dynamicChildren[CONSTRUCTION_SUBWIDGET_MATERIALS];

						if (cantBuildWidget != null && materialWidget != null) {
							boolean canBuild = cantBuildWidget.isHidden();
							String materialText = materialWidget.getText();
							if (materialText != null && !materialText.isEmpty()) {
								String[] materialLines = materialText.split("<br>");
								List<Item> materials = new ArrayList<>();
								for (String line : materialLines) {
									String[] data = line.split(": ");
									if (data.length == 2) {
										String name = data[0];
										try {
											int count = Integer.parseInt(data[1]);
											if (PLANK_NAMES.contains(name)) {
												materials.add(new Item(PLANKS.get(PLANK_NAMES.indexOf(name)), count));
											}
										} catch (NumberFormatException e) {
										}
									}
								}
								if (!materials.isEmpty()) {
									buildMenuItems.add(new BuildMenuItem(materials.toArray(new Item[0]), canBuild));
								}
							}
						}
					}
				}
			}
			menuItemsToCheck = 0;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event) {
		// No logic needed
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() == ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.DIALOG) {
			String messageText = event.getMessage();
			Matcher matcher = CONTRACT_PATTERN.matcher(messageText);
			if (matcher.find()) {
				String npc = matcher.group(1);
				String cityRaw = matcher.group(2);

				if (npc != null && cityRaw != null) {
					String npcTrimmed = npc.trim();
					String cityNormalized = cityRaw.toLowerCase().replace(" market", "");
					handleNewContract(npcTrimmed, cityNormalized);
				}
			}
		}

		if (event.getType() == ChatMessageType.GAMEMESSAGE) {
			String message = Text.removeTags(event.getMessage());
			if (message.startsWith("Basic&#160;planks:")) {
				try {
					int totalCount = Arrays.stream(message.split(","))
							.mapToInt(s -> Integer.parseInt(s.split(":&#160;")[1].trim()))
							.sum();
					setEstimatedPlankCount(totalCount);
					checkForUpdate = false;
				} catch (Exception e) {
				}
			} else if (message.equals("You haven't got any planks that can go in the sack.")) {
				checkForUpdate = false;
			} else if (message.equals("Your sack is full.")) {
				setEstimatedPlankCount(28);
				checkForUpdate = false;
				checkSupplies();
			} else if (message.equals("Your sack is empty.")) {
				setEstimatedPlankCount(0);
				checkForUpdate = false;
				checkSupplies();
			}
		}
	}

	private void handleNewContract(String npc, String city) {
		currentContractCity = city;
		currentContractNpc = npc;
		lastClickedGameObjectId = -1;

		if (config.dynamicMinimums()) {
			fetchAndUpdateDynamicSpinners();
		}

		if (!"varrock".equals(city)) {
			disableMarker(parseMarkerInfoString(config.varrockMarkerInfo()));
		}
		if (!"falador".equals(city)) {
			disableMarker(parseMarkerInfoString(config.faladorMarkerInfo()));
		}
		if (!"ardougne".equals(city)) {
			disableMarker(parseMarkerInfoString(config.ardougneMarkerInfo()));
		}
		if (!"hosidius".equals(city)) {
			disableMarker(parseMarkerInfoString(config.hosidiusMarkerInfo()));
		}

		String markerInfoString;
		switch (city) {
			case "varrock":
				markerInfoString = config.varrockMarkerInfo();
				break;
			case "falador":
				markerInfoString = config.faladorMarkerInfo();
				break;
			case "ardougne":
				markerInfoString = config.ardougneMarkerInfo();
				break;
			case "hosidius":
				markerInfoString = config.hosidiusMarkerInfo();
				break;
			default:
				currentCityMarker = null;
				checkSupplies();
				return;
		}

		MarkerInfo markerToActivate = parseMarkerInfoString(markerInfoString);

		if (markerToActivate != null && markerToActivate.getId() >= 0) {
			setMarkerVisibility(markerToActivate.getId(), markerToActivate.getSourcePluginKey(),
					markerToActivate.getGroupName(), true);
			currentCityMarker = markerToActivate;
		} else {
			currentCityMarker = null;
		}

		checkSupplies();
	}

	private void checkSupplies() {
		PlankType selectedPlank = config.plankType();
		if (selectedPlank == null) {
			return;
		}

		int minPlanks;
		int minBars;

		if (config.dynamicMinimums() && currentContractNpc != null) {
			Point dynamicReqs = getCurrentDynamicMaximums();
			if (dynamicReqs != null) {
				minPlanks = dynamicReqs.x;
				minBars = dynamicReqs.y;
			} else {
				minPlanks = config.minPlanks();
				minBars = config.minSteelBars();
			}
		} else {
			minPlanks = config.minPlanks();
			minBars = config.minSteelBars();
		}

		int inventoryPlankCount = getInventoryItemCount(selectedPlank.getItemId());
		int plankSackCount = (estimatedPlankCount == -1) ? 0 : estimatedPlankCount;
		int totalPlankCount = inventoryPlankCount + plankSackCount;
		int steelBarCount = getInventoryItemCount(ItemID.STEEL_BAR);

		boolean lowPlanks = totalPlankCount < minPlanks;
		boolean lowSteel = steelBarCount < minBars;

		MarkerInfo lowPlanksMarker = parseMarkerInfoString(config.lowPlanksMarkerInfo());
		MarkerInfo lowSteelMarker = parseMarkerInfoString(config.lowSteelMarkerInfo());

		updateMarkerVisibilityBasedOnCondition(lowPlanksMarker, lowPlanks);
		updateMarkerVisibilityBasedOnCondition(lowSteelMarker, lowSteel);
	}

	private void updateMarkerVisibilityBasedOnCondition(MarkerInfo markerInfo, boolean conditionMet) {
		if (markerInfo != null && markerInfo.getId() >= 0) {
			if (conditionMet) {
				enableMarker(markerInfo);
			} else {
				if (currentCityMarker == null || markerInfo.getId() != currentCityMarker.getId()) {
					disableMarker(markerInfo);
				}
			}
		}
	}

	private void enableMarker(MarkerInfo markerInfo) {
		if (markerInfo != null && markerInfo.getId() >= 0) {
			setMarkerVisibility(markerInfo.getId(), markerInfo.getSourcePluginKey(), markerInfo.getGroupName(),
					true);
		}
	}

	private void disableMarker(MarkerInfo markerInfo) {
		if (markerInfo != null && markerInfo.getId() >= 0) {
			setMarkerVisibility(markerInfo.getId(), markerInfo.getSourcePluginKey(), markerInfo.getGroupName(), false);
		}
	}

	private void disableAllHelperMarkers() {
		disableMarker(parseMarkerInfoString(config.varrockMarkerInfo()));
		disableMarker(parseMarkerInfoString(config.faladorMarkerInfo()));
		disableMarker(parseMarkerInfoString(config.ardougneMarkerInfo()));
		disableMarker(parseMarkerInfoString(config.hosidiusMarkerInfo()));
		disableMarker(parseMarkerInfoString(config.lowPlanksMarkerInfo()));
		disableMarker(parseMarkerInfoString(config.lowSteelMarkerInfo()));
		currentCityMarker = null;
	}

	public List<MarkerInfo> getAllAvailableMarkers() {
		List<MarkerInfo> allMarkers = new ArrayList<>();
		Type screenMarkerListType = new TypeToken<List<MarkerPojo>>() {
		}.getType();
		Type screenGroupMapType = new TypeToken<java.util.Map<String, List<MarkerPojo>>>() {
		}.getType();
		java.util.Set<Long> importedMarkerIds = new java.util.HashSet<>();

		String groupMarkersJson = configManager.getConfiguration(SCREEN_MARKER_GROUPS_CONFIG_GROUP,
				SCREEN_MARKER_GROUPS_KEY);
		if (groupMarkersJson != null && !groupMarkersJson.isEmpty()) {
			try {
				java.util.Map<String, List<MarkerPojo>> groupsMap = gson.fromJson(groupMarkersJson,
						screenGroupMapType);
				if (groupsMap != null) {
					for (java.util.Map.Entry<String, List<MarkerPojo>> entry : groupsMap.entrySet()) {
						String groupName = entry.getKey();
						List<MarkerPojo> markersInGroup = entry.getValue();
						if (markersInGroup != null) {
							for (MarkerPojo m : markersInGroup) {
								allMarkers.add(
										new MarkerInfo(m.id, m.name, groupName, SCREEN_MARKER_GROUPS_KEY));
								if (m.importedId != null) {
									importedMarkerIds.add(m.importedId);
								}
							}
						}
					}
				}
			} catch (JsonSyntaxException e) {
			}
		}

		String baseMarkersJson = configManager.getConfiguration(SCREEN_MARKER_CONFIG_GROUP,
				SCREEN_MARKER_MARKERS_KEY);
		if (baseMarkersJson != null && !baseMarkersJson.isEmpty()) {
			try {
				List<MarkerPojo> baseMarkers = gson.fromJson(baseMarkersJson, screenMarkerListType);
				if (baseMarkers != null) {
					for (MarkerPojo m : baseMarkers) {
						if (!importedMarkerIds.contains(m.id)) {
							allMarkers.add(new MarkerInfo(m.id, m.name, null, SCREEN_MARKER_MARKERS_KEY));
						}
					}
				}
			} catch (JsonSyntaxException e) {
			}
		}

		allMarkers.sort((m1, m2) -> m1.getDisplayName().compareToIgnoreCase(m2.getDisplayName()));
		return allMarkers;
	}

	public void setMarkerVisibility(long markerId, String sourcePluginKey, String sourcePluginGroup, boolean visible) {
		if (markerId < 0 || sourcePluginKey == null) {
			return;
		}

		String configGroup;
		String configKey = sourcePluginKey;
		boolean found = false;

		if (SCREEN_MARKER_MARKERS_KEY.equals(configKey)) {
			configGroup = SCREEN_MARKER_CONFIG_GROUP;
		} else if (SCREEN_MARKER_GROUPS_KEY.equals(configKey)) {
			configGroup = SCREEN_MARKER_GROUPS_CONFIG_GROUP;
		} else {
			return;
		}

		String json = configManager.getConfiguration(configGroup, configKey);
		final String oldValueJson = (json == null) ? "" : json;

		if (oldValueJson.isEmpty() && !visible) {
			return; // Cannot disable if config is empty/null
		}
		if (oldValueJson.isEmpty() && visible) {
			return;
		}

		try {
			if (SCREEN_MARKER_MARKERS_KEY.equals(configKey)) {
				Type screenMarkerListType = new TypeToken<List<MarkerPojo>>() {
				}.getType();
				List<MarkerPojo> markers = gson.fromJson(json, screenMarkerListType);
				if (markers != null) {
					for (MarkerPojo marker : markers) {
						if (marker.id == markerId) {
							if (marker.visible == visible)
								return; // No change needed
							marker.visible = visible;
							found = true;
							break;
						}
					}
					if (found) {
						String updatedJson = gson.toJson(markers, screenMarkerListType);
						if (!updatedJson.equals(oldValueJson)) {
							configManager.setConfiguration(configGroup, configKey, updatedJson);
							fireConfigChanged(configGroup, configKey, oldValueJson, updatedJson);
							updateLiveMarkerState(markerId, sourcePluginKey, sourcePluginGroup, visible);
						}
					}
				}
			} else { // SCREEN_MARKER_GROUPS_KEY
				Type screenGroupMapType = new TypeToken<java.util.Map<String, List<MarkerPojo>>>() {
				}.getType();
				java.util.Map<String, List<MarkerPojo>> groupsMap = gson.fromJson(json, screenGroupMapType);

				if (sourcePluginGroup == null) {
					return;
				}

				if (groupsMap != null && groupsMap.containsKey(sourcePluginGroup)) {
					List<MarkerPojo> markersInGroup = groupsMap.get(sourcePluginGroup);
					if (markersInGroup != null) {
						for (MarkerPojo marker : markersInGroup) {
							if (marker.id == markerId) {
								if (marker.visible == visible)
									return; // No change needed
								marker.visible = visible;
								found = true;
								break;
							}
						}
					}
				}

				if (found) {
					String updatedJson = gson.toJson(groupsMap, screenGroupMapType);
					if (!updatedJson.equals(oldValueJson)) {
						configManager.setConfiguration(configGroup, configKey, updatedJson);
						fireConfigChanged(configGroup, configKey, oldValueJson, updatedJson);
						updateLiveMarkerState(markerId, sourcePluginKey, sourcePluginGroup, visible);
					}
				}
			}
		} catch (JsonSyntaxException e) {
		}
	}

	private void updateLiveMarkerState(long markerId, String sourcePluginKey, String sourcePluginGroup,
			boolean visible) {
		java.util.Collection<Plugin> plugins = pluginManager.getPlugins();

		if (SCREEN_MARKER_MARKERS_KEY.equals(sourcePluginKey)) {
			plugins.stream()
					.filter(p -> p.getClass().getName()
							.equals("net.runelite.client.plugins.screenmarkers.ScreenMarkerPlugin"))
					.findFirst()
					.ifPresent(plugin -> {
						try {
							Method getMarkersMethod = plugin.getClass().getMethod("getScreenMarkers");
							List<?> overlays = (List<?>) getMarkersMethod.invoke(plugin);
							for (Object overlay : overlays) {
								Method getMarkerMethod = overlay.getClass().getMethod("getMarker");
								Object marker = getMarkerMethod.invoke(overlay);
								Method getIdMethod = marker.getClass().getMethod("getId");
								long id = (long) getIdMethod.invoke(marker);
								if (id == markerId) {
									Method setVisibleMethod = marker.getClass().getMethod("setVisible",
											boolean.class);
									setVisibleMethod.invoke(marker, visible);
									break;
								}
							}
						} catch (Exception e) {
						}
					});
		} else if (SCREEN_MARKER_GROUPS_KEY.equals(sourcePluginKey)) {
			plugins.stream()
					.filter(p -> p.getClass().getName()
							.equals("screenmarkergroups.ScreenMarkerGroupsPlugin"))
					.findFirst()
					.ifPresent(plugin -> {
						try {
							Field groupsField = plugin.getClass().getDeclaredField("markerGroups");
							groupsField.setAccessible(true);
							@SuppressWarnings("unchecked")
							Map<String, List<?>> groupsMap = (Map<String, List<?>>) groupsField
									.get(plugin);

							if (groupsMap != null && sourcePluginGroup != null
									&& groupsMap.containsKey(sourcePluginGroup)) {
								List<?> overlaysInGroup = groupsMap.get(sourcePluginGroup);
								if (overlaysInGroup != null) {
									for (Object overlay : overlaysInGroup) {
										Method getMarkerMethod = overlay.getClass()
												.getMethod("getMarker");
										Object marker = getMarkerMethod.invoke(overlay);
										Method getIdMethod = marker.getClass().getMethod("getId");
										long id = (long) getIdMethod.invoke(marker);
										if (id == markerId) {
											Method setVisibleMethod = marker.getClass()
													.getMethod("setVisible", boolean.class);
											setVisibleMethod.invoke(marker, visible);
											break;
										}
									}
								}
							}
						} catch (Exception e) {
						}
					});
		}
	}

	private void fireConfigChanged(String group, String key, String oldValue, String newValue) {
		ConfigChanged event = new ConfigChanged();
		event.setGroup(group);
		event.setKey(key);
		event.setOldValue(oldValue);
		event.setNewValue(newValue);
		eventBus.post(event);
	}

	private MarkerInfo parseMarkerInfoString(String info) {
		if (info == null || info.isEmpty()) {
			return null;
		}
		String[] parts = info.split("\\" + INFO_SEPARATOR, 3);
		if (parts.length != 3) {
			return null;
		}
		try {
			String key = parts[0];
			String group = "null".equalsIgnoreCase(parts[1]) ? null : parts[1];
			long id = Long.parseLong(parts[2]);
			if (id < 0) {
				return null;
			}
			return new MarkerInfo(id, null, group, key);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private int getInventoryItemCount(int itemId) {
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) {
			return 0;
		}
		return inventory.count(itemId);
	}

	private BufferedImage createPlaceholderIcon(int width, int height, Color color) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setColor(color);
			g.fillRect(0, 0, width, height);
		} finally {
			g.dispose();
		}
		return image;
	}

	private void setEstimatedPlankCount(int count) {
		if (count != -1) {
			estimatedPlankCount = Ints.constrainToRange(count, 0, 28);
		} else {
			estimatedPlankCount = -1;
		}
		configManager.setRSProfileConfiguration(MahoganyHomesHelperConfig.SACK_CONFIG_GROUP,
				MahoganyHomesHelperConfig.SACK_KEY, estimatedPlankCount);
		clientThread.invokeLater(this::checkSupplies);
	}

	private Multiset<Integer> createSnapshot(ItemContainer container) {
		if (container == null) {
			return null;
		}
		Multiset<Integer> snapshot = HashMultiset.create();
		Arrays.stream(container.getItems())
				.filter(item -> item != null && PLANKS.contains(item.getId()))
				.forEach(i -> snapshot.add(i.getId(), i.getQuantity()));
		return snapshot;
	}

	private Point getCurrentDynamicMaximums() {
		if (currentContractNpc == null || currentContractCity == null || config.plankType() == null) {
			return null;
		}

		Point reqs = NPC_REQUIREMENTS.getOrDefault(currentContractCity, Collections.emptyMap())
				.getOrDefault(currentContractNpc.toLowerCase(), Collections.emptyMap())
				.get(config.plankType());
		return reqs;
	}

	public void fetchAndUpdateDynamicSpinners() {
		if (!config.dynamicMinimums()) {
			return;
		}
		Point reqs = getCurrentDynamicMaximums();
		if (reqs != null && panel != null) {
			SwingUtilities.invokeLater(() -> panel.updateDynamicMinimumSpinners(reqs.x, reqs.y));
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("mahoganyhomeshelper")) {
			return;
		}

		if (config.dynamicMinimums() && "plankType".equals(event.getKey())) {
			fetchAndUpdateDynamicSpinners();
		}

		if ("dynamicMinimums".equals(event.getKey())) {
			boolean isEnabled = Boolean.parseBoolean(event.getNewValue());
			if (isEnabled) {
				fetchAndUpdateDynamicSpinners();
			}
		}
	}

	@Provides
	MahoganyHomesHelperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(MahoganyHomesHelperConfig.class);
	}
}
