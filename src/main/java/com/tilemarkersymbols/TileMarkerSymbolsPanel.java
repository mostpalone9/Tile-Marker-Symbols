package com.tilemarkersymbols;

import net.runelite.client.ui.PluginPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

class TileMarkerSymbolsPanel extends PluginPanel
{
	private static final Color SELECTED_BUTTON_COLOR = new Color(70, 70, 70);
	private static final Color NORMAL_BUTTON_COLOR = new Color(35, 35, 35);

	private final TileMarkerSymbolsConfig config;
	private final TileMarkerSymbolsPlugin plugin;
	private final JPanel symbolPanel = new JPanel();
	private final JLabel selectedLabel = new JLabel("No symbol selected", SwingConstants.CENTER);
	private final Map<String, JButton> symbolButtons = new HashMap<>();

	TileMarkerSymbolsPanel(TileMarkerSymbolsConfig config, TileMarkerSymbolsPlugin plugin)
	{
		this.config = config;
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		JLabel title = new JLabel("Tile Marker Symbols", SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
		add(title, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setOpaque(false);

		symbolPanel.setLayout(new GridLayout(0, 4, 6, 6));
		symbolPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 20, 0));
		centerPanel.add(symbolPanel, BorderLayout.NORTH);

		selectedLabel.setFont(selectedLabel.getFont().deriveFont(Font.BOLD, 18f));
		selectedLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 20, 0));
		centerPanel.add(selectedLabel, BorderLayout.CENTER);

		JPanel actionPanel = new JPanel(new GridLayout(0, 1, 0, 6));
		actionPanel.setOpaque(false);

		JButton undoButton = new JButton("Undo last marker");
		undoButton.setFocusPainted(false);
		undoButton.addActionListener(e -> plugin.undoLastMarker());
		actionPanel.add(undoButton);

		JButton clearButton = new JButton("Clear all symbol markers");
		clearButton.setFocusPainted(false);
		clearButton.addActionListener(e -> confirmClearAllMarkers());
		actionPanel.add(clearButton);

		centerPanel.add(actionPanel, BorderLayout.SOUTH);

		add(centerPanel, BorderLayout.CENTER);

		rebuild();
	}

	void rebuild()
	{
		symbolPanel.removeAll();
		symbolButtons.clear();

		String favorites = config.favorites();

		if (favorites == null || favorites.trim().isEmpty())
		{
			symbolPanel.add(new JLabel("No symbols configured."));
		}
		else
		{
			String[] symbols = favorites.trim().split("\\s+");

			for (String symbol : symbols)
			{
				JButton button = new JButton(symbol);

				button.setFocusPainted(false);
				button.setPreferredSize(new Dimension(52, 44));
				button.setFont(button.getFont().deriveFont(Font.BOLD, 20f));
				button.setToolTipText("Select and copy " + symbol);
				button.setBackground(NORMAL_BUTTON_COLOR);
				button.setOpaque(true);

				button.addActionListener(e -> selectSymbol(symbol));

				symbolButtons.put(symbol, button);
				symbolPanel.add(button);
			}
		}

		updateSelectedDisplay();

		symbolPanel.revalidate();
		symbolPanel.repaint();
	}

	private void selectSymbol(String symbol)
	{
		plugin.setSelectedSymbol(symbol);
		copyToClipboard(symbol);
		updateSelectedDisplay();
	}

	private void updateSelectedDisplay()
	{
		String selectedSymbol = plugin.getSelectedSymbol();

		for (Map.Entry<String, JButton> entry : symbolButtons.entrySet())
		{
			boolean isSelected = entry.getKey().equals(selectedSymbol);
			entry.getValue().setBackground(isSelected ? SELECTED_BUTTON_COLOR : NORMAL_BUTTON_COLOR);
		}

		if (selectedSymbol == null || selectedSymbol.isEmpty())
		{
			selectedLabel.setText(
				"<html><center>" +
					"Selected Symbol<br>" +
					"<span style='font-size:22px;'>None</span>" +
				"</center></html>"
			);
		}
		else
		{
			selectedLabel.setText(
				"<html><center>" +
					"✓ Selected & Copied<br>" +
					"<span style='font-size:36px;'>" +
						selectedSymbol +
					"</span><br>" +
					"<span style='font-size:11px;'>Right-click a tile to mark it.</span>" +
				"</center></html>"
			);
		}
	}

	private void confirmClearAllMarkers()
	{
		int result = JOptionPane.showConfirmDialog(
			this,
			"Clear all tiles marked with symbols?",
			"Clear symbol markers",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION)
		{
			plugin.clearAllMarkers();
		}
	}

	private void copyToClipboard(String text)
	{
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
	}
}