# Ada-Interpretter
An Interpreter for a subset of the Ada language

# Source Code Description (Lexical Analyzer)

In the main function of LexicalAnalyzer, a scanner is created to read input from a file
stream. The file path is specified by the user upon running the scanner. The contents of
the file are stored in a string variable named ‘input’, and formatted to convert all
whitespace to a single type of character. The function ‘tokenize’ reads the next lexeme
from the input string and updates values for the position of the cursors ‘curr_char’ and
‘lex_count’. It skips whitespace and records the current lexeme into its own variable,
where it is either matched to a predetermined substring or determined to be a unique
name or number. Two arrays are used to match a lexeme to a corresponding token,
where it is appended to the initially empty ‘output’ string. If a substring is not recognized,
an error message is displayed and an error code is put in its place within the ‘output’
string. ‘Tokenize’ runs until the cursor is at the end, and the program prints ‘output’.

# EBNF Grammar (Lexical Analyzer)

<procedure> → procedure <title> is <varlist> begin <statementlist> end <title> ;
<title> → (a b c … z A B C … Z 0 1 2 3 4 5 6 7 8 9) {(a b c … z A B C … Z 0 1 2 3 4 5 6 7 8 9)}
<varlist> → <declaration> <varlist>
  | <initialization> <varlist>
  | ε
<declaration> → <title> : Integer;
<initialization> → <title> := <value>;
<value> → (0 1 2 3 4 5 6 7 8 9) {(0 1 2 3 4 5 6 7 8 9)}
<statementlist> -> <varlist> <statementlist>
  |<conditional> <statementlist>
  | ε
<conditional> → if <condition> then <statementlist> {elsif <condition> then <statementlist>} [else
<statementlist>] end if;
<condition> → <conditionB> {or <conditionB>}
<conditionB> → <comparison> {and <comparison>}
<comparison> → (<condition>)
  | !(<condition>)
  | <num> <operator> <num>
<num> → <title>
  | <value>
<operator> → (< > <= >= /=)
Note: the ellipsis is not a symbol

  
This grammar describes the rules involving <procedure> which represents a most basic
function within Ada. Within a function, start and end statements can be made, integers
can be declared and initialized, if-else statements can be nested, and complex
conditional statements can be made. Integer variables can be declared as names which
may be a string of letter or digit characters, and they may contain integers which must
be a string of digits. <statementlist> represents a series of variable initializations and
conditionals which make up the body of a function. It is defined recursively to have any
possible length. The conditional if-else statements are described to have nested
statement lists. The condition inside the if-else statement has multiple non-terminals to
ensure proper parse trees are generated with correct operator precedence.
  
The empty string can be recognized as a complete statement. Programmatically, this
creates a condition where any erroneous phrase located in place of a needed
<varlist>/<statementlist> causes the program to assume one was provided with zero
elements. The erroneous phrase is then evaluated in terms of whatever would logically
come after the <varlist> or <statementlist>. Additionally, <statementlist> makes use of
the <varlist> instead of the <initialization>. Besides this, the grammar has recursive
elements that allows for nested conditional statements and complex boolean
expressions that preserve operator precedence and associativity, just like in the
previous report.
  
# Source Code Description (Parser)
  
The class ‘Parser’ implements the parser and is the class that includes the main
function. The original lexical analyzer is used as a subroutine within the main function to
generate output files and symbol tables to be used by the Parser members. The
function ‘lex_main’ in the scanner outputs an array of three strings which contain the
output file of tokens, a symbol table that includes the location within the file of all novel
identifiers that appear within the code, and a similar table of every lexeme and its
associated position. The latter table is used to clarify where an error has occurred in
error statements.
  
Within the parser, the function ‘find_pos’ takes in the numeric position of a token
in the output file, and outputs the corresponding lexeme and its original position. The
function ‘terminal_check’ will take in a possible token and check to see if it appears as
the next symbol in the output. If so, it updates the cursor and prints the location of the
lexeme. Each non terminal has a corresponding function that calls on functions of other
non terminals to parse its substructure. At the bottom of each branch, the
‘terminal_check’ is used to determine if the correct token is present. Each operation is
accompanied by a print statement that alerts the entering and exiting of a subroutine
related to a particular non terminal, thus tracing out the parse tree for that particular
code. The output statement from the terminal_check indicates that a particular branch of
the tree has been validated. Error statements build from the ground up at a specific
location. The locations of the parent code structures are appended at the front,
successively, as the parser backs out of each subroutine. Finally in the implementation
of <procedure> errors are printed.

# Source Code Description (Interpretter)
  
The ‘Interpreter’ class has been implemented to interface with the parsing
algorithm in real time. In particular the variable declaration/assignment statements and
all of the recursive functions involved in processing conditional statements activate
functions within the interpreter. Two arraylists, one indexing variable names
(‘RAM_indentifier’), and another storing the values of the simulated memory addresses
(‘RAM_value’), have been created as members of ‘interpreter’. The declare and initialize
functions add indexes of new variables to the arraylists and initialize their values
respectively. Within the ‘CONDITIONAL_check’ function of the parser,
‘enter_conditional’ is called from the interpreter which evaluates a boolean variable to
determine if the following variable manipulations will be processed. This function adds
to a stack of boolean variables, where the top element determines if the current nested
statement should be performed. The flag variable ‘contiguous’ determines if one of the if
clauses has already been activated, and prevents further statements from being
performed only within the current if-else.
  
A ‘wrapped_boolean’ object has been implemented primarily to allow for passing
boolean values by reference. Recursive parsing of conditional statements allows for
passing wrapped_boolean objects between them. The truthfulness of any subconditional can be stored in the object that was passed to it and referenced later on in
the functions that called it. This information is processed according to the logical
operation performed at each function, and the parent caller is passed the final result,
which is then forwarded to the interpreter. Separate temporary integer variables are
included within the wrapped_boolean object and are used to evaluate relational
operators at the lowest level.
