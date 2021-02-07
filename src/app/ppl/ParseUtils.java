package app.ppl;

import java.util.ArrayList;

public class ParseUtils {

    public static Language lang = null;

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
        return stm.get(index).getSymbol().equals(symbol);
    }

    private static boolean isType(String type, ArrayList<Token> stm, int index) {
        return stm.get(index).getType().equals(type);
    }

    // <data_type> <ident> = <exp>
    public static boolean parseVarStatement(ArrayList<Token> stm) {
        if (!lang.isDataType(stm.get(0).getSymbol())) {
            return false;
        }

        if (!isType("IDENTIFIER", stm, 1)) {
            return false;
        }

        if (!is("=", stm, 2)) {
            if (is(";", stm, 2)) {
                return true;
            } else {
                return false;
            }
        }

        if (!parseExp(new ArrayList<Token>(stm.subList(3, stm.size() - 1)))) {
            return false;
        }

        return true;
    }

    // IF ( <exp> ) { <stms> } [ELSE [IF ( <exp> )] { <stms> }]
    public static boolean parseIfStatement(ArrayList<Token> stm) {
        if (!is("IF", stm, 0)) {
            return false;
        }

        if (!is("(", stm, 1)) {
            return false;
        }

        int tmp = 2;
        int closures = 1;

        for (; tmp < stm.size(); tmp++) {
            if (is("COMP", stm, tmp)) {
                closures += 1;
            } else if (is(")", stm, tmp)) {
                closures -= 1;

                if (closures == 0) {
                    break;
                }
            }
        }

        if (tmp == stm.size()) {
            return false;
        }

        if (!parseGroup(new ArrayList<Token>(stm.subList(1, tmp + 1)))) {
            return false;
        }

        tmp += 1;

        if (!is("{", stm, tmp)) {
            return false;
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

        if (tmp == stm.size()) {
            return false;
        }

        if (new ParseStatements(new ArrayList<Token>(stm.subList(tmp2, tmp))).parse() == null) {
            return false;
        }

        tmp += 1;

        if (tmp < stm.size() && is("ELSE", stm, tmp)) {
            tmp += 1;

            if (is("IF", stm, tmp)) {
                if (!parseIfStatement(new ArrayList<Token>(stm.subList(tmp, stm.size())))) {
                    return false;
                }
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
                    return false;
                }

                if (new ParseStatements(new ArrayList<Token>(stm.subList(tmp2, tmp))).parse() == null) {
                    return false;
                }
            }
        }

        return true;
    }

    // COIL ( <exp> REPS ) { <stms> }
    public static boolean parseCoilStatement(ArrayList<Token> stm) {
        if (!is("COIL", stm, 0)) {
            return false;
        }

        if (!is("(", stm, 1)) {
            return false;
        }

        int tmp = 2;

        for (; tmp < stm.size(); tmp++) {
            if (is("REPS", stm, tmp)) {
                break;
            }
        }

        if (tmp == stm.size() || tmp == 2) {
            return false;
        }

        if (!parseExp(new ArrayList<Token>(stm.subList(2, tmp)))) {
            return false;
        }

        tmp += 2;

        if (!is("{", stm, tmp)) {
            return false;
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
            return false;
        }

        if (new ParseStatements(new ArrayList<Token>(stm.subList(tmp2, tmp))).parse() == null) {
            return false;
        }

        return true;
    }

    // <ident> = <exp>;
    public static boolean parseAssignStatement(ArrayList<Token> stm) {
        if (stm.size() < 4) {
            return false;
        }

        if (!isType("IDENTIFIER", stm, 0)) {
            return false;
        }

        if (!is("=", stm, 1)) {
            return false;
        }

        if (!parseExp(new ArrayList<Token>(stm.subList(2, stm.size() - 1)))) {
            return false;
        }

        return true;
    }

    // SHOW ( <exp> );
    public static boolean parseShowStatement(ArrayList<Token> stm) {
        if (!is("SHOW", stm, 0)) {
            return false;
        }

        if (!is("(", stm, 1)) {
            return false;
        }

        int tmp = 1;

        for (; tmp < stm.size(); tmp++) {
            if (is(")", stm, tmp)) {
                break;
            }
        }

        tmp += 1;

        if (tmp == stm.size()) {
            return false;
        }

        if (!parseGroup(new ArrayList<Token>(stm.subList(1, tmp)))) {
            return false;
        }

        return true;
    }

    // ( <exp> )
    public static boolean parseGroup(ArrayList<Token> stm) {
        if (!is("(", stm, 0)) {
            return false;
        }

        if (!is(")", stm, stm.size() - 1)) {
            return false;
        }

        if (!parseExp(new ArrayList<Token>(stm.subList(1, stm.size() - 1)))) {
            return false;
        }

        return true;
    }

    // <term> (<op> <term>)
    public static boolean parseExp(ArrayList<Token> stm) {
        // System.out.print("Parsing <exp>: ");
        // printStatement(stm);

        if (is("COMP", stm, 0)) {
            if (!is("(", stm, 1)) {
                return false;
            }

            if (!is(")", stm, 2)) {
                return false;
            }

            if (stm.size() > 3) {
                if (!isType("OPERATOR_ARITHMETIC", stm, 3) && !isType("OPERATOR_RELATIONAL", stm, 3)
                        && !isType("OPERATOR_LOGICAL", stm, 3)) {
                    return false;
                }

                if (!parseExp(new ArrayList<Token>(stm.subList(4, stm.size())))) {
                    return false;
                }
            }
        } else if (is("\"", stm, 0) || is("\'", stm, 0)) {
            if (stm.size() < 3) {
                return false;
            }
            
            if (!parseTerm(new ArrayList<Token>(stm.subList(0, 3)))) {
                return false;
            }

            if (stm.size() > 3) {
                if (!isType("OPERATOR_ARITHMETIC", stm, 3) && !isType("OPERATOR_RELATIONAL", stm, 3)
                        && !isType("OPERATOR_LOGICAL", stm, 3)) {
                    return false;
                }

                if (!parseExp(new ArrayList<Token>(stm.subList(4, stm.size())))) {
                    return false;
                }
            }
        } else {
            if (!parseTerm(stm.get(0))) {
                return false;
            }

            if (stm.size() > 1) {
                if (!isType("OPERATOR_ARITHMETIC", stm, 1) && !isType("OPERATOR_RELATIONAL", stm, 1)
                        && !isType("OPERATOR_LOGICAL", stm, 1)) {
                    return false;
                }

                if (!(stm.size() > 2)) {
                    return false;
                }

                if (!parseExp(new ArrayList<Token>(stm.subList(2, stm.size())))) {
                    return false;
                }
            }
        }

        return true;
    }

    // " <ident> " | ' <ident> '
    public static boolean parseTerm(ArrayList<Token> stm) {
        // System.out.print("Parsing <term>: ");
        // printStatement(stm);

        if (stm.size() < 3) {
            return false;
        }

        if (is("\"", stm, 0)) {
            if (!isType("IDENTIFIER", stm, 1) && !is("\"", stm, 2)) {
                return false;
            }
        } else if (is("\'", stm, 0)) {
            if (!isType("IDENTIFIER", stm, 1) && !is("\'", stm, 2) && !(stm.get(1).getSymbol().length() == 1)) {
                return false;
            }
        }

        return true;
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
