import java.io.*;
import java.util.*;

public class TuringMachineSimulator {
    public static void main(String[] args) throws IOException {
        // Read input file
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        // Parse input file
        int numVarsInputAlphabet = Integer.parseInt(lines.get(0));
        Set<Character> inputAlphabet = new HashSet<>();
        for (int i = 0; i < numVarsInputAlphabet; i++) {
            inputAlphabet.add(lines.get(1).charAt(i)); //new
        }
        int numVarsTapeAlphabet = Integer.parseInt(lines.get(2));
        Set<Character> tapeAlphabet = new HashSet<>();
        for (int i = 0; i < numVarsTapeAlphabet; i++) {
            tapeAlphabet.add(lines.get(3).charAt(i));
        }
        char blankSymbol = lines.get(4).charAt(0);
        int numStates = Integer.parseInt(lines.get(5));
        String[] states = lines.get(6).split(" ");
        String startState = lines.get(7);
        String acceptState = lines.get(8);
        String rejectState = lines.get(9);
        List<List<String>> transitions = new ArrayList<>();
        for (int i = 10; i < lines.size() - 1; i++) {
            String[] transition = lines.get(i).split(" ");
            transitions.add(Arrays.asList(transition));
        }
        String string = lines.get(lines.size() - 1);

        // Initialize Turing machine
        List<Character> tape = new ArrayList<>();
        for (char c : string.toCharArray()) {
            tape.add(c);
        }
        tape.add(blankSymbol);
        int tapeHeadIndex = 0;
        String state = startState;
        Set<String> visitedStates = new HashSet<>(); //new
        // Run Turing machine
        List<String> route = new ArrayList<>();
        while (true) {
            if (visitedStates.contains(state)) {
                break; // Turing machine is looping
            }
            visitedStates.add(state);
            route.add(state);
            boolean foundTransition = false;
            for (List<String> transition : transitions) {
                if (state.equals(transition.get(0)) && tape.get(tapeHeadIndex) == transition.get(1).charAt(0)) {
                    tape.set(tapeHeadIndex, transition.get(2).charAt(0));
                    if (transition.get(3).equals("L")) {
                        tapeHeadIndex--;
                        if (tapeHeadIndex < 0) {
                            tape.add(0, blankSymbol);
                            tapeHeadIndex = 0;
                        }
                    } else {
                        tapeHeadIndex++;
                        if (tapeHeadIndex >= tape.size()) {
                            tape.add(blankSymbol);
                        }
                    }
                    state = transition.get(4);
                    foundTransition = true;
                    break;
                }
            }
            if (!foundTransition) {
                break;
            }
        }

        // Printing the output to console
        System.out.print("ROUTE: ");
        for (String s : route) {
            System.out.print(s + " ");
        }
        System.out.println();

        if (state.equals(acceptState)) {
            System.out.println("RESULT: accepted");
        } else if (state.equals(rejectState)) {
            System.out.println("RESULT: rejected");
        } else {
            System.out.println("RESULT: looped");
        }
    }
}