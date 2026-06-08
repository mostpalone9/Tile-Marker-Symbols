package com.tilemarkersymbols;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.awt.Color;

@Getter
@AllArgsConstructor
class SymbolTileMarker
{
	private final WorldPoint worldPoint;
	private final String label;
	private final Color color;
}