package app.ppl;

import java.util.ArrayList;
import java.util.HashMap;

public class DigitFSM extends FiniteStateMachine {

    public DigitFSM() {
        super();

        states = new HashMap<>();
        acceptingStates = new ArrayList<>();

        states.put("initial", 0);
        states.put("intDigit", 1);
        states.put("integer", 2);
        states.put("fracStart", 3);
        states.put("fracDigit", 4);
        states.put("fraction", 5);
        states.put("NoNext", -1);

        acceptingStates.add(states.get("integer"));
        acceptingStates.add(states.get("fraction"));

        initialState = states.get("initial");
    }

    @Override
    public int nextState(int currentState, char input) {
        if (currentState == states.get("initial")) {
            if (Character.isDigit(input)) {
                return states.get("intDigit");
            }
        } else if (currentState == states.get("intDigit")) {
            if (Character.isDigit(input)) {
                return states.get("intDigit");
            } else if (input == '.') {
                return states.get("fracStart");
            } else {
                return states.get("integer");
            }
        } else if (currentState == states.get("fracStart")) {
            if (Character.isDigit(input)) {
                return states.get("fracDigit");
            }
        } else if (currentState == states.get("fracDigit")) {
            if (Character.isDigit(input)) {
                return states.get("fracDigit");
            } else {
                return states.get("fraction");
            }
        }

        return states.get("NoNext");
    }

}
