package app.ppl;

import java.util.ArrayList;

public class Statement extends ArrayList<Token> {

    private static final long serialVersionUID = 1L;

    private boolean valid;

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}
