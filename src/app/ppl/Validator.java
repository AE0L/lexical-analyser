package app.ppl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

public class Validator {

    File symbolTable;
    HashMap<String, Lexeme> symbols;

    public Validator() {
        this.symbolTable = new File("symbl_table.txt");
        this.symbols = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.symbolTable))) {
            // Skip first 2 lines
            reader.readLine();
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {
                BufferedReader lineReader = new BufferedReader(new StringReader(line));

                String symbol = this.readColumn(lineReader);
                String token = this.readColumn(lineReader);
                String definition = this.readColumn(lineReader);

                this.symbols.put(symbol, new Lexeme(token, definition));

                lineReader.close();
            }

            System.out.println(this.symbols);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readColumn(BufferedReader reader) throws IOException {
        StringBuilder column = new StringBuilder();
        int charcode;

        while ((charcode = reader.read()) == 9) {/* skip extra tabs */}

        column.append(Character.toChars(charcode));

        while ((charcode = reader.read()) != -1 && charcode != 9) {
            column.append(Character.toChars(charcode));
        }

        return column.toString();
    }

        /*
    public String validate(String inputSymbol) {
        ArrayList<String> possibleMatches = new ArrayList<>();

        this.symbols.forEach(symbol -> {
            if (symbol.charAt(0) == inputSymbol.charAt(0))
                possibleMatches.add(symbol);
        });

        if (possibleMatches.isEmpty()) {
            return "INVALID";
        } else {
            String match = "INVALID";
            int i;

            for (String symbol : possibleMatches) {
                if (symbol.length() < inputSymbol.length()) {
                    continue;
                }

                for (i = 0; i < symbol.length(); i++) {
                    try {
                        if (symbol.charAt(i) != inputSymbol.charAt(i))
                            break;
                    } catch (StringIndexOutOfBoundsException e) {
                        break;
                    }
                }

                if (i == symbol.length()) {
                    match = "VALID";

                    break;
                }
            }

            return match;
        }
    }
        */
}