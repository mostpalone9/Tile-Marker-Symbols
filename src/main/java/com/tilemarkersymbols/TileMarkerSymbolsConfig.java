package com.tilemarkersymbols;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("tilemarkersymbols")
public interface TileMarkerSymbolsConfig extends Config
{
	@ConfigSection(
		name = "Favorites",
		description = "Configure favorite Unicode tile marker symbols.",
		position = 0
	)
	String favoritesSection = "favorites";

	@ConfigSection(
		name = "Display",
		description = "Configure plugin display options.",
		position = 1
	)
	String displaySection = "display";

	@ConfigItem(
		keyName = "favorites",
		name = "Favorite symbols",
		description = "Symbols to show in the sidebar. Separate symbols with spaces.",
		position = 1,
		section = favoritesSection
	)
	default String favorites()
	{
		return "✓ ✕ ★ ☆ → ← ↑ ↓ ⚠ ⊙ ● ○ ◆ ◇";
	}

	@ConfigItem(
		keyName = "showSidebarPanel",
		name = "Show sidebar panel",
		description = "Shows the Tile Marker Symbols sidebar panel.",
		position = 1,
		section = displaySection
	)
	default boolean showSidebarPanel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "markMenuMode",
		name = "Show mark option",
		description = "Configures whether the Mark with Symbol option appears on right-click or only while shift-right-clicking.",
		position = 2,
		section = displaySection
	)
	default MarkMenuMode markMenuMode()
	{
		return MarkMenuMode.RIGHT_CLICK;
	}

	@ConfigItem(
		keyName = "savedMarkers",
		name = "Saved markers",
		description = "Saved symbol tile markers.",
		hidden = true
	)
	default String savedMarkers()
	{
		return "";
	}

	enum MarkMenuMode
	{
		RIGHT_CLICK("Right-click"),
		SHIFT_RIGHT_CLICK("Shift-right-click");

		private final String name;

		MarkMenuMode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}