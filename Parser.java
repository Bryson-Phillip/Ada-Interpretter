import java.io.*;
import java.util.Scanner;
public class Parser {
    public static String[] output_array = {};
    public static String output = "";
    public static String symbol_list = "";
    public static String pos_list = "";
    public static Interpreter interpreter = new Interpreter();
    // error is outputed when an error is detected, the position and nature of the error is recorded with the original lexeme
    public static String error = "";
    //stores the current character position within the output file
    public static int cursor = 0;
    // stores the position of the next token being evaluated, not in terms of its character position
    public static int token_num = 1;
    // a flag that may specify if a special action should be taken upon the return of a succeed or fail from a subroutine
    public static boolean end = false;
    public static wrapped_boolean bool = new wrapped_boolean();

    //evaluates the next token and returns a value of true if it matches the argument, updating the cursor and token_num
    public static boolean terminal_check(String terminal){
        String token = "";
        int cursor1 = cursor;
        while (output.charAt(cursor) == ' ' && cursor != output.length() -1){
            cursor++;
        }
        while (output.charAt(cursor) != ' ' && cursor != output.length() -1){
            token += output.charAt(cursor);
            cursor++;
        }
        if (terminal.equals(token)){
            System.out.println(token + " was found: " + find_pos(token_num)[0]);
            token_num++;
            return true;
        }
        else {cursor = cursor1; return false;}

    }

    // Scans pos_list to find a lexeme at a particular position (count) and extracts its value and position
    public static String[] find_pos(int count){
        Scanner scan = new Scanner(pos_list);
        String[] array = {"",""};
        for (int i = 0; i < count; i++){
            String temp = scan.next();
            array[0] = temp.substring(0,temp.indexOf("|"));
            array[1] = temp.substring(temp.indexOf("|")+1);
        }
        return array;
    }


    // checks if a valid variable declaration has been made with corresponding Enter and Exit statements and error statements
    // two tokens are evaluated for correctness
    public static boolean DECLARATION_check(String name){
        boolean check;
        System.out.println("Enter terminal");
        check = terminal_check("DEC_OP");
        System.out.println("Exit terminal");
        if (!check){
            // lack of changed 'end' flag indicates that varlist should check for an initialization afterwards
            return false;
        }
        System.out.println("Enter terminal");
        check = terminal_check("INT");
        System.out.println("Exit terminal");
        if (!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Improper variable declaration";
            // specifies that the declaration has started but was not completed correctly
            end = true;
            return false;
        }
        interpreter.declare(name);
        return true;
    }

    // Checks for a valid variable initialization
    // includes errors for each position in the initialization where an incorrect token could be detected
    public static boolean INITIALIZATION_check(String name){
        boolean check;
        System.out.println("Enter terminal");
        check = terminal_check("ASSIGN_OP");
        System.out.println("Exit terminal");
        if (!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Assignment operator expected";
            return false;
        }
        int value = Integer.parseInt(find_pos(token_num)[0]);
        System.out.println("Enter terminal");
        check = terminal_check("VALUE");
        System.out.println("Exit terminal");
        if (!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Improper variable assignment";
            return false;
        }
        System.out.println("Enter terminal");
        check = terminal_check("SEMI");
        System.out.println("Exit terminal");
        if (!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Missing Semicolon";
            return false;
        }
        interpreter.initialize(name, value);
        return true;
    }

    //checks for successive declarations or initializations, evaluating the success of the subroutines for each of them
    public static boolean VARLIST_check(){
        boolean check;
        String name = find_pos(token_num)[0];
        System.out.println("Enter terminal");
        check = terminal_check("NAME");
        System.out.println("Exit terminal");
        if (!check){
            //'end' is set to true to indicate to the calling function that an empty varlist
            end = true;
            return true;
        }
        System.out.println("Enter <declaration>");
        check = DECLARATION_check(name);
        System.out.println("Exit <declaration>");
        //recursively enters varlist to evaluate more declarations and initializations
        //evaluates the success of the subsequent varlist instances
        if (check){
            System.out.println("Enter <varlist>");
            check = VARLIST_check();
            System.out.println("Exit <varlist>");
            if (!check){return false;}
            else{end = false; return true;}
        }
        //checks end flag to determine if a failed declaration check should result in an error
        if (end){ return false;}
        System.out.println("Enter <initialization>");
        check = INITIALIZATION_check(name);
        System.out.println("Exit <initialization>");
        //recursively enters varlist to evaluate more declarations and initializations
        //evaluates the success of the subsequent varlist instances
        if (check){

            System.out.println("Enter <varlist>");
            check = VARLIST_check();
            System.out.println("Exit <varlist>");
            if (!check){return false;}
            else{end = false; return true;}
        }
        return false;
    }

    //checks for the presence of a <num> phrase
    // records the value of a variable or literal in a member variable of the wrapped_boolean
    public static boolean NUM_check(wrapped_boolean temp){
        boolean check;
        System.out.println("Enter terminal");
        try {
            temp.temp1 = Integer.parseInt(find_pos(token_num)[0]);
        }catch (Exception io){}
        check = terminal_check("VALUE");
        if (check){return true;}

        String a = find_pos(token_num)[0];
        temp.temp1 = interpreter.RAM_value.get(interpreter.RAM_identifier.indexOf(a));
        check = terminal_check("NAME");
        System.out.println("Exit terminal");
        if(check){return true;}
        return false;
    }

    //checks for the presence of an operator phrase and generates an appropriate error
    //updates a member variable within a wrapped_boolean to be used in the comparison function
    public static boolean OPERATOR_check(wrapped_boolean temp){
        boolean check;
        System.out.println("Enter terminal");
        check = terminal_check("EQUAL");
        if(check){temp.temp0 = 0; return true;}
        check = terminal_check("GREATER_THAN");
        if(check){temp.temp0 = 1; return true;}
        check = terminal_check("LESS_THAN");
        if(check){temp.temp0 = 2; return true;}
        check = terminal_check("GREATER_EQUAL");
        if(check){temp.temp0 = 3; return true;}
        check = terminal_check("LESS_EQUAL");
        if(check){temp.temp0 = 4; return true;}
        check = terminal_check("NOT_EQUAL");
        System.out.println("Exit terminal");
        if(check){temp.temp0 = 5; return true;}
        error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Relational operator expected";
        return false;
    }

    // checks for the presence of a <comparison> phrase
    //evaluates the first lexeme of each possible rule for <comparison>
    //generates errors if subsequent lexemes are incorrect or if no correct first lexeme is found
    public static boolean COMPARISON_check(wrapped_boolean is_true){
        is_true.bool = true;
        wrapped_boolean is_true1 = new wrapped_boolean();
        boolean check;
        //checks for a nested condition
        System.out.println("Enter terminal");
        check = terminal_check("PAREN1");
        System.out.println("Exit terminal");
        if(check){
            System.out.println("Enter <condition>");
            check = CONDITION_check(is_true1);
            is_true.bool = is_true1.bool;
            System.out.println("Exit <condition>");
            if(!check){
                return false;
            }
            System.out.println("Enter terminal");
            check = terminal_check("PAREN2");
            System.out.println("Exit terminal");
            if(!check){
                error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Ending parentheses expected";
                return false;}
            return true;
        }

        //checks for a nested negated condition
        System.out.println("Enter terminal");
        check = terminal_check("NEGATE");
        System.out.println("Exit terminal");
        if(check){
            System.out.println("Enter <condition>");
            check = CONDITION_check(is_true1);
            is_true.bool = !is_true1.bool;
            System.out.println("Exit <condition>");
            if(!check){
                return false;
            }
            System.out.println("Enter terminal");
            check = terminal_check("PAREN2");
            System.out.println("Exit terminal");
            if(!check){
                error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Ending parentheses expected";
                return false;}
            return true;
        }

        //checks for a relational expression
        //updates all three temporary variables of the wrapped_boolean
        System.out.println("Enter <num>");
        check = NUM_check(is_true1);
        is_true1.temp2 = is_true1.temp1;
        System.out.println("Exit <num>");
        if(check){
            System.out.println("Enter <operator>");
            check = OPERATOR_check(is_true1);
            System.out.println("Exit <operator>");
            if(!check){
                return false;
            }
            System.out.println("Enter <num>");
            check = NUM_check(is_true1);
            System.out.println("Exit <num>");
            if(!check){
                error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Second operand expected";
                return false;
            }

            //determines if the logical operation is true and updates the wrapped_boolean passed to it
            if (is_true1.temp0 == 0 && is_true1.temp1 == is_true1.temp2){is_true.bool = true;}
            else if (is_true1.temp0 == 1 && is_true1.temp1 > is_true1.temp2){is_true.bool = true;}
            else if (is_true1.temp0 == 2 && is_true1.temp1 < is_true1.temp2){is_true.bool = true;}
            else if (is_true1.temp0 == 3 && is_true1.temp1 >= is_true1.temp2){is_true.bool = true;}
            else if (is_true1.temp0 == 4 && is_true1.temp1 <= is_true1.temp2){is_true.bool = true;}
            else if (is_true1.temp0 == 5 && is_true1.temp1 != is_true1.temp2){is_true.bool = true;}
            else{is_true.bool = false;}
            return true;
        }
        error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Comparison expected";
        return false;

    }

    //checks for the presence of comparisons seperated by 'and' symbols
    //updates a wrapped boolean to determine the truthfulness of the entire statement
    public static boolean CONDITIONB_check(wrapped_boolean is_true){
        is_true.bool = true;
        wrapped_boolean is_true1 = new wrapped_boolean();
        boolean check;
        //checks the first comparison
        System.out.println("Enter <comparison>");
        check = COMPARISON_check(is_true1);
        if (!is_true1.bool){is_true.bool = false;}
        System.out.println("Exit <comparison>");
        if (!check){
            return false;
        }
        // checks for iterations of and symbols followed by comparisons
        while(true){
            System.out.println("Enter terminal");
            check = terminal_check("AND");
            System.out.println("Exit terminal");
            if(!check){return true;}
            System.out.println("Enter <comparison>");
            check = COMPARISON_check(is_true1);
            if (!is_true1.bool) {is_true.bool = false;}
            System.out.println("Exit <comparison>");
            if(!check){
                return false;
            }

        }
    }

    //checks for conditionB's separated by 'or' symbols
    //updates a wrapped boolean to determine the truthfulness of the entire statement
    public static boolean CONDITION_check(wrapped_boolean is_true){
        wrapped_boolean is_true1 = new wrapped_boolean();
        is_true.bool = false;
        boolean check;
        //updates count to output the position of the calling function with an error that originated lower
        int count = Integer.parseInt(find_pos(token_num)[1]);
        //checks the first conditionB
        System.out.println("Enter <conditionB>");
        check = CONDITIONB_check(is_true1);
        if(is_true1.bool){is_true.bool = true;}
        System.out.println("Exit <conditionB>");
        if (!check){
            error = "Error in condition at position" + count + ": \n" + error;
            return false;
        }
        //checks for iterations of 'or' symbols followed by conditionB's
        while(true){
            count = Integer.parseInt(find_pos(token_num)[1]);
            System.out.println("Enter terminal");
            check = terminal_check("OR");
            System.out.println("Exit terminal");
            if(!check){
                return true;}
            System.out.println("Enter <conditionB>");
            check = CONDITIONB_check(is_true1);
            if(is_true1.bool){is_true.bool = true;}
            System.out.println("Exit <conditionB>");
            if(!check){
                error = "Error in condition statement at position" + count + ": \n" + error;
                return false;
            }

        }
    }

    //cheks for valid if else statements with optional 'elsif' clauses
    public static boolean CONDITIONAL_check(){
        boolean check;
        //updates count to output the position of the calling function with an error that originated lower
        int count = Integer.parseInt(find_pos(token_num)[1]);
        //checks for the if clause
        //returns true with an altered end flag for the case of a non-existent if-else statement
        System.out.println("Enter terminal");
        check = terminal_check("IF");
        System.out.println("Exit terminal");
        if(!check){
            end = true;
            return true;
        }
        System.out.println("Enter <condition>");
        check = CONDITION_check(bool);
        //passes the final result of the logical operations to the interpreter
        interpreter.enter_condition(bool);
        System.out.println("Exit <condition>");
        if(!check){
            error = "Error in conditional statement at position" + count + ": \n" + error;
            return false;
        }
        System.out.println("Enter terminal");
        check = terminal_check("THEN");
        System.out.println("Exit terminal");
        if(!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Body of if-else statement not found";
            return false;
        }
        System.out.println("Enter <statementlist>");
        check = STATEMENTLIST_check();
        System.out.println("Exit <statementlist>");
        if(!check){
            error = "Error in if-else body at position" + count + ": \n" + error;
            return false;
        }
        interpreter.exit_condition();
        //checks for iterations of elsif clauses
        while(true){
            System.out.println("Enter terminal");
            check = terminal_check("ELSE_IF");
            System.out.println("Exit terminal");
            if(!check){
                break;
            }
            System.out.println("Enter <condition>");
            check = CONDITION_check(bool);
            //passes the final result of the logical operations to the interpreter
            interpreter.enter_condition(bool);
            System.out.println("Exit <condition>");
            if(!check){
                error = "Error in conditional statement at position" + count + ": \n" + error;
                return false;
            }
            System.out.println("Enter terminal");
            check = terminal_check("THEN");
            System.out.println("Exit terminal");
            if(!check){
                error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Body of if-else statement not found";
                return false;
            }
            System.out.println("Enter <statementlist>");
            check = STATEMENTLIST_check();
            System.out.println("Exit <statementlist>");
            if(!check){
                error = "Error in if-else body at position" + count + ": \n" + error;
                return false;
            }
            interpreter.exit_condition();
        }
        //checks for the else clause
        System.out.println("Enter terminal");
        check = terminal_check("ELSE");
        System.out.println("Exit terminal");
        if(!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : 'else' clause not found";
            return false;
        }
        System.out.println("Enter <statementlist>");
        check = STATEMENTLIST_check();
        System.out.println("Exit <statementlist>");
        if(!check){
            error = "Error in if-else body at position" + count + ": \n" + error;
            return false;
        }
        System.out.println("Enter terminal");
        check = terminal_check("END");
        System.out.println("Exit terminal");
        if(!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : end if statement expected";
            return false;
        }
        System.out.println("Enter terminal");
        check = terminal_check("IF_");
        System.out.println("Exit terminal");
        if(!check){
            error = "Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Improper end-if statement";
            return false;
        }
        interpreter.reset();
        return true;
    }

    //checks for valid statementlists
    public static boolean STATEMENTLIST_check() {
        boolean check;
        //updates count to output the position of the calling function with an error that originated lower
        int count = Integer.parseInt(find_pos(token_num)[1]);
        System.out.println("Enter <conditional>");
        check = CONDITIONAL_check();
        System.out.println("Exit <conditional>");
        //recursively calls statmentlist checking for invalid conditionals or forwarding the check to varlist
        if (!check) {
            error = "Error in conditional statement at position" + count + ": \n" + error;
            return false;
        } else if (end == true) {
            end = false;
        } else {
            System.out.println("Enter <statementlist>");
            check = STATEMENTLIST_check();
            System.out.println("Exit <statementlist>");
            if (!check) {
                return false;
            } else {
                return true;
            }
        }
        System.out.println("Enter <varlist>");
        check = VARLIST_check();
        System.out.println("Exit <varlist>");
        //recursively calls statmentlist checking for invalid varlists
        if (!check) {
            error = "Error in variable list at position" + count + ": \n" + error;
            return false;
        } else if (end == true) {end = false; return true;}
        else {
            System.out.println("Enter <statementlist>");
            check = STATEMENTLIST_check();
            System.out.println("Exit <statementlist>");
            if (!check) {
                return false;
            } else {
                return true;
            }
        }
    }



    //the root of the recursive descent  that initiates all further calls
    public static boolean PROCEDURE_check(){
        System.out.println();
        System.out.println("Enter <procedure>");
        boolean check;
        System.out.println("Enter terminal");
        check = terminal_check("PROC_START");
        System.out.println("Exit terminal");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Procedure start expected");
            return false;
        }

        System.out.println("Enter <title>");
        check = terminal_check("NAME");
        System.out.println("Exit <title>");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Procedure name expected");
            return false;
        }

        System.out.println("Enter terminal");
        check = terminal_check("PROC_ASSIGN");
        System.out.println("Exit terminal");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Beginning of variable list expected");
            return false;
        }

        System.out.println("Enter <varlist>");
        int count = Integer.parseInt(find_pos(token_num)[1]);
        check = VARLIST_check();
        System.out.println("Exit <varlist>");
        if(!check){
            error = "Error in variable list at position " + count + ": \n" + error;
            System.out.println(error);
            return false;
        }
        end = false;

        System.out.println("Enter terminal");
        check = terminal_check("FUNC_START");
        System.out.println("Exit terminal");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Beginning of function expected");
            return false;
        }

        System.out.println("Enter <statementlist>");
        check = STATEMENTLIST_check();
        System.out.println("Exit <statementlist>");
        if(!check){
            error = "Error in statement list at position " + count + ": \n" + error;
            System.out.println(error);
            return false;
        }

        System.out.println("Enter terminal");
        check = terminal_check("END");
        System.out.println("Exit terminal");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Missing function termination");
            System.out.println(error);
            return false;
        }

        System.out.println("Enter terminal");
        check = terminal_check("NAME");
        System.out.println("Exit terminal");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Function termination should include function name");
            System.out.println(error);
            return false;
        }

        System.out.println("Enter terminal");
        check = terminal_check("SEMI");
        System.out.println("Exit terminal");
        if(!check){
            System.out.println("Error at position " + find_pos(token_num)[1] + " Symbol: " + find_pos(token_num)[0] + " : Missing semicolon");
            System.out.println(error);
            return false;
        }
        return true;
    }
    //main function which reassigns the return values from the scanner starts the parsing
    public static void main(String[] args) throws IOException{
        LexicalAnalyzer lex = new LexicalAnalyzer();
        output_array = lex.lex_main();
        if (lex.flag == true){System.exit(1);}
        output = output_array[0];
        symbol_list = output_array[1];
        pos_list = output_array[2];
        PROCEDURE_check();
        //Outputs the final result of all the variables
        for (String a : interpreter.RAM_identifier){
            System.out.println(a + ": " + interpreter.RAM_value.get(interpreter.RAM_identifier.indexOf(a)));
        }


    }
}
