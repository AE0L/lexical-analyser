package app.ppl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UI {

	public static void main(String[] args) {
		Validator validator = new Validator();
		JFrame frame = new JFrame("FILE");
		JTextArea txtarea = new JTextArea(30, 30);
		JScrollPane scroll = new JScrollPane(txtarea);

		txtarea.setEditable(false);
		frame.setSize(400, 650);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnfile = new JButton("Select Input file and Validate");
		JPanel panel = new JPanel();

		panel.add(btnfile);

		JLabel label = new JLabel("No file Selected");

		panel.add(label);
		panel.add(scroll);
		frame.add(panel);

		btnfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				String file, path;
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
						txtarea.read(br, null);
						br.close();
						txtarea.requestFocus();
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
					txtarea.setText("");
				}
			}

		});

	}

	public static void valid(String f) throws IOException {
		Validator validator = new Validator();
		/*
		InputScanner inputScanner = new InputScanner(f);
		FileWriter out = new FileWriter("output.txt");

		out.write("TOKEN \t \t VALIDITY\n\n");
		inputScanner.getSymbols().forEach(input -> {
			try {
				out.write(input + "\t " + "\t\t" + validator.validate(input) + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		out.close();
		*/
	}

}
