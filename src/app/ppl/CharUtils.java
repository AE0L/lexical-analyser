package app.ppl;

public class CharUtils {

    private CharUtils() {}

    public static boolean isOperator(char character) {
        char[] validOperatorStarts = {'+', '-', '*', '/', '^', '%', '<', '>', '=', '!', '&', '|'};

        for (char operator : validOperatorStarts) {
            if (operator == character) {
                return true;
            }
        }

        return false;
    }

    public static boolean isArithmeticOperator(char character) {
        char[] arithmeticSymbols = {'+', '-', '*', '/', '^', '%'};

        for (char operator : arithmeticSymbols) {
            if (operator == character) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRelationalOperator(char character) {
        char[] relationalSymbols = {'<', '>', '='};

        for (char operator : relationalSymbols) {
            if (operator == character) {
                return true;
            }
        }

        return false;
    }

    public static boolean isLogicalOperator(char character) {
        char[] logicalSymbols = {'!', '&', '|'};

        for (char operator : logicalSymbols) {
            if (operator == character) {
                return true;
            }
        }

        return false;
    }
    
    public static boolean isBrackets(char character) {
        char[] validBrackets = {'(', ')', '{', '}', '\'', '\"'};

        for (char bracket : validBrackets) {
            if (bracket == character) {
                return true;
            }
        }

        return false;
    }

   
}
