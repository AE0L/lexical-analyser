package app.ppl;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class FiniteStateMachine {

    public HashMap<String, Integer> states;
    public ArrayList<Integer> acceptingStates;
    public int initialState;

    public abstract int nextState(int currentState, char input);

    public FSMOutput run(String input) {
        StringBuilder symbol = new StringBuilder();
        int currentState = this.initialState;

        for (int i = 0, length = input.length(); i < length; i++) {
            char currentChar = input.charAt(i);
            int nextState = this.nextState(currentState, currentChar);

            if (this.acceptingStates.contains(nextState)) {
                return new FSMOutput(true, symbol.toString());
            }

            if (nextState == -1) {
                break;
            }

            currentState = nextState;
            symbol.append(currentChar);
        }

        return this.acceptingStates.contains(currentState)
            ? new FSMOutput(true, symbol.toString())
            : new FSMOutput(false);
    }
}
