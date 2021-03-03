package app.ppl;

import java.util.ArrayList;

public class ParseUtils {

    public static Language lang = null;

    private ParseUtils() {
    }

    public static void setLang(Language newLang) {
        lang = newLang;
    }

    public static String toString(ArrayList<Token> stm) {
        StringBuilder string = new StringBuilder();

        for (Token token : stm) {
            string.append(token.getSymbol() + " ");
        }

        return string.toString();
    }

    private static boolean is(String symbol, ArrayList<Token> stm, int index) {
        try {
            return stm.get(index).getSymbol().equals(symbol);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean isType(String type, ArrayList<Token> stm, int index) {
        return stm.get(index).getType().equals(type);
    }

    // <data_type> <ident> = <exp>
    public static ArrayList<ParseResult> parseVarStatement(ArrayList<Token> stm) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (!lang.isDataType(stm.get(0).getSymbol())) {
            results.add(new ParseResult(false, stm.get(0), "Invalid data type"));

            return results;
        }

        if (!isType("IDENTIFIER", stm, 1)) {
            results.add(new ParseResult(false, stm.get(1), "Unable to resolve identifier"));

            return results;
        }

        if (stm.size() == 2) {
            return results;
        }

        if (!is("=", stm, 2)) {
            ParseResult result = new ParseResult();
            result.setValid(false);
            result.setLine(stm.get(1));

            if (stm.size() == 3 && !is(";", stm, 2)) {
                result.setMessage("Invalid syntax: expected \";\"");
                results.add(result);

                return results;
            }

            result.setMessage("Invalid syntax: expected \"=\"");
            results.add(result);

            return results;
        } else {
            if (stm.size() <= 3) {
                results.add(new ParseResult(false, stm.get(2), "Invalid syntax: missing expression"));

                return results;
            }
        }

        results.addAll(parseExp(new ArrayList<>(stm.subList(3, stm.size()))));

        return results;
    }

    // IF ( <exp> ) { <stms> } [ELSE [IF ( <exp> )] { <stms> }]
    public static ArrayList<ParseResult> parseIfStatement(ArrayList<Token> stm, ArrayList<Variable> vars, int scope) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (!is("IF", stm, 0)) {
            results.add(new ParseResult(false, stm.get(0), "Invalid syntax: expected \"IF\""));

            return results;
        }

        if (!is("(", stm, 1)) {
            results.add(new ParseResult(false, stm.get(1), "Invalid syntax: expected \"(\""));

            return results;
        }

        int tmp = 2;
        int closures = 1;

        for (; tmp < stm.size(); tmp++) {
            if (is(")", stm, tmp)) {
                closures -= 1;

                if (closures == 0) {
                    break;
                }
            }
        }

        if (tmp == stm.size()) {
            results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \")\""));

            return results;
        }

        results.addAll(parseGroup(new ArrayList<>(stm.subList(1, tmp + 1))));

        tmp += 1;

        if (!is("{", stm, tmp)) {
            results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \"{\""));

            return results;
        }

        tmp += 1;

        int tmp2 = tmp;
        closures = 1;

        for (; tmp < stm.size(); tmp++) {
            if (is("{", stm, tmp)) {
                closures += 1;
            } else if (is("}", stm, tmp)) {
                closures -= 1;

                if (closures == 0) {
                    break;
                }
            }
        }

        ArrayList<ParseResult> ifStmRes = new ParseStatements(new ArrayList<>(stm.subList(tmp2, tmp - 1)), vars, scope)
                .parse();

        if (ifStmRes == null) {
            results.add(new ParseResult(false, stm.get(tmp2), "Invalid statements"));
        }

        results.addAll(ifStmRes);

        if (tmp == stm.size() && !stm.get(tmp).getSymbol().equals("}")) {
            results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \"}\""));

            return results;
        }

        tmp += 1;

        if (tmp < stm.size() && is("ELSE", stm, tmp)) {
            tmp += 1;

            if (is("IF", stm, tmp)) {
                results.addAll(parseIfStatement(new ArrayList<>(stm.subList(tmp, stm.size())), vars, scope + 1));
            } else if (is("{", stm, tmp)) {
                tmp += 1;
                tmp2 = tmp;
                closures = 1;

                for (; tmp < stm.size(); tmp++) {
                    if (is("{", stm, tmp)) {
                        closures += 1;
                    } else if (is("}", stm, tmp)) {
                        closures -= 1;

                        if (closures == 0) {
                            break;
                        }
                    }
                }

                if (tmp == stm.size()) {
                    results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \"}\""));

                    return results;
                }

                ArrayList<ParseResult> elseStmRes = new ParseStatements(new ArrayList<>(stm.subList(tmp2, tmp)), vars,
                        scope).parse();

                if (elseStmRes == null) {
                    results.add(new ParseResult(false, stm.get(tmp2), "Invalid statements"));
                }

                results.addAll(elseStmRes);
            }
        }

        return results;
    }

    // COIL ( <exp> REPS ) { <stms> }
    public static ArrayList<ParseResult> parseCoilStatement(ArrayList<Token> stm) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (!is("COIL", stm, 0)) {
            results.add(new ParseResult(false, stm.get(0), "Invalid syntax: expected \"COIL\""));

            return results;
        }

        if (!is("(", stm, 1)) {
            results.add(new ParseResult(false, stm.get(1), "Invalid syntax: expected \"(\""));

            return results;
        }

        int tmp = 2;

        for (; tmp < stm.size(); tmp++) {
            if (is(")", stm, tmp)) {
                break;
            }
        }

        if (tmp == stm.size()) {
            results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \")\""));

            return results;
        }

        if (!stm.get(tmp - 1).getSymbol().equals("REPS")) {
            results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \"REPS\""));
        }

        results.addAll(parseExp(new ArrayList<>(stm.subList(2, tmp - 1))));

        tmp += 1;

        if (!is("{", stm, tmp)) {
            results.add(new ParseResult(false, stm.get(tmp), "Invalid syntax: expected \"{\""));

            return results;
        }

        tmp += 1;

        int tmp2 = tmp;

        int closures = 1;

        for (; tmp < stm.size(); tmp++) {
            if (is("{", stm, tmp)) {
                closures += 1;
            } else if (is("}", stm, tmp)) {
                closures -= 1;

                if (closures == 0) {
                    break;
                }
            }
        }

        if (tmp == stm.size()) {
            results.add(new ParseResult(false, stm.get(tmp - 1), "Invalid syntax: expected \"}\""));

            return results;
        }

        ArrayList<ParseResult> stmRes = new ParseStatements(new ArrayList<>(stm.subList(tmp2, tmp + 1))).parse();

        if (stmRes == null) {
            results.add(new ParseResult(false, stm.get(tmp2), "Invalid statements"));

            return results;
        }

        results.addAll(stmRes);

        return results;
    }

    // <ident> = <exp>;
    public static ArrayList<ParseResult> parseAssignStatement(ArrayList<Token> stm) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (!isType("IDENTIFIER", stm, 0)) {
            results.add(new ParseResult(false, stm.get(0), "Unable to resolve variable"));

            return results;
        }

        if (is("(", stm, 1)) {
            results.add(new ParseResult(false, stm.get(0), "Invalid keyword"));

            return results;
        }

        if (isType("IDENTIFIER", stm, 1)) {
            results.add(new ParseResult(false, stm.get(0), "data type cannot be resolved"));

            return results;
        }

        if (!is("=", stm, 1)) {
            results.add(new ParseResult(false, stm.get(1), "Invalid syntax: Expected \"=\""));

            return results;
        }

        results.addAll(parseExp(new ArrayList<>(stm.subList(2, stm.size()))));

        return results;
    }

    // SHOW ( <exp> );
    public static ArrayList<ParseResult> parseShowStatement(ArrayList<Token> stm) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (!is("SHOW", stm, 0)) {
            results.add(new ParseResult(false, stm.get(0), "Invalid syntax: expected \"SHOW\""));

            return results;
        }

        if (!is("(", stm, 1)) {
            results.add(new ParseResult(false, stm.get(1), "Invalid syntax: expected \"(\""));

            return results;
        }

        int tmp = 1;

        for (; tmp < stm.size(); tmp++) {
            if (is(")", stm, tmp)) {
                break;
            }
        }

        if (tmp >= stm.size()) {
            results.add(new ParseResult(false, stm.get(stm.size() - 1), "Invalid syntax: \")\" not found"));

            return results;
        }

        results.addAll(parseGroup(new ArrayList<>(stm.subList(1, tmp + 1))));

        return results;
    }

    // ( <exp> )
    public static ArrayList<ParseResult> parseGroup(ArrayList<Token> stm) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (!is("(", stm, 0)) {
            results.add(new ParseResult(false, stm.get(0), "Invalid syntax: expected \"(\""));
        }

        if (!is(")", stm, stm.size() - 1)) {
            results.add(new ParseResult(false, stm.get(stm.size() - 1), "Invalid syntax: expected \")\""));
        }

        results.addAll(parseExp(new ArrayList<>(stm.subList(1, stm.size() - 1))));

        return results;
    }

    // <term> (<op> <term>)
    public static ArrayList<ParseResult> parseExp(ArrayList<Token> stm) {
        ArrayList<ParseResult> results = new ArrayList<>();

        if (is("COMP", stm, 0)) {
            if (!is("(", stm, 1)) {
                results.add(new ParseResult(false, stm.get(1), "Invalid syntax: expected \"(\""));

                return results;
            } else if (!is(")", stm, 2)) {
                results.add(new ParseResult(false, stm.get(1), "Invalid syntax: expected \")\""));

                return results;
            }

            if (stm.size() > 3) {
                if (!isType("OPERATOR_ARITHMETIC", stm, 3) && !isType("OPERATOR_RELATIONAL", stm, 3)
                        && !isType("OPERATOR_LOGICAL", stm, 3)) {

                    results.add(new ParseResult(false, stm.get(3), "Invalid expression"));

                    return results;
                }

                if (stm.size() < 5) {
                    results.add(new ParseResult(false, stm.get(3), "Invalid expression"));

                    return results;
                }

                results.addAll(parseExp(new ArrayList<>(stm.subList(4, stm.size()))));
            }
        } else if (is("\"", stm, 0) || is("\'", stm, 0)) {
            if (stm.size() < 3) {
                results.add(new ParseResult(false, stm.get(0), "Invalid syntax: quotes are not properly closed"));

                return results;
            }

            ParseResult termRes = parseTerm(new ArrayList<>(stm.subList(0, 3)));

            if (!termRes.isValid()) {
                results.add(termRes);

                return results;
            }

            if (stm.size() > 3) {
                if (!isType("OPERATOR_ARITHMETIC", stm, 3) && !isType("OPERATOR_RELATIONAL", stm, 3)
                        && !isType("OPERATOR_LOGICAL", stm, 3)) {

                    results.add(new ParseResult(false, stm.get(3), "Invalid expression"));

                    return results;
                }

                if (stm.size() < 5) {
                    results.add(new ParseResult(false, stm.get(3), "Invalid expression"));

                    return results;
                }

                results.addAll(parseExp(new ArrayList<>(stm.subList(4, stm.size()))));
            }
        } else {
            if (!parseTerm(stm.get(0))) {
                results.add(new ParseResult(false, stm.get(0), "Invalid expression"));

                return results;
            }

            if (stm.size() > 1) {
                if (!isType("OPERATOR_ARITHMETIC", stm, 1) && !isType("OPERATOR_RELATIONAL", stm, 1)
                        && !isType("OPERATOR_LOGICAL", stm, 1)) {

                    results.add(new ParseResult(false, stm.get(1), "Invalid expression"));

                    return results;
                }

                if (stm.size() < 3) {
                    results.add(new ParseResult(false, stm.get(1), "Invalid expression"));

                    return results;
                }

                results.addAll(parseExp(new ArrayList<>(stm.subList(2, stm.size()))));
            }
        }

        return results;
    }

    // " <ident> " | ' <ident> '
    public static ParseResult parseTerm(ArrayList<Token> stm) {
        if (is("\"", stm, 0)) {
            if (!isType("STR_CONST", stm, 1)) {
                return new ParseResult(false, stm.get(1), "Invalid string value");
            } else if (!is("\"", stm, 2)) {
                return new ParseResult(false, stm.get(2), "Invalid syntax: quotes are not properly closed");
            }
        } else if (is("\'", stm, 0)) {
            if (!isType("CHAR_CONST", stm, 1)) {
                return new ParseResult(false, stm.get(1), "Invalid string value");
            } else if (!is("\'", stm, 2)) {
                return new ParseResult(false, stm.get(2), "Invalid syntax: quotes are not properly closed");
            } else if (stm.get(1).getSymbol().length() != 1) {
                return new ParseResult(false, stm.get(1), "Invalid character value");
            }
        }

        return new ParseResult(true);
    }

    // <ident> | <number> | TRUE | FALSE
    public static boolean parseTerm(Token token) {
        String type = token.getType();
        String symbol = token.getSymbol();

        if (type.equals("IDENTIFIER") || type.equals("NUMBER") || symbol.equals("TRUE") || symbol.equals("FALSE")) {
            return true;
        }

        return false;
    }

    public static String predict(Token token) {
        String symbol = token.getSymbol();
        String type = token.getType();

        if (symbol.equals("IF")) {
            return "if_stm";
        } else if (symbol.equals("COIL")) {
            return "coil_stm";
        } else if (symbol.equals("SHOW")) {
            return "show_stm";
        } else if (type.equals("DATATYPE")) {
            return "var_stm";
        } else if (type.equals("IDENTIFIER")) {
            return "assign_stm";
        } else if (type.equals("SINGLE_COMMENT")) {
            return "single_comm";
        } else if (type.equals("MULTI_START_COMMENT") || type.equals("DOC_START_COMMENT")) {
            return "multi_comm";
        }

        return "invalid";
    }

}
