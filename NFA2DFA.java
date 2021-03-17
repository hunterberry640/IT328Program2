/* 
Author: Hunter Berry
Date: March 17th, 2021
Desc: Program that computes NFA read from a textfile to a DFA and then
        Tests the DFA on strings in a test file.
      command line use: $ java NFA2DFA X.nfa strings.txt
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * NFA2DFA
 */
public class NFA2DFA {
    static ArrayList<ArrayList<Set<Integer>>> NFAGraph = new ArrayList<>();
    static ArrayList<ArrayList<Set<Integer>>> DFAGraph = new ArrayList<>();
    static ArrayList<ArrayList<Integer>> simplifiedDFA = new ArrayList<>();
    static ArrayList<Integer> finStates = new ArrayList<>();
    static ArrayList<Set<Integer>> DFAfinStates = new ArrayList<>();
    static ArrayList<Integer> simplifiedDFAfinStates = new ArrayList<>();
    static ArrayList<Set<Integer>> Qprime;
    static ArrayList<Set<Integer>> statesToAdd;
    static String[] transitions;
    static int startState;
    static int numStates;
    static int numTransitions;
    static int initState;
    public static void main(String[] args) {
        ArrayList<Set<Integer>> state;
        HashSet<Integer> stateTransitions;
        
        StringTokenizer st;
        String graphfile = args[0];
        String testFile = args[1];
        numTransitions = 0;
        String token;
        int initState;
        Scanner lineScanner = null;
        System.out.println("\n" + graphfile + " to DFA: \n");
        try 
        {
        lineScanner = new Scanner(new File(graphfile)); // scanner that reads file line by line
        } 
        catch (FileNotFoundException e) 
        {
        System.out.println("file not found " + graphfile + "\n " + e);
        }

        String line = lineScanner.nextLine(); 
        
        numStates = Integer.parseInt(line);

        line = lineScanner.nextLine();

        transitions = line.split(" ");
        numTransitions = transitions.length;
       int count = 0;
        while (count < numStates) {

            line = lineScanner.nextLine();

            st = new StringTokenizer(line, "{},", true);

            state = new ArrayList<>();

            while (st.hasMoreTokens()){ 
                token = st.nextToken();
                stateTransitions = new HashSet<>();
                if(token.equals("{")){
                    while(!token.equals("}")){
                        token = st.nextToken();
                        if(!token.equals(",") && !token.equals("}")){
                            
                            stateTransitions.add(Integer.parseInt(token));
                        }
                    }
                    state.add(stateTransitions);
                }
            }
            NFAGraph.add(state);
            count++;
        }

        initState = Integer.parseInt(lineScanner.nextLine());
        finStates = new ArrayList<>();
        st = new StringTokenizer(lineScanner.nextLine(), "{},", true);
        while (st.hasMoreTokens()){ 
            token = st.nextToken();
            if(token.equals("{")){
                while(!token.equals("}")){
                    token = st.nextToken();
                    if(!token.equals(",") && !token.equals("}")){
                        finStates.add(Integer.parseInt(token));
                    }
                }
            }
        }
        

        // create DFA
        // initialize with initial state
        Qprime = new ArrayList<>(); // set of states already in dfa
        statesToAdd = new ArrayList<>(); // set of states that need put in dfa
        Set<Integer> first = new HashSet<>();
        
        first.add(initState);
        statesToAdd.add(first); // queue with states that will be added


        ArrayList<Set<Integer>> stateRow = new ArrayList<>();
        
        while(!statesToAdd.isEmpty()){
            
            for (Integer i : finStates) {
                if(statesToAdd.get(0).contains(i)){
                    DFAfinStates.add(statesToAdd.get(0));
                    simplifiedDFAfinStates.add(DFAGraph.size());// this row will be next row inserted and will be a final state
                    break;
                }
            }            
            
            stateRow = addState(statesToAdd.get(0));
                
            
            Qprime.add(statesToAdd.get(0));
            statesToAdd.remove(0); //remove just inserted state

            for (Set<Integer> states : stateRow) {
                if(!Qprime.contains(states) && !statesToAdd.contains(states)){
                    statesToAdd.add(states);
                }
            }
            DFAGraph.add(stateRow);
        
        }
        printDFAGraph();
        checkSolution(testFile);
        
    }



    public static void printNFAGraph(){
        for (ArrayList<Set<Integer>> row : NFAGraph) {
            for (Set<Integer> item : row) {
                System.out.print("{");
                for (Integer i : item) {
                    if(item.size()>1){
                        System.out.print(i + ",");
                    }else{
                        System.out.print(i);
                    }
                }
                System.out.print("} ");
            }
            System.out.println();
        }
    }

    public static void printDFAGraph(){
        int rowNum = 0;
        System.out.printf("%5s", "Sigma:");
        for (String a : transitions) {
            System.out.printf("%5s", a);
        }
        System.out.println("\n--------------------------------------------");
        for (ArrayList<Set<Integer>> row : DFAGraph) {
            System.out.printf("%5s ", rowNum + ":");
            rowNum++;
            ArrayList<Integer> simpleifiedRow = new ArrayList<>();
            for (Set<Integer> item : row) {
                int itemNum =0;
                for(int i = 0; i < Qprime.size(); i++){
                    if(item.equals(Qprime.get(i))){
                        itemNum = i;
                    }
                }

                // System.out.print("{"); // uncomment to print the actual values of the states instead of numbers
                // for (Integer i : item) {
                //     if(item.size()>1){
                //         System.out.print(i + ",");
                //     }else{
                //         System.out.print(i);
                //     }
                // }
                // System.out.print("} ");

                System.out.printf("%5s",itemNum);
                simpleifiedRow.add(itemNum);
            }
            simplifiedDFA.add(simpleifiedRow);
            System.out.println();
        }
        
        System.out.println("--------------------------------------------");
        System.out.println(initState + ": Initial State");
        for (int state : simplifiedDFAfinStates) {
            if(state != simplifiedDFAfinStates.get(simplifiedDFAfinStates.size()-1)){
                System.out.print(state + ", ");
            }
            else{
                System.out.print(state + ": ");
            }
        }
        System.out.print("Accepting State(s)");
        System.out.println("\n");

    }


    public static ArrayList<Set<Integer>> addState( Set<Integer> currentStates){
        ArrayList<Set<Integer>> row = new ArrayList<>();
       
        for(int num =0; num < numTransitions; num++){
            Set<Integer> lst = new HashSet<>();
            row.add(lst);
        }

        for (Integer i : currentStates) {
            for (int j=0; j < numTransitions; j++){
                for (Integer ii : NFAGraph.get(i).get(j)) {
                    row.get(j).addAll(NFAGraph.get(ii).get(numTransitions));
                    row.get(j).addAll(lClosure(NFAGraph.get(ii).get(numTransitions), new HashSet<Integer>()));
                }
            }
            
        }
        return row;
    }

    //method to get the recursive lambda closure of the states
    public static Set<Integer> lClosure(Set<Integer> state, Set<Integer> set){
        if(set.containsAll(state)){
            return set;
        }
        
        if(state.size()==1){
            set.addAll(state);
        }
        else
        {
            for (Integer st : state) {
                set.addAll(state);
                set.addAll(lClosure(NFAGraph.get(st).get(numTransitions), set));
            }
        }
        return set;
    }

    public static void checkSolution(String testFile){
        System.out.println("Parsing results of Strings in " + testFile + ": ");
        ArrayList<String> inputs= new ArrayList<>();
        inputs.add("a");
        inputs.add("b");
        inputs.add("c");
        inputs.add("d");
        inputs.add("e");
        inputs.add("f");
        Scanner lineScanner = null;
        String testString = null;
        try 
        {
        lineScanner = new Scanner(new File(testFile)); // scanner that reads file line by line
        } 
        catch (FileNotFoundException e) 
        {
        System.out.println("file not found " + testFile + "\n " + e);
        }
        int count = 0;
        while(lineScanner.hasNext()){
            testString = lineScanner.nextLine();
            // System.out.println(testString);
            String[] testArr = testString.split("");
            int currentState = 0;
            int lastInput = 0;

            for (String str : testArr) {
                if(str == ""){
                    break;
                }
                if(inputs.indexOf(str) >= numTransitions){
                    lastInput = inputs.indexOf(str);
                    break;
                }
                
                currentState = simplifiedDFA.get(currentState).get(inputs.indexOf(str));

                lastInput = inputs.indexOf(str);
            }
            count++;
            if(count == 16){
                System.out.println();
            }
            if(lastInput >= numTransitions){
                System.out.printf("%-5s ", "No ");
                
            }else if (simplifiedDFAfinStates.contains(currentState)){ // if dfa ends in accepting state, accept
                System.out.printf("%-5s ","Yes " );
            }else{
                System.out.printf("%-5s ", "No ");
            }
            
        }
        System.out.println();
    }
}