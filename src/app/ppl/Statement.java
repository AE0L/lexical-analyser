package app.ppl;

import java.util.ArrayList;
import java.util.List;

public class Statement extends ArrayList<Token> {

    private static final long serialVersionUID = 1L;

    private ParseResult result;

    public Statement() {}

    public Statement(List<Token> tokens) {
        super(tokens);
    }

    public void setResult(ParseResult result) {
        this.result = result;
    }

    public ParseResult getResult() {
        return result;
    }

    public boolean isValid() {
        return result.isValid();
    }
}
