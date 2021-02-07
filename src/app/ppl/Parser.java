package app.ppl;

import java.util.ArrayList;

public class Parser {

    private ArrayList<Token> tokens;

    public Parser(ArrayList<Token> tokens, Language lang) {
        this.tokens = tokens;

        ParseUtils.setLang(lang);
    }

    public ArrayList<Statement> parse() {
        return new ParseStatements(tokens).parse();
    }

}