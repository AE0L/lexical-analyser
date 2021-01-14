package app.ppl;

public class Lexeme {

    private String token;
    private String definition;

    public Lexeme(String token, String definition) {
        this.token = token;
        this.definition = definition;
    }

    public String getToken() {
        return token;
    }

    public String getDefinition() {
        return definition;
    }

    public String toString() {
        return this.token + "; " + this.definition;
    }

}
