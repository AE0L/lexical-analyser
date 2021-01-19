package app.ppl;

import java.util.ArrayList;

public class Lexer {

    private String input;
    private Language lang;
    private int position;
    private boolean quoteOpen;
    private boolean singleCommentOpen;
    private boolean multiCommentOpen;

    public Lexer(Language lang, String input) {
        this.lang = lang;
        this.input = input;
        this.position = 0;
        this.quoteOpen = false;
        this.singleCommentOpen = false;
        this.multiCommentOpen = false;
    }

    public ArrayList<Token> generateTokens() {
        ArrayList<Token> tokens = new ArrayList<>();
        Token token = this.nextToken();

        while (!token.getType().equals("EOL") && !token.getType().equals("INVALID")) {
            tokens.add(token);
            token = this.nextToken();
        }

        if (token.getType().equals("EOL") || token.getType().equals("INVALID")) {
            tokens.add(token);
        }

        return tokens;
    }

    public Token nextToken() {
        if (this.position >= this.input.length()) {
            return new Token("EOL", "EOL", "End Of File");
        }

        if (this.singleCommentOpen) {
            return this.recognizeSingleComment();
        }

        if (this.multiCommentOpen) {
            return this.recognizeMultiComment();
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

        if (CharUtils.isComment(currentChar)) {
            return this.recognizeComment();
        }

        if (currentChar == ';') {
            this.position += 1;
            return this.lang.token(";");
        }

        return new InvalidToken(Character.toString(currentChar));
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

        return new InvalidToken(output.symbol);
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

        return new InvalidToken(Character.toString(currentChar));
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

            default: return new InvalidToken(Character.toString(currentChar));
        }
    }

    public Token recognizeRelationalOperator() {
        int tmp = this.position;
        char currentChar = this.input.charAt(this.position);
        char nextChar = tmp + 1 < this.input.length() ? this.input.charAt(tmp + 1) : ' ';
        boolean hasEqualNext = nextChar == '=';

        this.position += hasEqualNext ? 2 : 1;

        switch (currentChar) {
            case '<': return hasEqualNext ? this.lang.token("<=") : this.lang.token("<");
            case '>': return hasEqualNext ? this.lang.token(">=") : this.lang.token(">");
            case '=': return hasEqualNext ? this.lang.token("==") : this.lang.token("=");

            default: return new InvalidToken(Character.toString(currentChar));
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

        return new InvalidToken(Character.toString(currentChar));
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

            default: return new InvalidToken(Character.toString(currentChar));
        }
    }

    public Token recognizeComment() {
        char currentChar = this.input.charAt(this.position);
        char nextChar = this.input.charAt(this.position + 1);

        switch (currentChar) {
            case '#':
                if (nextChar == '#') {
                    this.position += 2;

                    this.singleCommentOpen = true;

                    return this.lang.token("##");
                } else if (nextChar == '\\') {
                    this.position += 2;

                    return this.lang.token("#\\");
                }

                break;

            case '\\':
                if (nextChar == '\\') {
                    if (this.input.charAt(this.position + 2) == '#') {
                        this.position += 3;

                        this.multiCommentOpen = true;

                        return this.lang.token("\\\\#");
                    }
                } else if (nextChar == '#') {
                    this.position += 2;

                    this.multiCommentOpen = true;

                    return this.lang.token("\\#");
                }

                break;
            
            default: return new InvalidToken(Character.toString(currentChar));
        }
        
        return new InvalidToken(Character.toString(currentChar));
    }

    public Token recognizeSingleComment() {
        StringBuilder comment = new StringBuilder();
        int tmp = this.position;

        while (tmp < this.input.length()) {
            char currentChar = this.input.charAt(tmp);

            if (currentChar == '\n') {
                this.singleCommentOpen = false;

                break;
            }

            comment.append(currentChar);
            tmp += 1;
        }

        this.position += comment.length();

        return new Token(comment.toString().stripTrailing(), "SINGLE_COMMENT", "single line comment");
    }

    public Token recognizeMultiComment() {
        StringBuilder comment = new StringBuilder();
        int tmp = this.position;

        while (tmp < this.input.length()) {
            char currentChar = this.input.charAt(tmp);

            if (currentChar == '#' && this.input.charAt(tmp + 1) == '\\') {
                this.multiCommentOpen = false;

                break;
            }

            comment.append(currentChar);
            tmp += 1;
        }

        this.position += comment.length();

        return new Token(comment.toString().strip(), "MULTI_COMMENT", "multi line comment");
    }
    
}
