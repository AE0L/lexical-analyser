package app.ppl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Language {

    HashMap<String, Token> symbolTable;
    ArrayList<String> keywords;

    public Language(String symbolTableFileName) {
        File symbolTableFile = new File(symbolTableFileName);
        this.symbolTable = new HashMap<>();
        this.keywords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(symbolTableFile))) {
            // Skip first 2 lines
            reader.readLine();
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {
                BufferedReader lineReader = new BufferedReader(new StringReader(line));

                String symbol = this.readColumn(lineReader);
                String token = this.readColumn(lineReader);
                String definition = this.readColumn(lineReader);

                this.symbolTable.put(symbol, new Token(symbol, token, definition));

                if (token.equals("KEYWORD") || token.equals("DATATYPE")) {
                    this.keywords.add(symbol);
                }

                lineReader.close();
            }
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

    public Token token(String key) {
        return this.symbolTable.get(key);
    }

    public boolean isKeyword(String identifier) {
        for (String keyword : this.keywords) {
            if (keyword.equals(identifier)) {
                return true;
            }
        }

        return false;
    }

}
