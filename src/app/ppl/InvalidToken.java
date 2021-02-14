package app.ppl;

public class InvalidToken extends Token {

    public InvalidToken(String symbol, int line) {
        super(symbol, "INVALID", "invalid symbol");
        super.setLine(line);
    }
    
}
