# Tile Marker Symbols

Tile Marker Symbols is a RuneLite plugin that makes it easy to mark tiles with Unicode symbols, short labels, and custom colors.

It is designed as a companion-style upgrade to RuneLite's Ground Markers workflow. The plugin mirrors Ground Markers' visual settings for marker color, border width, and fill opacity, while adding a symbol-focused sidebar and quick right-click marking options.

## Features

* Sidebar panel with configurable favorite symbols
* Click a symbol to select it and copy it to clipboard
* Right-click or shift-right-click a tile to mark it with the selected symbol
* Ground Markers-style tile rendering; inherits Ground Markers visual settings by default
* Customizable tile labels and colors
* Undo the last marker or clear all tiles marked with symbols
* Clear-all confirmation to help prevent accidental marker wipes
* Persistent saved markers

## Default Symbols

The default symbol palette includes:

```text
✓ ✕ ★ ☆ → ← ↑ ↓ ⚠ ⊙ ● ○ ◆ ◇
```

These can be changed in the plugin config under **Favorite symbols**. Most Unicode symbols should work, though display may depend on client font support.

## Usage

1. Enable **Tile Marker Symbols**.
2. Open the sidebar panel.
3. Click a symbol to select it.
4. Right-click a tile and choose **Mark with Symbol**.
5. Right-click an existing symbol marker to clear it, relabel it, or change its color.

The selected symbol is also copied to your system clipboard when clicked.

## Configuration

### Favorite symbols

Controls which symbols appear in the sidebar panel.

Symbols should be separated by spaces.

Example:

```text
✓ ✕ ★ ☆ → ← ↑ ↓ ⚠ ⊙ ● ○ ◆ ◇
```

### Show sidebar panel

Shows or hides the Tile Marker Symbols sidebar panel.

### Show mark option

Controls when the **Mark with Symbol** option appears:

* Right-click
* Shift-right-click

## Relationship to Ground Markers

Tile Marker Symbols is intended to feel visually consistent with Ground Markers.

By default, symbol markers use the user's Ground Markers settings for:

* Marker color
* Border width
* Fill opacity

Individual symbol markers can still be given their own custom colors.

## Notes

Tile Marker Symbols stores its own markers separately from RuneLite's built-in Ground Markers plugin. This avoids modifying Ground Markers' internal saved marker data while still providing a similar visual style and workflow.

I'm working on getting this plugin more directly implemented with Ground Markers to make the user experience even more seamless.

Please feel free to make suggestions, report bugs, or fork the plugin.

## Credits & Donations

Created by ShesXPWaste. If you're enjoying this plugin and would like to show support, you may donate via [PayPal](https://paypal.me/brendanhulla99)
