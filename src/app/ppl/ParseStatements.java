package app.ppl;

import java.util.ArrayList;

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

        if (curPos == stms.size()) {
            return;
        }

        currToken = stms.get(curPos);
    }

    private boolean is(String token) {
        return currToken.getSymbol().equals(token);
    }

    private Statement getStatement() {
        Statement stm = new Statement();

        while (hasTokens() && !is(";")) {
            stm.add(currToken);
            advance();
        }

        stm.add(currToken);

        try {
            advance();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

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

    public ArrayList<ParseResult> parse() {
        ArrayList<ParseResult> errors = new ArrayList<>();

        while (hasTokens()) {
            String stm = ParseUtils.predict(currToken);

            if (!stm.equals("invalid")) {
                if (stm.equals("var_stm")) {
                    // try {
                        errors.addAll(ParseUtils.parseVarStatement(getStatement()));
                    // } catch (IndexOutOfBoundsException e) {
                    //     e.printStackTrace();

                    //     return null;
                    // }
                } else if (stm.equals("if_stm")) {
                    errors.addAll(ParseUtils.parseIfStatement(getIfStatement()));
                } else if (stm.equals("coil_stm")) {
                    errors.addAll(ParseUtils.parseCoilStatement(getCoilStatement()));
                } else if (stm.equals("assign_stm")) {
                    errors.addAll(ParseUtils.parseAssignStatement(getStatement()));
                } else if (stm.equals("show_stm")) {
                    errors.addAll(ParseUtils.parseShowStatement(getStatement()));
                } else if (stm.equals("single_comm")) {
                    skipSingleComment();
                } else if (stm.equals("multi_comm")) {
                    skipMultiComment();
                }
            // } else {
            //     try {
            //         validity.add(getStatement());
            //     } catch (IndexOutOfBoundsException e) {
            //         e.printStackTrace();
            //         validity.add(new Statement(stms.subList(0, stms.size())));
            //         return validity;
            //     }
            }
        }

        return errors;
    }

}
