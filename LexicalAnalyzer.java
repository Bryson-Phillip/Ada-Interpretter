import java.util.Scanner;
import java.io.*;

public class LexicalAnalyzer {
    // Variable list, 'lex_array' is the list of lexemes, these line up with the corresponding tokens in 'token_array' in monospace fonts
    public String[] lex_array = {  "!(",      "(",      ")",       "procedure",  "is",          ":",      ":=",        "Integer;", "begin",      "if", "if;", "then", "elsif",   "else",   "=",     ">",            "<",         ">=",            "<=",         "/=",        "and", "or", "not", ";",    "end", ""};
    public String[] token_array = {"NEGATE",  "PAREN1", "PAREN2",  "PROC_START", "PROC_ASSIGN", "DEC_OP", "ASSIGN_OP", "INT",      "FUNC_START", "IF", "IF_", "THEN", "ELSE_IF", "ELSE",   "EQUAL", "GREATER_THAN", "LESS_THAN", "GREATER_EQUAL", "LESS_EQUAL", "NOT_EQUAL", "AND", "OR", "NOT", "SEMI", "END", "", "ERROR", "VALUE", "NAME"};
    public String symbol_list = "";
    public String pos_list = "";
    // 'Output stores' the output file of tokens, 'input' stores the original ada text file, 'lexeme' stores the current lexeme being parsed
    public String output = "";
    public String input = "";
    public String lexeme = "";
    // 'curr_char' and 'lex_count' store the current position of a lexeme for error messagegs
    public int curr_char = 0;
    public int lex_count = 0;
    // 'done' stores whether the scanning is complete
    public boolean flag = false;
    public boolean done = false;

    //scans individual lexemes
    public void tokenize(){
        if (input.length() > 0){
            // skips whitespace
            while (input.charAt(curr_char) == ' ' && curr_char != (input.length() - 1)){
                curr_char++;
            }
            int err_pos = curr_char;
            // records characters into 'lexeme'
            while ((input.charAt(curr_char) != ' ') && (curr_char != (input.length() - 1))){
                lexeme += input.charAt(curr_char);
                curr_char++;
            }
            // adds the character position and original value of each lexeme to a list to be used in parsing
            pos_list += " " + lexeme + "|" + err_pos + " ";
            // searches for lexeme in the array and adds its token to 'output', displays a message
            for (int i = 0; i < lex_array.length; i++){
                if (lex_array[i].equals(lexeme)){
                    output += " " + token_array[i];
                    System.out.println("Lexeme: " + lexeme + " translates to token: " + token_array[i]);
                    if (lexeme.equals("")){
                        done = true;
                    }
                    lexeme = "";
                    lex_count++;
                    return;
                }
            }
            // Determines if the current lexeme is number, name, or unidentified symbol
            int a = 3;
            for(int i = 0; i < lexeme.length(); i++){
                char b = lexeme.charAt(i);

                if (a == 3 && !(b > 47 && b < 58)){
                    a = 2;
                }
                if ( a == 2 && !(  (b > 47 && b < 58) || (b > 64 && b < 91) || (b > 96 && b < 123)  )){
                    a = 1;
                }
                if (a == 1) {break;}
            }
            // Adds the correct token to 'output' based on the state variable and displays a message
            if (a == 3){
                output += " " + "VALUE";
                System.out.println("Lexeme: " + lexeme + " translates to token: " + "VALUE");
                lexeme = "";
                lex_count++;
            }
            else if (a == 2){
                output += " " + "NAME";
                System.out.println("Lexeme: " + lexeme + " translates to token: " + "NAME");
                // adds new identifiers to the symbol table in a similar format as pos_list
                if (!symbol_list.contains(lexeme)){ symbol_list += " " + lexeme + "|" + err_pos + " ";}
                lexeme = "";
                lex_count++;
            }
            else{
                output += " " + "Error";
                System.out.println("Error: At lexeme " + lex_count + " Character " + err_pos + "; Unexpected symbol: " + lexeme);
                flag = true;
                lexeme = "";
                lex_count++;
            }



        }
    }
// formats the input so that all characters are recognizable
    public void format (){
        input = input.replace("\n", " ");
        input = input.replace("\r", " ");
        input = input.replace("\t", " ");
        input = input.replace(System.lineSeparator(), " ");
        input += " ";

    }
    public String[] lex_main () throws IOException{
        // asks the user to specify a text file to be scanned
        File in;
        Scanner scan = new Scanner (System.in);
        System.out.println("Enter the name of the input file");
        String filename = scan.nextLine();
        in = new File(filename);
        Scanner filescan = new Scanner(in);
        // scans the input file into 'input'
        try{
            while (true) {
                input += filescan.nextLine() + " ";
            }
        }
        catch (RuntimeException IO){
            System.out.println("Input recorded");
        }
        // formats the input
        format();
        // sequentially parses lexemes until all have been parsed
        System.out.println("Input: \n" + input +"\n\n");
        while (!done){
            tokenize();
        }
        // displays the string of tokens that will be parsed later, as well as the symbol table
        System.out.println("\n\nOutput: \n" + output);
        System.out.println("\n\nSymbol list: \n" + symbol_list);
        // outputs an array of both symbol lists and the output string
        String[] return_val = {output,symbol_list,pos_list};
        return return_val;
    }
}
