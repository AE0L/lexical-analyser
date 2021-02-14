package app.ppl;

public class ParseResult {

    private boolean valid;
    private int line;
    private String message;

    public ParseResult() {
    }

    public ParseResult(boolean valid) {
        this.valid = valid;
    }

    public ParseResult(boolean valid, Token token, String message) {
        this.valid = valid;
        this.line = token.getLine();
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setLine(Token token) {
        this.line = token.getLine();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
