package com.tilemarkersymbols;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;

class TileMarkerSymbolsOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;

	private static final String GROUND_MARKER_CONFIG_GROUP = "groundMarker";
	private static final String MARKER_COLOR_KEY = "markerColor";
	private static final String BORDER_WIDTH_KEY = "borderWidth";
	private static final String FILL_OPACITY_KEY = "fillOpacity";

	private static final Color DEFAULT_MARKER_COLOR = Color.YELLOW;
	private static final double DEFAULT_BORDER_WIDTH = 2.0;
	private static final int DEFAULT_FILL_OPACITY = 50;

	private final Client client;
	private final ConfigManager configManager;
	private final TileMarkerSymbolsPlugin plugin;

	@Inject
	TileMarkerSymbolsOverlay(
		Client client,
		ConfigManager configManager,
		TileMarkerSymbolsPlugin plugin
	)
	{
		this.client = client;
		this.configManager = configManager;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WorldView worldView = client.getTopLevelWorldView();

		if (worldView == null)
		{
			return null;
		}

		graphics.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON
		);

		int fillOpacity = getGroundMarkerFillOpacity();
		double borderWidth = getGroundMarkerBorderWidth();

		Stroke stroke = new BasicStroke((float) borderWidth);
		Color fillColor = new Color(0, 0, 0, fillOpacity);

		for (SymbolTileMarker marker : plugin.getMarkers())
		{
			Color markerColor = marker.getColor();

			if (markerColor == null)
			{
				markerColor = getGroundMarkerColor();
			}

			drawTile(graphics, worldView, marker, markerColor, fillColor, stroke);
		}

		return null;
	}

	private void drawTile(
		Graphics2D graphics,
		WorldView worldView,
		SymbolTileMarker marker,
		Color markerColor,
		Color fillColor,
		Stroke stroke
	)
	{
		WorldPoint worldPoint = marker.getWorldPoint();

		if (worldPoint.getPlane() != worldView.getPlane())
		{
			return;
		}

		Player player = client.getLocalPlayer();

		if (player != null && player.getWorldView().isTopLevel())
		{
			WorldPoint playerLocation = player.getWorldLocation();

			if (worldPoint.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
			{
				return;
			}
		}

		LocalPoint localPoint = LocalPoint.fromWorld(worldView, worldPoint);

		if (localPoint == null)
		{
			return;
		}

		Polygon tilePolygon = Perspective.getCanvasTilePoly(client, localPoint);

		if (tilePolygon != null)
		{
			OverlayUtil.renderPolygon(
				graphics,
				tilePolygon,
				markerColor,
				fillColor,
				stroke
			);
		}

		String label = marker.getLabel();

		if (label == null || label.isEmpty())
		{
			return;
		}

		Point textLocation = Perspective.getCanvasTextLocation(
			client,
			graphics,
			localPoint,
			label,
			0
		);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(
				graphics,
				textLocation,
				label,
				markerColor
			);
		}
	}

	private Color getGroundMarkerColor()
	{
		Color color = configManager.getConfiguration(
			GROUND_MARKER_CONFIG_GROUP,
			MARKER_COLOR_KEY,
			Color.class
		);

		if (color == null)
		{
			return DEFAULT_MARKER_COLOR;
		}

		return color;
	}

	private double getGroundMarkerBorderWidth()
	{
		Double borderWidth = configManager.getConfiguration(
			GROUND_MARKER_CONFIG_GROUP,
			BORDER_WIDTH_KEY,
			double.class
		);

		if (borderWidth == null)
		{
			return DEFAULT_BORDER_WIDTH;
		}

		return borderWidth;
	}

	private int getGroundMarkerFillOpacity()
	{
		Integer fillOpacity = configManager.getConfiguration(
			GROUND_MARKER_CONFIG_GROUP,
			FILL_OPACITY_KEY,
			int.class
		);

		if (fillOpacity == null)
		{
			return DEFAULT_FILL_OPACITY;
		}

		return Math.max(0, Math.min(255, fillOpacity));
	}
}