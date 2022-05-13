import java.util.ArrayList;
import java.util.Stack;

public class Interpreter {
    // RAM_value stores an array of variable values
    public ArrayList<Integer> RAM_value = new ArrayList<Integer>();
    // RAM_identifier stores an array of variable names
    public ArrayList<String> RAM_identifier = new ArrayList<String>();
    // flag is a stack of boolean varbales which stores the current state of a conditional at each layer of nesting
    public Stack<Boolean> flag = new Stack<Boolean>();
    // contiguous is set to true if an if clause has already been performed for the current if-else statement
    private boolean contiguous = false;
    //just-reset is set to true after a conditional ends, and is used as a flag to determine the state of 'contiguous' in subsequent conditionals
    private boolean just_reset = false;

    Interpreter(){
        flag.push(true);
    }

    // Stores a new variable in the simulated RAM  and checks if the same variable already exists
    public boolean declare (String name){

        if (RAM_identifier.contains(name)) {
            System.out.println("Error: duplicate declaration of variable: " + name);
            return false;
        }
        else {
            if (flag.peek() && !contiguous) {
                RAM_identifier.add(name);
                RAM_value.add(0);
            }
            return true;
        }
    }

    //checks if a variable exists and initializes it
    public boolean initialize (String name, int value){
        if (!RAM_identifier.contains(name)){
            System.out.println("Error: Variable " + name + " does not exist");
            return false;
        }
        else{
            if (flag.peek() && !contiguous) {
                RAM_value.set(RAM_identifier.indexOf(name), value);
            }
            return true;
        }
    }

    //updates the stack with the value of the new condition, resets contiguous for the next if-else statment
    public void enter_condition(wrapped_boolean condition){
        if (just_reset){
            contiguous = false;
        }
        flag.push(condition.bool);
    }

    //updates the stack by removing the previous condition
    //ensures that the value of 'contiguous' for the parent scope is preserved once the nested clause ends
    public void exit_condition(){
        if (just_reset){
            contiguous = true;
            just_reset = false;
        }

        if (flag.peek()){
            contiguous = true;
            flag.pop();
        }
        else{
            flag.pop();
        }
    }

    //marks the end of an if-else statement
    // 'just_reset' will be used to reverse the default value of contiguous, should the program return to an unfinished if_else in a parent scope
    public void reset(){
        just_reset = true;
    }
}
