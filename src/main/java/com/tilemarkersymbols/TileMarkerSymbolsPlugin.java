package com.tilemarkersymbols;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Tile Marker Symbols",
	description = "Provides quick access to Unicode symbols for tile marker labels.",
	tags = {"tile", "marker", "symbols", "labels", "unicode"}
)
public class TileMarkerSymbolsPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "tilemarkersymbols";
	private static final String SAVED_MARKERS_KEY = "savedMarkers";

	private static final String MARK_TILE_OPTION = "Mark with Symbol";
	private static final String CLEAR_TILE_OPTION = "Clear Symbol Marker";
	private static final String LABEL_TILE_OPTION = "Label Symbol Marker";
	private static final String COLOR_TILE_OPTION = "Color Symbol Marker";

	private static final String GROUND_MARKER_CONFIG_GROUP = "groundMarker";
	private static final String GROUND_MARKER_COLOR_KEY = "markerColor";
	private static final Color DEFAULT_MARKER_COLOR = Color.YELLOW;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TileMarkerSymbolsOverlay overlay;

	@Inject
	private TileMarkerSymbolsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ColorPickerManager colorPickerManager;

	private final List<SymbolTileMarker> markers = new ArrayList<>();

	private TileMarkerSymbolsPanel panel;
	private NavigationButton navButton;
	private boolean sidebarPanelAdded = false;
	private String selectedSymbol = "";

	@Override
	protected void startUp()
	{
		loadMarkers();

		panel = new TileMarkerSymbolsPanel(config, this);

		navButton = NavigationButton.builder()
			.tooltip("Tile Marker Symbols")
			.icon(createIcon())
			.priority(5)
			.panel(panel)
			.build();

		updateSidebarPanel();

		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		removeSidebarPanel();

		panel = null;
		navButton = null;
		markers.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		if (panel != null)
		{
			panel.rebuild();
		}

		updateSidebarPanel();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!shouldShowMarkerMenu())
		{
			return;
		}

		MenuAction menuAction = event.getMenuEntry().getType();

		if (menuAction != MenuAction.WALK && menuAction != MenuAction.SET_HEADING)
		{
			return;
		}

		int worldViewId = event.getMenuEntry().getWorldViewId();
		WorldView worldView = client.getWorldView(worldViewId);

		if (worldView == null)
		{
			return;
		}

		Tile selectedSceneTile = worldView.getSelectedSceneTile();

		if (selectedSceneTile == null)
		{
			return;
		}

		final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, selectedSceneTile.getLocalLocation());

		if (worldPoint == null)
		{
			return;
		}

		final SymbolTileMarker existingMarker = getMarker(worldPoint);

		if (selectedSymbol != null && !selectedSymbol.isEmpty())
		{
			client.createMenuEntry(-1)
				.setOption(MARK_TILE_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> markTile(worldPoint, selectedSymbol));
		}

		if (existingMarker != null)
		{
			client.createMenuEntry(-2)
				.setOption(CLEAR_TILE_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> clearMarker(worldPoint));

			client.createMenuEntry(-3)
				.setOption(LABEL_TILE_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> labelTile(existingMarker));

			MenuEntry colorMenuEntry = client.createMenuEntry(-4)
				.setOption(COLOR_TILE_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE);

			Menu colorSubMenu = colorMenuEntry.createSubMenu();

			colorSubMenu.createMenuEntry(-1)
				.setOption("Pick")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> openColorPicker(existingMarker));

			colorSubMenu.createMenuEntry(-2)
				.setOption("Reset")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> colorTile(existingMarker, null));

			List<Color> existingColors = markers.stream()
				.map(SymbolTileMarker::getColor)
				.filter(color -> color != null)
				.distinct()
				.collect(Collectors.toList());

			for (Color color : existingColors)
			{
				if (!color.equals(existingMarker.getColor()))
				{
					colorSubMenu.createMenuEntry(-1)
						.setOption(ColorUtil.prependColorTag("Color", color))
						.setType(MenuAction.RUNELITE)
						.onClick(e -> colorTile(existingMarker, color));
				}
			}
		}
	}

	void setSelectedSymbol(String selectedSymbol)
	{
		this.selectedSymbol = selectedSymbol;
	}

	String getSelectedSymbol()
	{
		return selectedSymbol;
	}

	Collection<SymbolTileMarker> getMarkers()
	{
		return markers;
	}

	void undoLastMarker()
	{
		if (markers.isEmpty())
		{
			return;
		}

		markers.remove(markers.size() - 1);
		saveMarkers();
	}

	void clearAllMarkers()
	{
		markers.clear();
		saveMarkers();
	}

	private boolean shouldShowMarkerMenu()
	{
		if (config.markMenuMode() == TileMarkerSymbolsConfig.MarkMenuMode.SHIFT_RIGHT_CLICK)
		{
			return client.isKeyPressed(KeyCode.KC_SHIFT);
		}

		return true;
	}

	private void updateSidebarPanel()
	{
		if (navButton == null)
		{
			return;
		}

		if (config.showSidebarPanel())
		{
			addSidebarPanel();
		}
		else
		{
			removeSidebarPanel();
		}
	}

	private void addSidebarPanel()
	{
		if (navButton != null && !sidebarPanelAdded)
		{
			clientToolbar.addNavigation(navButton);
			sidebarPanelAdded = true;
		}
	}

	private void removeSidebarPanel()
	{
		if (navButton != null && sidebarPanelAdded)
		{
			clientToolbar.removeNavigation(navButton);
			sidebarPanelAdded = false;
		}
	}

	private void markTile(WorldPoint worldPoint, String label)
	{
		if (worldPoint == null)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Could not mark tile: no tile found.", null);
			return;
		}

		if (label == null || label.isEmpty())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Could not mark tile: no symbol selected.", null);
			return;
		}

		clearMarker(worldPoint);

		markers.add(new SymbolTileMarker(worldPoint, label, null));
		saveMarkers();
	}

	private void labelTile(SymbolTileMarker existingMarker)
	{
		WorldPoint worldPoint = existingMarker.getWorldPoint();
		Color color = existingMarker.getColor();
		String currentLabel = existingMarker.getLabel() == null ? "" : existingMarker.getLabel();

		SwingUtilities.invokeLater(() ->
		{
			String input = (String) JOptionPane.showInputDialog(
				null,
				"Enter symbol marker label:",
				"Symbol marker label",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				currentLabel
			);

			if (input != null)
			{
				updateMarker(worldPoint, input.trim(), color);
			}
		});
	}

	private void openColorPicker(SymbolTileMarker existingMarker)
	{
		Color startingColor = existingMarker.getColor();

		if (startingColor == null)
		{
			startingColor = getGroundMarkerDefaultColor();
		}

		Color finalStartingColor = startingColor;

		SwingUtilities.invokeLater(() ->
		{
			RuneliteColorPicker colorPicker = colorPickerManager.create(
				client,
				finalStartingColor,
				"Symbol tile marker color",
				false
			);

			colorPicker.setOnClose(color -> colorTile(existingMarker, color));
			colorPicker.setVisible(true);
		});
	}

	private void colorTile(SymbolTileMarker existingMarker, Color color)
	{
		updateMarker(existingMarker.getWorldPoint(), existingMarker.getLabel(), color);
	}

	private void updateMarker(WorldPoint worldPoint, String label, Color color)
	{
		clearMarker(worldPoint);
		markers.add(new SymbolTileMarker(worldPoint, label, color));
		saveMarkers();
	}

	private SymbolTileMarker getMarker(WorldPoint worldPoint)
	{
		if (worldPoint == null)
		{
			return null;
		}

		for (SymbolTileMarker marker : markers)
		{
			if (marker.getWorldPoint().equals(worldPoint))
			{
				return marker;
			}
		}

		return null;
	}

	private void clearMarker(WorldPoint worldPoint)
	{
		if (worldPoint == null)
		{
			return;
		}

		Iterator<SymbolTileMarker> iterator = markers.iterator();

		while (iterator.hasNext())
		{
			SymbolTileMarker marker = iterator.next();

			if (marker.getWorldPoint().equals(worldPoint))
			{
				iterator.remove();
			}
		}

		saveMarkers();
	}

	private Color getGroundMarkerDefaultColor()
	{
		Color color = configManager.getConfiguration(
			GROUND_MARKER_CONFIG_GROUP,
			GROUND_MARKER_COLOR_KEY,
			Color.class
		);

		if (color == null)
		{
			return DEFAULT_MARKER_COLOR;
		}

		return color;
	}

	private void loadMarkers()
	{
		markers.clear();

		String savedMarkers = config.savedMarkers();

		if (savedMarkers == null || savedMarkers.trim().isEmpty())
		{
			return;
		}

		String[] lines = savedMarkers.split("\\R");

		for (String line : lines)
		{
			String[] parts = line.split(":", -1);

			if (parts.length < 4)
			{
				continue;
			}

			try
			{
				int x = Integer.parseInt(parts[0]);
				int y = Integer.parseInt(parts[1]);
				int plane = Integer.parseInt(parts[2]);
				String label = new String(Base64.getDecoder().decode(parts[3]), StandardCharsets.UTF_8);

				Color color = null;

				if (parts.length >= 5 && parts[4] != null && !parts[4].isEmpty())
				{
					color = new Color(Integer.parseInt(parts[4]), true);
				}

				markers.add(new SymbolTileMarker(new WorldPoint(x, y, plane), label, color));
			}
			catch (Exception ignored)
			{
			}
		}
	}

	private void saveMarkers()
	{
		StringBuilder builder = new StringBuilder();

		for (SymbolTileMarker marker : markers)
		{
			WorldPoint worldPoint = marker.getWorldPoint();
			String label = marker.getLabel() == null ? "" : marker.getLabel();
			String encodedLabel = Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8));
			String colorValue = marker.getColor() == null ? "" : Integer.toString(marker.getColor().getRGB());

			builder
				.append(worldPoint.getX())
				.append(":")
				.append(worldPoint.getY())
				.append(":")
				.append(worldPoint.getPlane())
				.append(":")
				.append(encodedLabel)
				.append(":")
				.append(colorValue)
				.append("\n");
		}

		configManager.setConfiguration(CONFIG_GROUP, SAVED_MARKERS_KEY, builder.toString());
	}

	@Provides
	TileMarkerSymbolsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TileMarkerSymbolsConfig.class);
	}

	private BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setColor(Color.WHITE);
		graphics.drawString("★", 3, 13);

		graphics.dispose();
		return image;
	}
}