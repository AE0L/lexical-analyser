package app.ppl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	JTextArea txtArea;
	JScrollPane scroll;

	public Main() {
		this.txtArea = new JTextArea(30, 30);
		this.scroll = new JScrollPane(txtArea);

		txtArea.setEditable(false);
		this.setSize(400, 650);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnfile = new JButton("Select Input file and Validate");
		JPanel panel = new JPanel();

		panel.add(btnfile);

		JLabel label = new JLabel("No file Selected");

		panel.add(label);
		panel.add(scroll);
		this.add(panel);

		btnfile.addActionListener(event -> {
			String file;
			String path;
			int response;

			JFileChooser chooser = new JFileChooser(".");

			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			response = chooser.showOpenDialog(null);

			if (response == JFileChooser.APPROVE_OPTION) {

				file = chooser.getSelectedFile().getName();
				// print the content of input file in the textarea
				path = chooser.getSelectedFile().getAbsolutePath();
				try {
					FileReader read = new FileReader(path);
					BufferedReader br = new BufferedReader(read);
					txtArea.read(br, null);
					br.close();
					txtArea.requestFocus();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1);
				}

				try {
					valid(file);
				} catch (IOException e) {
					e.printStackTrace();
				}

				label.setText(chooser.getSelectedFile().getName() + " file is selected.");
			} else {
				label.setText("The user cancelled the operation.");
				txtArea.setText("");
			}
		});

	}

	public static void valid(String fileName) throws IOException {
		Language polytechnica = new Language("symbol_table.txt");
		Lexer lexer = new Lexer(polytechnica, Files.readString(Paths.get(fileName)));
		ArrayList<Token> tokens = lexer.generateTokens();
		FileWriter out = new FileWriter("output.txt");

		int maxSymbolLength = 0;
		int maxTokenLength = 0;

		for (Token token : tokens) {
			if (token.getSymbol().length() > maxSymbolLength) {
				maxSymbolLength = token.getSymbol().length();
			}

			if (token.getType().length() > maxTokenLength) {
				maxTokenLength = token.getType().length();
			}
		}

		maxSymbolLength += 3;
		maxTokenLength += 3;

		out.write(String.format("%1$-" + maxSymbolLength + "s %2$-" + maxTokenLength + "sDEFINITION\n\n", "LEXEME", "TOKEN"));

		for (Token token : tokens) {
			String symbol = token.getSymbol();
			String type = token.getType();
			try {
				out.write(String.format("%1$-" + maxSymbolLength + "s %2$-" + maxTokenLength + "s", symbol, type));
				out.write(token.getDefinition() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		out.close();
	}

	public static void main(String[] args) {
		new Main();
	}

}
