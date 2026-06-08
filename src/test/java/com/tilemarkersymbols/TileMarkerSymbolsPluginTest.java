package com.tilemarkersymbols;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TileMarkerSymbolsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TileMarkerSymbolsPlugin.class);
		RuneLite.main(args);
	}
}