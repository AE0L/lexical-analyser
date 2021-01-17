package app.ppl;

import java.util.ArrayList;

public class Lexer {

    private String input;
    private Language lang;
    private int position;
    private boolean quoteOpen;

    public Lexer(Language lang, String input) {
        this.lang = lang;
        this.input = input;
        this.position = 0;
        this.quoteOpen = false;
    }

    public ArrayList<Token> generateTokens() {
        ArrayList<Token> tokens = new ArrayList<>();
        Token token = this.nextToken();

        while (!token.getType().equals("INVALID")) {
            tokens.add(token);

            if (token.getType().equals("EOL")) {
                break;
            }

            token = this.nextToken();
        }

        return tokens;
    }

    public Token nextToken() {
        if (this.position >= this.input.length()) {
            return new Token("EOL", "EOL", "End Of File");
        }

        this.skipWhiteSpaceAndNewLines();

        char currentChar = this.input.charAt(this.position);

        if (Character.isLetter(currentChar)) {
            return this.recognizeIdentifier();
        }

        if (Character.isDigit(currentChar)) {
            return this.recognizeDigit();
        }

        if (CharUtils.isOperator(currentChar)) {
            return this.recognizeOperator();
        }

        if (CharUtils.isBrackets(currentChar)) {
            return this.recognizeBrackets();
        }

        if (currentChar == ';') {
            this.position += 1;
            return this.lang.token(";");
        }

        return new InvalidToken();
    }

    public void skipWhiteSpaceAndNewLines() {
        char currentChar = this.input.charAt(this.position);

        while (this.position < this.input.length() && (Character.isWhitespace(currentChar) || currentChar == '\n')) {
            this.position += 1;

            currentChar = this.input.charAt(this.position);
        }
    }

    public Token recognizeIdentifier() {
        StringBuilder identifier = new StringBuilder();
        int tmp = this.position;

        while (tmp < this.input.length()) {
            char currentChar = this.input.charAt(tmp);

            if (currentChar == '\'' || currentChar == '\"') {
                break;
            }

            if (!this.quoteOpen) {
                if (!(Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_')) {
                    break;
                }
            }

            identifier.append(currentChar);
            tmp += 1;
        }

        this.position += identifier.length();

        if (this.lang.isKeyword(identifier.toString())) {
            return this.lang.token(identifier.toString());
        }

        return new Token(identifier.toString(), "IDENTIFIER", "identifier");
    }

    public Token recognizeDigit() {
        FiniteStateMachine fsm = new DigitFSM();

        String fsmInput = this.input.substring(this.position);
        FSMOutput output = fsm.run(fsmInput);

        if (output.recognized) {
            this.position += output.symbol.length();

            return new Token(output.symbol, "NUMBER", "number");
        }

        return new InvalidToken();
    }

    public Token recognizeOperator() {
        char currentChar = this.input.charAt(this.position);

        if (CharUtils.isArithmeticOperator(currentChar)) {
            return this.recognizeArithmeticOperator();
        } else if (CharUtils.isRelationalOperator(currentChar)) {
            return this.recognizeRelationalOperator();
        } else if (CharUtils.isLogicalOperator(currentChar)) {
            return this.recognizeLogicalOperator();
        }

        return new InvalidToken();
    }

    public Token recognizeArithmeticOperator() {
        char currentChar = this.input.charAt(this.position);

        this.position += 1;

        switch (currentChar) {
            case '+': return this.lang.token("+");
            case '-': return this.lang.token("-");
            case '*': return this.lang.token("*");
            case '/': return this.lang.token("/");
            case '^': return this.lang.token("^");
            case '%': return this.lang.token("%");

            default: return new InvalidToken();
        }
    }

    public Token recognizeRelationalOperator() {
        int tmp = this.position;
        char currentChar = this.input.charAt(this.position);
        char nextChar = tmp + 1 < this.input.length() ? this.input.charAt(tmp + 1) : ' ';
        boolean hasEqualNext = nextChar == '=';

        this.position += hasEqualNext ? 2 : 1;

        switch (currentChar) {
            case '<': return hasEqualNext
                    ? this.lang.token("<=")
                    : this.lang.token("<");
            case '>': return hasEqualNext
                    ? this.lang.token(">=")
                    : this.lang.token(">");
            case '=': return hasEqualNext
                    ? this.lang.token("==")
                    : this.lang.token("=");

            default: return new InvalidToken();
        }
    }

    public Token recognizeLogicalOperator() {
        int tmp = this.position;
        char currentChar = this.input.charAt(this.position);
        
        if (currentChar == '!') {
            this.position += 1;

            return this.lang.token("!");
        }

        char nextChar = tmp + 1 < this.input.length() ? this.input.charAt(tmp + 1) : ' ';

        if (currentChar == '&' && currentChar == nextChar) {
            this.position += 2;

            return this.lang.token("&&");
        } else if (currentChar == '|' && currentChar == nextChar) {
            this.position += 2;

            return this.lang.token("||");
        }

        return new InvalidToken();
    }

    public Token recognizeBrackets() {
        char currentChar = this.input.charAt(this.position);

        this.position += 1;

        switch (currentChar) {
            case '(': return this.lang.token("(");
            case ')': return this.lang.token(")");
            case '{': return this.lang.token("{");
            case '}': return this.lang.token("}");
            case '\'': 
                if (this.quoteOpen) {
                    this.quoteOpen = false;
                } else {
                    this.quoteOpen = true;
                }
               
                return this.lang.token("\'");
            case '\"': 
                if (this.quoteOpen) {
                    this.quoteOpen = false;
                } else {
                    this.quoteOpen = true;
                }

                return this.lang.token("\"");

            default: return new InvalidToken();
        }
    }
}
