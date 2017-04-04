package com.itranswarp.crypto.ui;

import java.util.Arrays;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class AgentClient {

	public static void main(String[] args) throws Exception {
		Terminal terminal = new DefaultTerminalFactory().createTerminal();
		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();
		Panel panel = new Panel(new BorderLayout());

		Table<String> table = new Table<>("- Quotation -");
		table.getTableModel().addRow("135.09");
		table.getTableModel().addRow("134.56");
		table.getTableModel().addRow("134.27");
		table.getTableModel().addRow("133.90");
		table.getTableModel().addRow("132.81");
		table.setLayoutData(BorderLayout.Location.RIGHT);

		panel.addComponent(table);

		TextBox textBox = new TextBox("EMPTY", TextBox.Style.MULTI_LINE);
		textBox.setLayoutData(BorderLayout.Location.CENTER);
		panel.addComponent(textBox);

		// Create window to hold the panel
		BasicWindow window = new BasicWindow();
		window.setComponent(panel);
		window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));

		// Create gui and start gui
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
				new EmptySpace(TextColor.ANSI.BLUE));
		gui.addWindowAndWait(window);
	}
}
