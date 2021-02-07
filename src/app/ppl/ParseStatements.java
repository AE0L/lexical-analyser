package app.ppl;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseStatements {

    private ArrayList<Token> stms;
    private int stmPos = 0;
    private int curPos = 0;
    private Token currToken;

    public ParseStatements(ArrayList<Token> stms) {
        this.stms = stms;

        currToken = stms.get(curPos);
    }

    public boolean hasTokens() {
        return curPos < stms.size() - 1;
    }

    private void advance() {
        curPos += 1;

        if (curPos == stms.size() && !stms.get(curPos - 1).getType().equals("EOL")) {
            return;
        }

        currToken = stms.get(curPos);
    }

    private boolean is(String token) {
        return currToken.getSymbol().equals(token);
    }

    private Statement getStatement() throws IndexOutOfBoundsException {
        Statement stm = new Statement();

        while (hasTokens() && !is(";")) {
            stm.add(currToken);
            advance();
        }

        stm.add(currToken);
        advance();

        stmPos = curPos;

        return stm;
    }

    private Statement getIfStatement() {
        Statement stm = new Statement();

        stm.add(currToken);
        advance();

        while (hasTokens()) {
            if (is("IF")) {
                for (Token token : getIfStatement()) {
                    stm.add(token);
                }
            } else if (is("COIL")) {
                for (Token token : getCoilStatement()) {
                    stm.add(token);
                }
            }

            if (is("}")) {
                stm.add(currToken);
                advance();

                if (is("ELSE")) {
                    stm.add(currToken);
                    advance();

                    if (is("IF")) {
                        for (Token token : getIfStatement()) {
                            stm.add(token);
                        }

                        break;
                    } else {
                        continue;
                    }
                }

                break;
            }

            stm.add(currToken);
            advance();
        }

        stmPos = curPos;

        return stm;
    }

    private Statement getCoilStatement() {
        Statement stm = new Statement();

        while (hasTokens() && !is("}")) {
            stm.add(currToken);
            advance();
        }

        stm.add(currToken);
        advance();

        stmPos = curPos + 1;

        return stm;
    }

    private void skipSingleComment() {
        while (hasTokens()) {
            advance();

            if (currToken.getType().equals("SINGLE_COMMENT")) {
                advance();

                break;
            }
        }

        stmPos = curPos;
    }

    private void skipMultiComment() {
        while (hasTokens()) {
            advance();

            if (is("#\\")) {
                advance();

                break;
            }
        }
    }

    public ArrayList<Statement> parse() {
        ArrayList<Statement> validity = new ArrayList<>();

        while (hasTokens()) {
            String stm = ParseUtils.predict(currToken);

            if (!stm.equals("invalid")) {
                if (stm.equals("var_stm")) {
                    try {
                        Statement var_stm = getStatement();
                        boolean valid = ParseUtils.parseVarStatement(var_stm);
                        var_stm.setValid(valid);
                        validity.add(var_stm);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();

                        return null;
                    }
                } else if (stm.equals("if_stm")) {
                    Statement if_stm = getIfStatement();
                    boolean valid = ParseUtils.parseIfStatement(if_stm);
                    if_stm.setValid(valid);
                    validity.add(if_stm);
                } else if (stm.equals("coil_stm")) {
                    Statement coil_stm = getCoilStatement();
                    boolean valid = ParseUtils.parseCoilStatement(coil_stm);
                    coil_stm.setValid(valid);
                    validity.add(coil_stm);
                } else if (stm.equals("assign_stm")) {
                    Statement assign_stm = getStatement();
                    boolean valid = ParseUtils.parseAssignStatement(assign_stm);
                    assign_stm.setValid(valid);
                    validity.add(assign_stm);
                } else if (stm.equals("show_stm")) {
                    Statement show_stm = getStatement();
                    boolean valid = ParseUtils.parseShowStatement(show_stm);
                    show_stm.setValid(valid);
                    validity.add(show_stm);
                } else if (stm.equals("single_comm")) {
                    skipSingleComment();
                } else if (stm.equals("multi_comm")) {
                    skipMultiComment();
                }
            } else {
                return validity;
            }
        }

        return validity;
    }

}
