package app.ppl;

import java.util.ArrayList;
import java.util.List;

public class ParseStatements {

    private ArrayList<Token> stms;
    private int stmPos = 0;
    private int curPos = 0;
    private int scope;
    private Token currToken;
    private ArrayList<Variable> vars;

    public ParseStatements(List<Token> stms, ArrayList<Variable> vars, int scope) {
        this.stms = new ArrayList<>(stms);
        this.vars = vars;
        this.scope = scope + 1;

        currToken = stms.get(curPos);
    }

    public ParseStatements(ArrayList<Token> stms) {
        this.stms = stms;
        this.vars = new ArrayList<>();
        this.scope = 0;

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

        int closures = 1;

        while (hasTokens()) {
            if (is("{")) {
                closures += 1;
            } else if (is("}")) {
                closures -= 1;

                if (closures == 0) {
                    break;
                }
            }

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
                    Statement var_stm = getStatement();
                    var_stm.remove(var_stm.size() - 1);
                    ArrayList<ParseResult> syntax = ParseUtils.parseVarStatement(var_stm);

                    if (isResultValid(syntax)) {
                        ParseResult semantic = analyzeVarStm(var_stm);
                        errors.add(semantic);
                    }

                    errors.addAll(syntax);
                } else if (stm.equals("assign_stm")) {
                    Statement assign_stm = getStatement();
                    assign_stm.remove(assign_stm.size() - 1);
                    ArrayList<ParseResult> syntax = ParseUtils.parseAssignStatement(assign_stm);

                    if (isResultValid(syntax)) {
                        ParseResult semantic = analyzeAssignStm(assign_stm);
                        errors.add(semantic);
                    }

                    errors.addAll(syntax);
                } else if (stm.equals("if_stm")) {
                    Statement if_stm = getIfStatement();
                    ArrayList<ParseResult> syntax = ParseUtils.parseIfStatement(if_stm, vars, scope);
                    ArrayList<ParseResult> semantic = analyzeIfStm(if_stm);

                    errors.addAll(semantic);
                    errors.addAll(syntax);
                } else if (stm.equals("coil_stm")) {
                    Statement coil_stm = getCoilStatement();
                    ArrayList<ParseResult> syntax = ParseUtils.parseCoilStatement(coil_stm);
                    ArrayList<ParseResult> semantic = analyzeCoilStm(coil_stm);

                    errors.addAll(semantic);
                    errors.addAll(syntax);
                } else if (stm.equals("show_stm")) {
                    Statement show_stm = getStatement();
                    ArrayList<ParseResult> syntax = ParseUtils.parseShowStatement(show_stm);

                    errors.addAll(syntax);
                } else if (stm.equals("single_comm")) {
                    skipSingleComment();
                } else if (stm.equals("multi_comm")) {
                    skipMultiComment();
                }
            } else {
                errors.add(new ParseResult(false, currToken, "Invalid symbol"));

                break;
            }
        }

        return errors;
    }

    private boolean isResultValid(ArrayList<ParseResult> results) {
        for (ParseResult result : results) {
            if (!result.isValid()) {
                return false;
            }
        }

        return true;
    }

    private ParseResult analyzeVarStm(Statement stm) {
        Variable var;
        Token data_type = stm.get(0);
        Token ident = stm.get(1);

        if (isVarDeclared(ident)) {
            return new ParseResult(false, ident, "Variable '" + ident.getSymbol() + "' already declared");
        }

        if (stm.size() > 3) {
            List<Token> exp = stm.subList(3, stm.size());
            ParseResult result = parseExpDataType(exp, data_type.getSymbol());

            if (result.isValid()) {
                var = new Variable(data_type, ident, exp, scope);
            } else {
                return result;
            }
        } else {
            var = new Variable(data_type, ident, scope);
        }

        vars.add(var);

        return new ParseResult(true);
    }

    private ParseResult analyzeAssignStm(Statement stm) {
        Token ident = stm.get(0);

        if (!isVarDeclared(ident)) {
            return new ParseResult(false, ident, "Variable '" + ident.getSymbol() + "' not yet declared");
        } else {
            Variable var = getVar(ident);
            List<Token> exp = stm.subList(2, stm.size());

            if (var != null) {
                if (var.getScope() > scope) {
                    return new ParseResult(false, ident, "Variable '" + ident.getSymbol() + "' not yet declared");
                }

                ParseResult result = parseExpDataType(exp, var.getData_type().getSymbol());

                if (!result.isValid()) {
                    return result;
                }
            }
        }

        return new ParseResult(true);
    }

    private Variable getVar(Token ident) {
        for (Variable var : vars) {
            if (var.getIdent().getSymbol().equals(ident.getSymbol())) {
                return var;
            }
        }

        return null;
    }

    private boolean isVarDeclared(Token ident) {
        for (Variable var : vars) {
            if (var.getIdent().getSymbol().equals(ident.getSymbol())) {
                return true;
            }
        }

        return false;
    }

    private ParseResult parseExpDataType(List<Token> exp, String data_type) {
        for (Token term : exp) {
            String type = term.getType();
            String symbol = term.getSymbol();

            if (type.equals("IDENTIFIER")) {
                if (isVarDeclared(term)) {
                    Variable var = getVar(term);

                    if (var != null) {
                        if (!var.getData_type().getSymbol().equals(data_type)) {
                            return new ParseResult(false, term,
                                    "Data type mismatch '" + term.getSymbol() + "', expected a " + data_type);
                        } else if (var.getExp() == null || var.getExp().isEmpty()) {
                            return new ParseResult(false, term, "Variable has not been initialized yet");
                        }
                    }
                }
            } else {

                switch (data_type) {
                    case "REAL":
                    case "INT":
                        if (isOp(type)) {
                            if (type.equals("OPERATOR_RELATIONAL") || type.equals("OPERATOR_LOGICAL")) {
                                return new ParseResult(false, term, "Invalid operator for type " + data_type);
                            }
                        } else if (!type.equals("NUMBER")) {
                            return new ParseResult(false, term,
                                    "Data type mismatch '" + term.getSymbol() + "', expected a NUMBER");
                        }
                        break;
                    case "STR":
                        if (isOp(type)) {
                            if (type.equals("OPERATOR_RELATIONAL") || type.equals("OPERATOR_LOGICAL")) {
                                return new ParseResult(false, term, "Invalid operator for type " + data_type);
                            }
                        } else if (!type.equals("STR_CONST") && !type.equals("DOUBLE_QUOTE")) {
                            return new ParseResult(false, term, "Data type mismatch, expected a STR");
                        }
                        break;
                    case "CHAR":
                        if (isOp(type)) {
                            return new ParseResult(false, term, "Invalid operator for type " + data_type);
                        } else if (!type.equals("CHAR_CONST") && !type.equals("SINGLE_QUOTE")) {
                            return new ParseResult(false, term, "Data type mismatch, expected a CHAR");
                        }
                        break;
                    case "BOOL":
                        if (isOp(type)) {
                            if (type.equals("OPERATOR_ARITHMETIC")) {
                                return new ParseResult(false, term, "Invalid operator for type " + data_type);
                            }
                        } else if (!symbol.equals("TRUE") && !symbol.equals("FALSE")) {
                            return new ParseResult(false, term,
                                    "Data type mismatch '" + term.getSymbol() + "', expected a BOOL");
                        }
                }
            }
        }

        return new ParseResult(true);
    }

    private boolean isOp(String type) {
        return type.equals("OPERATOR_RELATIONAL") || type.equals("OPERATOR_ARITHMETIC")
                || type.equals("OPERATOR_LOGICAL");
    }

    private ArrayList<ParseResult> analyzeIfStm(Statement stm) {
        ArrayList<ParseResult> result = new ArrayList<>();
        ParseResult if_cond = analyzeIfCondition(getCondition(stm));

        if (!if_cond.isValid()) {
            result.add(if_cond);

            return result;
        }

        List<Token> stm_block = getStmBlock(stm);

        result.addAll(new ParseStatements(stm_block, vars, scope + 1).parse());

        return result;
    }

    private ParseResult analyzeIfCondition(List<Token> exp) {
        for (Token term : exp) {
            String type = term.getType();
            String symbol = term.getSymbol();

            if (type.equals("STR_CONST") || type.equals("DOUBLE_QUOTE") || type.equals("CHAR_CONST")
                    || type.equals("SINGLE_QUOTE")) {
                return new ParseResult(false, term, "Invalid IF condition");
            } else if (!isOp(type)) {
                if (!type.equals("NUMBER") && !symbol.equals("TRUE") && !symbol.equals("FALSE")
                        && !type.equals("IDENTIFIER")) {
                    return new ParseResult(false, term, "Invalid IF condition");
                } else if (type.equals("IDENTIFIER")) {
                    if (!isVarDeclared(term)) {
                        return new ParseResult(false, term, "Variable '" + term.getSymbol() + "' not yet declared");
                    } else {
                        Variable var = getVar(term);

                        if (var != null) {
                            String var_type = var.getData_type().getSymbol();

                            if (!var_type.equals("INT") && !var_type.equals("REAL") && !var_type.equals("BOOL")) {
                                return new ParseResult(false, term, "Invalid variable");
                            }
                        }
                    }
                }
            } else if (isOp(type)) {
                if (type.equals("OPERATOR_ARITHMETIC")) {
                    return new ParseResult(false, term, "Invalid IF condition");
                }
            }
        }

        return new ParseResult(true);
    }

    private List<Token> getCondition(Statement stm) {
        int tmp = 2;

        for (; tmp < stm.size(); tmp++) {
            Token curr = stm.get(tmp);

            if (curr.getType().equals("RIGHT_PAREN")) {
                break;
            }
        }

        return stm.subList(2, tmp);
    }

    private ArrayList<ParseResult> analyzeCoilStm(Statement stm) {
        ArrayList<ParseResult> result = new ArrayList<>();
        ParseResult coil_cond = analyzeCoilCondition(getCondition(stm));

        if (!coil_cond.isValid()) {
            result.add(coil_cond);

            return result;
        }

        List<Token> stm_block = getStmBlock(stm);

        result.addAll(new ParseStatements(stm_block, vars, scope + 1).parse());

        return result;
    }

    private ParseResult analyzeCoilCondition(List<Token> exp) {
        for (Token term : exp) {
            String type = term.getType();

            if (!type.equals("NUMBER") && !type.equals("OPERATOR_ARITHMETIC") && !type.equals("IDENTIFIER")
                    && !term.getSymbol().equals("REPS")) {
                return new ParseResult(false, term, "Invalid COIL condition");
            } else if (type.equals("IDENTIFIER")) {
                Variable var = getVar(term);

                if (!isVarDeclared(term)) {
                    return new ParseResult(false, term, "Variable '" + term.getSymbol() + "' not yet declared");
                } else if (var.getExp() == null || var.getExp().isEmpty()) {
                    return new ParseResult(false, term, "Variable has not been initialized yet");
                } else {
                    String var_type = var.getData_type().getSymbol();

                    if (var != null) {
                        if (!var_type.equals("INT")) {
                            return new ParseResult(false, term, "Invalid variable");
                        }
                    }
                }
            }
        }

        return new ParseResult(true);
    }

    private List<Token> getStmBlock(Statement stm) {
        int tmp = 2;

        for (; tmp < stm.size(); tmp++) {
            if (stm.get(tmp).getSymbol().equals("{")) {
                break;
            }
        }

        int tmp2 = tmp + 1;

        for (; tmp < stm.size(); tmp++) {
            if (stm.get(tmp).getSymbol().equals("}")) {
                break;
            }
        }

        return stm.subList(tmp + 1, tmp2);
    }

}
