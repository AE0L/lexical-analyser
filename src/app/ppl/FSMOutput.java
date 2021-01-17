package app.ppl;

public class FSMOutput {

    public final boolean recognized;
    public final String symbol;

    public FSMOutput(boolean recognized) {
        this.recognized = recognized;
        this.symbol = null;
    }

    public FSMOutput(boolean recognized, String symbol) {
        this.recognized = recognized;
        this.symbol = symbol;
    }
    
}
