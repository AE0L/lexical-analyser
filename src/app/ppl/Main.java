package app.ppl;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends JFrame {

    private static final long serialVersionUID = 1L;


    private JPanel panel;
    public String file,path;
    public static JTextArea areaOutput;
    
    public Main() {
        
        
        this.setTitle("Polytechnica Lexical Analyer");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.setBounds(100, 100, 1000, 700);
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,5,5));	
        this.setContentPane(panel);
        panel.setLayout(null);
        
        JLabel poly = new JLabel("POLYTECHNICA LEXICAL ANALYZER");
        poly.setForeground(Color.RED);
        poly.setBounds(210, 30,550, 31);  
        poly.setFont(new Font("Tahoma",Font.BOLD,25));
        poly.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(poly);
        
        JLabel inputLabel = new JLabel("INPUT");
        inputLabel.setFont(new Font("Tahoma",Font.BOLD,18));
        inputLabel.setBounds(50, 100, 88, 14);
        panel.add(inputLabel);
        
        JTextArea areaInput = new JTextArea();
        areaInput.setEditable(false);
        areaInput.setLineWrap(false);
        
        JScrollPane scrollInput = new JScrollPane(areaInput);
        scrollInput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollInput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollInput.setBounds(50,120,387,314);
        panel.add(scrollInput);
        
        JLabel outputLabel = new JLabel("OUTPUT");
        outputLabel.setFont(new Font("Tahoma",Font.BOLD,18));
        outputLabel.setBounds(550, 100, 88, 14);
        panel.add(outputLabel);
        
        areaOutput = new JTextArea();
        areaOutput.setEditable(false);
        areaOutput.setLineWrap(false);
        
        JScrollPane scrollOutput = new JScrollPane(areaOutput);
        scrollOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollOutput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollOutput.setBounds(550,120,387,314);
        panel.add(scrollOutput);
        
        JLabel select = new JLabel("SELECT INPUT FILE");
        select.setFont(new Font("Tahoma",Font.PLAIN,18));
        select.setHorizontalAlignment(SwingConstants.CENTER);
        select.setBounds(300,475,375,14);
        panel.add(select);
        
        JButton openFile = new JButton("OPEN FILE");
        openFile.setBounds(435,500,105,25);
        panel.add(openFile);
        
        JLabel status = new JLabel("No input file selected");
        status.setHorizontalAlignment(SwingConstants.CENTER);
        status.setBounds(420,525,130,25);
        panel.add(status);
        
        JButton analyze = new JButton("ANALYZE");
        analyze.setBounds(435, 575, 105, 25);
        panel.add(analyze);
        
        openFile.addActionListener(event -> {
        
            int response;

            JFileChooser chooser = new JFileChooser(".");
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("POLY File", "poly"));
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            response = chooser.showOpenDialog(null);

            if (response == JFileChooser.APPROVE_OPTION) {

                // print the content of input file in the textarea
                path = chooser.getSelectedFile().getAbsolutePath();
                try {
                    FileReader read = new FileReader(path);
                    BufferedReader br = new BufferedReader(read);
                    areaInput.read(br, null);
                    br.close();
                    areaInput.requestFocus();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, e1);
                }

                status.setText(chooser.getSelectedFile().getName() + " file is selected.");
            } else {
                status.setText("The user cancelled the operation.");
                areaInput.setText("");
            }
        });
        
        analyze.addActionListener(event ->{
            
            try {
                valid(path);
                JOptionPane.showMessageDialog(null,"Successfully Analyzed.","Analyzed",JOptionPane.INFORMATION_MESSAGE); 
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        });
        

    }

    public static void valid(String fileName) throws IOException {
        Language polytechnica = new Language("symbol_table.txt");
        Lexer lexer = new Lexer(polytechnica, Files.readString(Paths.get(fileName)).trim());
        ArrayList<Token> tokens = lexer.generateTokens();
        FileWriter out = new FileWriter("output.txt");
        Parser parser = new Parser(tokens, polytechnica);
        ArrayList<ParseResult> stms = parser.parse();

        int maxSymbolLength = 0;
        int maxTokenLength = 0;
        int maxStatementLength = 0;

        for (Statement stm : stms) {
            System.out.println(ParseUtils.toString(stm));
            System.out.println(stm.isValid());
            System.out.println();

            int stmLen = ParseUtils.toString(stm).length();

            if (stmLen > maxStatementLength) {
                maxStatementLength = stmLen;
            }
        }

        for (Statement stm : stms) {
            String stmStr = ParseUtils.toString(stm);
            boolean valid = stm.isValid();

            out.write(String.format("%1$-" + maxStatementLength + "s %2$s", stmStr, valid ? "VALID" : "INVALID"));
            out.write("\n");
        }

        out.close();

        out = new FileWriter("output_token.txt");

        for (Token token : tokens) {
        	if (token.getType().equals("MULTI_COMMENT")) {
        		BufferedReader br = new BufferedReader(new StringReader(token.getSymbol()));

        		String line = "";

        		while ((line = br.readLine()) != null) {
        			if (line.length() > maxSymbolLength) {
        				maxSymbolLength = line.length();
        			}
        		}

        		br.close();
        	} else {
        		if (token.getSymbol().length() > maxSymbolLength) {
        			maxSymbolLength = token.getSymbol().length();
        		}

        		if (token.getType().length() > maxTokenLength) {
        			maxTokenLength = token.getType().length();
        		}
        	}

        }

        maxSymbolLength += 3;
        maxTokenLength += 3;

        out.write(String.format("%1$-" + maxSymbolLength + "s %2$-" + maxTokenLength + "sDEFINITION\n\n", "LEXEME", "TOKEN"));

        areaOutput.append("LEXEME \t\t TOKEN \t\t\t DEFINITION");
        areaOutput.append("\n\n");
        
        for (Token token : tokens) {
            String symbol = token.getSymbol();
            String type = token.getType();

            try {
                if (type.equals("MULTI_COMMENT")) {
                    BufferedReader br = new BufferedReader(new StringReader(symbol));

                    String line = br.readLine();
                    String nextLine = br.readLine();

                    while (line != null) {
                        if (nextLine == null) {
                            out.write(String.format("%1$-" + maxSymbolLength + "s %2$-" + maxTokenLength + "s", line, type));

                            areaOutput.append(line);
                            areaOutput.append(" \t\t ");
                            areaOutput.append(type);
                            areaOutput.append(" \t\t ");
                        } else {
                            out.write(line + "\n");
                            areaOutput.append(line + "\n");
                        }

                        line = nextLine;
                        nextLine = br.readLine();
                    }

                    br.close();
                } else {
                    out.write(String.format("%1$-" + maxSymbolLength + "s %2$-" + maxTokenLength + "s", symbol, type));
                    areaOutput.append(symbol);
                    areaOutput.append(" \t\t ");
                    areaOutput.append(type);
                    areaOutput.append(" \t\t ");
                }

                out.write(token.getDefinition() + "\n");
                areaOutput.append(token.getDefinition() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        out.close();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Main frame = new Main();
                // frame.setVisible(true);
                // frame.setLocationRelativeTo(null);

                Language polytechnica = new Language("symbol_table.txt");
                Lexer lexer = new Lexer(polytechnica, Files.readString(Paths.get("input.poly")).trim());
                ArrayList<Token> tokens = lexer.generateTokens();
                Parser parser = new Parser(tokens, polytechnica);
                ArrayList<ParseResult> errors = parser.parse();

                for (Token token : tokens) {
                    // System.out.println("TOKEN: " + token.getSymbol() + " | LINE: " + token.getLine());
                }

                for (ParseResult error : errors) {
                    System.out.println("Line " + error.getLine() + ": " + error.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
