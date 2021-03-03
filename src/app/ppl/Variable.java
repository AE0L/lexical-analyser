package app.ppl;

import java.util.List;

public class Variable {

    private Token data_type;
    private Token ident;
    private List<Token> exp;
    private int scope;

    public Variable(Token data_type, Token ident, int scope) {
        this.data_type = data_type;
        this.ident = ident;
        this.scope = scope;
    }

    public Variable(Token data_type, Token ident, List<Token> exp, int scope) {
        this.data_type = data_type;
        this.ident = ident;
        this.exp = exp;
    }

    public Token getData_type() {
        return data_type;
    }

    public void setData_type(Token data_type) {
        this.data_type = data_type;
    }

    public Token getIdent() {
        return ident;
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }

    public List<Token> getExp() {
        return exp;
    }

    public void setExp(List<Token> exp) {
        this.exp = exp;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

}
