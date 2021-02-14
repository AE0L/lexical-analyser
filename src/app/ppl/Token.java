package app.ppl;

public class Token implements Cloneable {

    private String symbol;
    private String type;
    private String definition;
    private int line;

    public Token(String symbol, String type, String definition) {
        this.symbol = symbol;
        this.type = type;
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

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        return this.symbol + " [" + this.line + "]";
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
