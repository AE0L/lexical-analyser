package app.ppl;

public class Token {

    private String symbol;
    private String type;
    private String definition;

    public Token(String symbol, String token, String definition) {
        this.symbol = symbol;
        this.type = token;
        this.definition = definition;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getType() {
        return this.type;
    }

    public String getDefinition() {
        return this.definition;
    }

    public String toString() {
        return this.symbol + "\t<" + this.type + ">\t" + this.definition;
    }

}
