import java.util.*;
import java.util.Scanner; //the Scanner class to read text files
import java.io.File;      // Import the File class
import java.io.IOException;
import java.nio.file.*;   //File path to read the contents of file
public class CFGtoCNF {
    static String input, newline, filePath="./G1.txt"; //path to file (stored in current folder)
    static int currentLine, terminalIndex, ruleIndex, startIndex, rulesCount;
    String replaceEpsilon;
    final Map<String, List<String>> variableToProductionMap = new LinkedHashMap<>(); // map variable with production ( variable -> production)

    public static void main(String[] args) throws IOException {
        StringBuilder final_string;
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            currentLine++;
            if (line.contains("TERMINAL")) {
                terminalIndex = currentLine;
            }
            if (line.contains("RULES")) {
                ruleIndex = currentLine;
            }
            if (line.contains("START")) {
                startIndex = currentLine;//index of start symbol
            }
        }
    List<String> str = new ArrayList<>();
        for (int i = ruleIndex; i < startIndex - 1; i++) {
            Path path = Paths.get(filePath);
            newline = Files.readAllLines(path).get(i);
            while (Files.readAllLines(path).get(i).charAt(0) == Files.readAllLines(path).get(i+1).charAt(0)){
                newline = newline+"|"+Files.readAllLines(path).get(i+1).substring(2);
                i++;
            }
            str.add(newline); //[S:a|aA|B, A:aBB|e, B:Aa|b]
        }
        final_string = new StringBuilder(str.get(0) + "\n");
        for (int i = 1; i < str.size()-1; i++)
            final_string.append(str.get(i)).append("\n");
        final_string.append(str.get(str.size() - 1));
        //System.out.println(final_string);
        CFGtoCNF c = new CFGtoCNF();
        c.setterMethod(final_string.toString(), str.size()+1);
        c.convertCFGtoCNF();
    }
    public void setterMethod(String input, int rulesCount) {
        CFGtoCNF.input = input;
        CFGtoCNF.rulesCount = rulesCount;
    }
    public void convertCFGtoCNF() throws IOException {
        insertNewStartSymbol();          //Step 1. Eliminate start symbol from RHS.
        convertStringToMap();
        eliminateNullProductions();      //Step 2a. Eliminate null productions.
        removeDuplicateKeyValue();
        eliminateUnitProductions();      //Step 2b. Eliminate Unit productions
        onlyTwoTerminalandOneVariable(); //Step 3. Assign new variable for two non-terminal or one terminal
        eliminateThreeTerminal();        //Step 4. Eliminate RHS with more than two non-terminals.
        outputResult();                  //Print the output to console
    }
    private void outputResult() throws IOException {
        System.out.println("NON-TERMINAL");
        for (Object key : variableToProductionMap.keySet()){
            System.out.println(key);
        }
        System.out.println("TERMINAL");
        Path path = Paths.get(filePath);
        for (int i = terminalIndex; i < ruleIndex - 1; i++){
            System.out.println(Files.readAllLines(path).get(i));
        }
        System.out.println("RULES");
        printMap();
        System.out.println("START");
        System.out.println("S");
    }

    private void eliminateUnitProductions() {
        for (int i = 0; i < rulesCount; i++) {
            removeSingleVariable();
        }
    }
    private void eliminateThreeTerminal() {
        for (int i = 0; i < rulesCount; i++) {
            removeThreeTerminal();
        }
    }
    private void eliminateNullProductions() {
        for (int i = 0; i < rulesCount; i++) {
            removeEpsilon();
        }
    }
    private String[] splitEnter(String input) {
        String[] tmpArray = new String[rulesCount];
        for (int i = 0; i < rulesCount; i++) {
            tmpArray = input.split("\\n");
        }
        return tmpArray;
    }
    private void printMap() {
        for (Map.Entry<String, List<String>> stringListEntry : variableToProductionMap.entrySet()) {
            System.out.println(stringListEntry.getKey() + ":" + stringListEntry.getValue());
        }
    }
    private void convertStringToMap() {
        String[] splitEnterInput = splitEnter(input);
        for (String s : splitEnterInput) {
            String[] tempString = s.split(":|\\|");
            String variable = tempString[0].trim();
            String[] production = Arrays.copyOfRange(tempString, 1, tempString.length);
            List<String> productionList = new ArrayList<String>();
            for (int k = 0; k < production.length; k++) {
                production[k] = production[k].trim();
            }
            Collections.addAll(productionList, production);
            variableToProductionMap.put(variable, productionList);
        }
    }
    private void insertNewStartSymbol() {
        String newStart = "S0";
        List<String> newProduction = new ArrayList<>();
        newProduction.add("S");
        variableToProductionMap.put(newStart, newProduction);
    }
    private void removeEpsilon() {
        Iterator<Map.Entry<String, List<String>>> myIterator = variableToProductionMap.entrySet().iterator();
        Iterator<Map.Entry<String, List<String>>> myIterator2 = variableToProductionMap.entrySet().iterator();
        while (myIterator.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator.next();
            ArrayList<String> productionRow = (ArrayList<String>) entry.getValue();
            if (productionRow.contains("e")) {
                if (productionRow.size() > 1) {
                    productionRow.remove("e");
                    replaceEpsilon = entry.getKey();
                } else {
                    replaceEpsilon = entry.getKey();
                    variableToProductionMap.remove(replaceEpsilon);
                }
            }
        }
        while (myIterator2.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator2.next();
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
            for (int i = 0; i < productionList.size(); i++) {
                String temp = productionList.get(i);
                for (int j = 0; j < temp.length(); j++) {
                    if (replaceEpsilon.equals(Character.toString(productionList.get(i).charAt(j)))) {
                        if (temp.length() == 2) {
                            temp = temp.replace(replaceEpsilon, "");
                            if (!variableToProductionMap.get(entry.getKey()).contains(temp)) {
                                variableToProductionMap.get(entry.getKey()).add(temp);
                            }
                        } else if (temp.length() == 3) {
                            String deletedTemp = new StringBuilder(temp).deleteCharAt(j).toString();
                            if (!variableToProductionMap.get(entry.getKey()).contains(deletedTemp)) {
                                variableToProductionMap.get(entry.getKey()).add(deletedTemp);
                            }
                        } else if (temp.length() == 4) {
                            String deletedTemp = new StringBuilder(temp).deleteCharAt(j).toString();
                            if (!variableToProductionMap.get(entry.getKey()).contains(deletedTemp)) {
                                variableToProductionMap.get(entry.getKey()).add(deletedTemp);
                            }
                        } else {
                            if (!variableToProductionMap.get(entry.getKey()).contains("e")) {
                                variableToProductionMap.get(entry.getKey()).add("e");
                            }
                        }
                    }
                }
            }
        }
    }
    private void removeDuplicateKeyValue() {
        Iterator<Map.Entry<String, List<String>>> myIterator3 = variableToProductionMap.entrySet().iterator();
        while (myIterator3.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator3.next();
            ArrayList<String> productionRow = (ArrayList<String>) entry.getValue();
            for (int i = 0; i < productionRow.size(); i++) {
                if (productionRow.get(i).contains(entry.getKey())) {
                    productionRow.remove(entry.getKey());
                }
            }
        }
    }
    private void removeSingleVariable() {
        Iterator<Map.Entry<String, List<String>>> myIterator4 = variableToProductionMap.entrySet().iterator();
        String key;
        while (myIterator4.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator4.next();
            Set<String> set = variableToProductionMap.keySet();
            ArrayList<String> keySet = new ArrayList<>(set);
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
            for (int i = 0; i < productionList.size(); i++) {
                String temp = productionList.get(i);
                for (int j = 0; j < temp.length(); j++) {
                    for (String value : keySet) {
                        if (value.equals(temp)) {
                            key = entry.getKey().toString();
                            List<String> productionValue = variableToProductionMap.get(temp);
                            productionList.remove(temp);
                            for (String s : productionValue) {
                                variableToProductionMap.get(key).add(s);
                            }
                        }
                    }
                }
            }
        }
    }
    private Boolean checkDuplicateInProductionList(Map<String, List<String>> map, String key) {
        boolean notFound = true;
        Iterator<Map.Entry<String, List<String>>> myIterator = map.entrySet().iterator();
        outerLoop:
        while (myIterator.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator.next();
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
            for (int i = 0; i < productionList.size(); i++) {
                if (productionList.size() < 2) {
                    if (productionList.get(i).equals(key)) {
                        notFound = false;
                        break outerLoop;
                    } else {
                        notFound = true;
                    }
                }
            }
        }
        return notFound;
    }
    private void onlyTwoTerminalandOneVariable() {
        Iterator<Map.Entry<String, List<String>>> myIterator5 = variableToProductionMap.entrySet().iterator();
        String key = null;
        int asciiBegin = 70; //F
        Map<String, List<String>> tempList = new LinkedHashMap<>();
        while (myIterator5.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator5.next();
            Set<String> set = variableToProductionMap.keySet();
            ArrayList<String> keySet = new ArrayList<>(set);
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
            boolean found1, found2;
            boolean found = false;
            for (int i = 0; i < productionList.size(); i++) {
                String temp = productionList.get(i);
                for (int j = 0; j < temp.length(); j++) {
                    if (temp.length() == 3) {
                        String newProduction = temp.substring(1, 3); // SA
                        found = checkDuplicateInProductionList(tempList, newProduction) && checkDuplicateInProductionList(variableToProductionMap, newProduction);
                        if (found) {
                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newProduction);
                            key = Character.toString((char) asciiBegin);
                            tempList.put(key, newVariable);
                            asciiBegin++;
                        }
                    } else if (temp.length() == 2) { // if only two substring
                        for (String s : keySet) {
                            if (!s.equals(Character.toString(productionList.get(i).charAt(j)))) { // if substring not equals to keySet
                                found = false;
                            } else {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            String newProduction = Character.toString(productionList.get(i).charAt(j));
                            if (checkDuplicateInProductionList(tempList, newProduction) && checkDuplicateInProductionList(variableToProductionMap, newProduction)) {
                                ArrayList<String> newVariable = new ArrayList<>();
                                newVariable.add(newProduction);
                                key = Character.toString((char) asciiBegin);
                                tempList.put(key, newVariable);
                                asciiBegin++;
                            }
                        }
                    } else if (temp.length() == 4) {
                        String newProduction1 = temp.substring(0, 2); // SA
                        String newProduction2 = temp.substring(2, 4); // SA
                        found1 = checkDuplicateInProductionList(tempList, newProduction1) && checkDuplicateInProductionList(variableToProductionMap, newProduction1);
                        found2 = checkDuplicateInProductionList(tempList, newProduction2) && checkDuplicateInProductionList(variableToProductionMap, newProduction2);
                        if (found1) {
                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newProduction1);
                            key = Character.toString((char) asciiBegin);
                            tempList.put(key, newVariable);
                            asciiBegin++;
                        }
                        if (found2) {
                            ArrayList<String> newVariable = new ArrayList<>();
                            newVariable.add(newProduction2);
                            key = Character.toString((char) asciiBegin);
                            tempList.put(key, newVariable);
                            asciiBegin++;
                        }
                    }
                }
            }
        }
        variableToProductionMap.putAll(tempList);
    }
    private void removeThreeTerminal() {
        Iterator<Map.Entry<String, List<String>>> myIterator = variableToProductionMap.entrySet().iterator();
        ArrayList<String> keyList = new ArrayList<>();
        Iterator<Map.Entry<String, List<String>>> myIterator2 = variableToProductionMap.entrySet().iterator();
        while (myIterator.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator.next();
            ArrayList<String> productionRow = (ArrayList<String>) entry.getValue();
            if (productionRow.size() < 2) {
                keyList.add(entry.getKey());
            }
        }
        while (myIterator2.hasNext()) {
            Map.Entry<String, List<String>> entry = myIterator2.next();
            ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
            if (productionList.size() > 1) {
                for (int i = 0; i < productionList.size(); i++) {
                    String temp = productionList.get(i);
                    for (int j = 0; j < temp.length(); j++) {
                        if (temp.length() > 2) {
                            String stringToBeReplaced1 = temp.substring(j);
                            String stringToBeReplaced2 = temp.substring(0, temp.length() - j);
                            for (String key : keyList) {
                                List<String> keyValues = new ArrayList<>();
                                keyValues = variableToProductionMap.get(key);
                                String[] values = keyValues.toArray(new String[0]);
                                String value = values[0];
                                if (stringToBeReplaced1.equals(value)) {
                                    variableToProductionMap.get(entry.getKey()).remove(temp);
                                    temp = temp.replace(stringToBeReplaced1, key);
                                    if (!variableToProductionMap.get(entry.getKey()).contains(temp)) {
                                        variableToProductionMap.get(entry.getKey()).add(i, temp);
                                    }
                                } else if (stringToBeReplaced2.equals(value)) {
                                    variableToProductionMap.get(entry.getKey()).remove(temp);
                                    temp = temp.replace(stringToBeReplaced2, key);
                                    if (!variableToProductionMap.get(entry.getKey()).contains(temp)) {
                                        variableToProductionMap.get(entry.getKey()).add(i, temp);
                                    }
                                }
                            }
                        } else if (temp.length() == 2) {
                            for (String key : keyList) {
                                List<String> keyValues;
                                keyValues = variableToProductionMap.get(key);
                                String[] values = keyValues.toArray(new String[0]);
                                temp = getString(entry, i, temp, key, values);
                            }
                        }
                    }
                }
            } else if (productionList.size() == 1) {
                for (int i = 0; i < productionList.size(); i++) {
                    String temp = productionList.get(i);
                    if (temp.length() == 2) {
                        for (String key : keyList) {
                            List<String> keyValues;
                            keyValues = variableToProductionMap.get(key);
                            String[] values = keyValues.toArray(new String[0]);
                            temp = getString(entry, i, temp, key, values);
                        }
                    }
                }
            }
        }
    }
    private String getString(Map.Entry<String, List<String>> entry, int i, String temp, String key, String[] values) {
        String value = values[0];
        for (int pos = 0; pos < temp.length(); pos++) {
            String tempChar = Character.toString(temp.charAt(pos));
            if (value.equals(tempChar)) {
                variableToProductionMap.get(entry.getKey()).remove(temp);
                temp = temp.replace(tempChar, key);
                if (!variableToProductionMap.get(entry.getKey()).contains(temp)) {
                    variableToProductionMap.get(entry.getKey()).add(i, temp);
                }
            }
        }
        return temp;
    }
}