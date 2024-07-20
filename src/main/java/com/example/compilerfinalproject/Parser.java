package com.example.compilerfinalproject;

import java.util.*;
import java.util.regex.Pattern;

public class Parser {

    //The parser (Syntax analyzer), the second phase of the compilation process
    //It's job is to group set of tokens sent by the scanner and check if the source code follows the production rules(grammar)
    //of the language.
    List<Token> tokens;

    public Parser(List<Token> tokens) {
        this.tokens=tokens;
        initializeProductionRules();
        entries();
        initializeTables();
        parsingFunction();
    }

    static Map<String, List<String>> productionRules = new LinkedHashMap<>();
    static Map<String, List<String>> firstForNonTerminals = new LinkedHashMap<>();
    private static final Stack<String> stack = new Stack<>();


    //Add all the non-terminals in the production rules in the array non-terminals.
    static String[] nonTerminals = {
            "module-decl", "module-heading", "block", "declarations", "const-decl", "const-list",
            "var-decl", "var-list", "var-item", "name-list", "more-names", "data-type", "procedure-decl",
            "procedure-heading", "stmt-list", "statement", "ass-stmt", "exp", "exp-prime", "term",
            "term-prime", "factor", "add-oper", "mul-oper", "read-stmt", "write-stmt", "write-list",
            "more-write-value", "write-item", "if-stmt", "else-part", "while-stmt", "loop-stmt",
            "exit-stmt", "call-stmt", "condition", "relational-oper", "name-value", "value"
    };

    //Add all the terminals in the production rules in the array terminals.
    static String[] terminals = {
            "module", "name", ";", "begin", "end", "const", "=", "|=", "<", "<=", ">", ">=", ",",
            "ε", "var", ":", "integer", "real", "char", "procedure", "(", ")", ":=",
            "+", "-", "*", "/", "mod", "div", "readint", "readreal", "readchar", "readln",
            "writeint", "writereal", "writechar", "writeln", "if", "then", "else", "while",
            "do", "loop", "until", "exit", "call", "integer-value", "real-value","."
    };


    // For each production A –> α
    // First we will calculate the first(α), ),and then make entry A –> α in the table.
    // if First(non-terminal) contains ε as terminal,
    // then we will calculate the Follow(A) and for each terminal in Follow(A), then make entry A –>  ε in the table.
    private static void entries() {
        firstForNonTerminals.put("module-decl", Arrays.asList("module"));
        firstForNonTerminals.put("module-heading", Arrays.asList("module"));
        firstForNonTerminals.put("block", Arrays.asList("begin"));
        firstForNonTerminals.put("declarations", Arrays.asList("const", "procedure", "begin", "var"));
        firstForNonTerminals.put("const-decl", Arrays.asList("const", "procedure", "begin", "var"));
        firstForNonTerminals.put("const-list", Arrays.asList("name", "var", "procedure", "begin"));
        firstForNonTerminals.put("var-decl", Arrays.asList("var", "procedure", "begin"));
        firstForNonTerminals.put("var-list", Arrays.asList("name", "procedure", "begin"));
        firstForNonTerminals.put("var-item", Arrays.asList("name"));
        firstForNonTerminals.put("name-list", Arrays.asList("name"));
        firstForNonTerminals.put("more-names", Arrays.asList(",", ":",")"));
        firstForNonTerminals.put("data-type", Arrays.asList("integer", "real", "char"));
        firstForNonTerminals.put("procedure-decl", Arrays.asList("procedure", "begin"));
        firstForNonTerminals.put("procedure-heading", Arrays.asList("procedure"));
        firstForNonTerminals.put("stmt-list", Arrays.asList("name", "readint", "readreal", "readchar", "readln", "writeint","writechar", "writereal", "writeln", "if", "while", "loop", "exit", "call", "begin", ";", "end",
                "else", "until"));
        firstForNonTerminals.put("statement", Arrays.asList("name", "readint", "readreal", "readchar", "readln", "writeint","writechar", "writereal", "writeln", "if", "while", "loop", "exit", "call", "begin", ";"));
        firstForNonTerminals.put("ass-stmt", Arrays.asList("name"));
        firstForNonTerminals.put("exp", Arrays.asList("(", "name", "integer-value","real-value"));
        firstForNonTerminals.put("exp-prime", Arrays.asList("+", "-", ";",")"));
        firstForNonTerminals.put("term", Arrays.asList("(", "name", "integer-value", "real-value"));
        firstForNonTerminals.put("term-prime", Arrays.asList("*", "/", "mod", "div", "+", "-",";",")"));
        firstForNonTerminals.put("factor", Arrays.asList("(", "name", "integer-value", "real-value"));
        firstForNonTerminals.put("add-oper", Arrays.asList("+", "-"));
        firstForNonTerminals.put("mul-oper", Arrays.asList("*", "/", "mod", "div"));
        firstForNonTerminals.put("read-stmt", Arrays.asList("readint", "readreal", "readchar", "readln"));
        firstForNonTerminals.put("write-stmt", Arrays.asList("writeint", "writereal", "writechar", "writeln"));
        firstForNonTerminals.put("write-list", Arrays.asList("name", "integer-value", "real-value"));
        firstForNonTerminals.put("more-write-value", Arrays.asList(",", ")"));
        firstForNonTerminals.put("write-item", Arrays.asList("name","integer-value", "real-value"));
        firstForNonTerminals.put("if-stmt", Arrays.asList("if"));
        firstForNonTerminals.put("else-part", Arrays.asList("else", "end"));
        firstForNonTerminals.put("while-stmt", Arrays.asList("while"));
        firstForNonTerminals.put("loop-stmt", Arrays.asList("loop"));
        firstForNonTerminals.put("exit-stmt", Arrays.asList("exit"));
        firstForNonTerminals.put("call-stmt", Arrays.asList("call"));
        firstForNonTerminals.put("condition", Arrays.asList("name", "integer-value", "real-value"));
        firstForNonTerminals.put("relational-oper", Arrays.asList("=", "|=", "<", "<=", ">", ">="));
        firstForNonTerminals.put("name-value", Arrays.asList("name", "integer-value", "real-value"));
        firstForNonTerminals.put("value", Arrays.asList("integer-value", "real-value"));
    }

    //generating parsing table.
    private static final Set<String>[][] parsingTable = new Set[nonTerminals.length][terminals.length];

    private static void initializeTables() {

        //The table consist of rows and columns, the rows represents the nonTerminals and the column represent the terminals.
        for (int i = 0; i < nonTerminals.length; i++) {
            String nonTerminal = nonTerminals[i];  //current non-terminal
            List<String> setOfFirst = setOfFirst(nonTerminals[i]);  //get all first for the current non-terminal

            //for every first related to the setOfFirst, we will add the production rule in the table.
            for (int j = 0; j < terminals.length; j++) {
                if (setOfFirst.contains(terminals[j])) {
                    List<String> alternatives = productionRules.get(nonTerminal);  //get the production rule for the non-terminal
                    if (alternatives.size() == 1) {
                        parsingTable[i][j] = Collections.singleton(alternatives.get(0)); //adding the production rule to the table
                        System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                    }
                    if (alternatives.size() >= 2) {
                        //If the non-terminal has more than one production rule,
                        // we will add for its corressponding terminals, the appropriate production rule.
                        if (nonTerminals[i].equals("const-decl")) {
                            if (terminals[j].equals("var")||terminals[j].equals("begin") || terminals[j].equals("begin")) {
                                System.out.println(alternatives.get(1));
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }
                        if (nonTerminals[i].equals("const-list")) {
                            if (terminals[j].equals( "var") ||  terminals[j].equals("procedure") || terminals[j].equals("begin")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("var-decl")) {
                            if (terminals[j] == "procedure" || terminals[j] == "begin") {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }
                        if (nonTerminals[i].equals("var-list")) {
                            if (terminals[j].equals("procedure") || terminals[j].equals("begin")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }
                        if (nonTerminals[i].equals("more-names")) {
                            if (terminals[j].equals(":")  || terminals[j].equals(")") ) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("procedure-decl")) {
                            if (terminals[j].equals("begin")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("stmt-list")) {
                            if (terminals[j].equals("end") || terminals[j].equals("until") || terminals[j].equals("else")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }
                        if (nonTerminals[i].equals("statement")) {
                            if (terminals[j].equals("name")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                            if (terminals[j].equals("readint") || terminals[j].equals("readchar") || terminals[j].equals("readreal") || terminals[j].equals("readln")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            }
                            if (terminals[j].equals("writeint") || terminals[j].equals("writereal") || terminals[j].equals("writechar") || terminals[j].equals("writeln")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(2).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(2).toString()));
                            }
                            if (terminals[j].equals("if")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(3).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(3).toString()));
                            }
                            if (terminals[j].equals("while")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(4).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(4).toString()));
                            }
                            if (terminals[j].equals("loop")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(5).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(5).toString()));
                            }
                            if (terminals[j].equals("exit")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(6).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(6).toString()));
                            }
                            if (terminals[j].equals("call")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(7).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(7).toString()));
                            }
                            if (terminals[j].equals("begin")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(8).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(8).toString()));
                            }
                            if (terminals[j].equals(";")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(9).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(9).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("exp-prime")) {
                            if (terminals[j].equals(";") || terminals[j].equals(")")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("term-prime")) {
                            if (terminals[j].equals("+") || terminals[j].equals("-") || terminals[j].equals(";") || terminals[j].equals(")")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("more-write-value")) {
                            if (terminals[j].equals(")")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("else-part")) {
                            if (terminals[j].equals("end")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("data-type")) {
                            if (terminals[j].equals("integer")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("real")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else if (terminals[j].equals("char")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(2).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(2).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("factor")) {
                            if (terminals[j].equals("(")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("name") || terminals[j].equals("integer-value") ||terminals[j].equals("real-value")  ) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("add-oper")) {
                            if (terminals[j].equals("+")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("-")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("mul-oper")) {
                            if (terminals[j].equals("*")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("/")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else if (terminals[j].equals("mod")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(2).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(2).toString()));
                            } else if (terminals[j].equals("div")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(3).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(3).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("read-stmt")) {
                            if (terminals[j].equals("readint")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("readreal")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else if (terminals[j].equals("readchar")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(2).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(2).toString()));
                            } else if (terminals[j].equals("readln")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(3).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(3).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("write-stmt")) {
                            if (terminals[j].equals("writeint")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("writereal")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else if (terminals[j].equals("writechar")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(2).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(2).toString()));
                            } else if (terminals[j].equals("writeln")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(3).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(3).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("write-item")) {
                            if (terminals[j].equals("name")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("integer-value") || terminals[j].equals("real-value")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            }
                        }

                        if (nonTerminals[i].equals("relational-oper")) {
                            if (terminals[j].equals("=")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("|=")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            } else if (terminals[j].equals("<")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(2).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(2).toString()));
                            } else if (terminals[j].equals("<=")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(3).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(3).toString()));
                            } else if (terminals[j].equals(">")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(4).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(4).toString()));
                            } else if (terminals[j].equals(">=")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(5).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(5).toString()));
                            }
                        }
                        if (nonTerminals[i].equals("name-value")) {
                            if (terminals[j].equals("name")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if ((terminals[j].equals("integer-value") || terminals[j].equals("real-value"))) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            }
                        }
                        if (nonTerminals[i].equals("value")) {
                            if (terminals[j].equals("integer-value")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(0).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(0).toString()));
                            } else if (terminals[j].equals("real-value")) {
                                parsingTable[i][j] = Collections.singleton(alternatives.get(1).toString());
                                System.out.println(nonTerminals[i] + "," + terminals[j] + ":->>>" + Collections.singleton(alternatives.get(1).toString()));
                            }
                        }

                    }


                }

            }
        }
    }



    //This method is to initialize all the production rules.
    private static void initializeProductionRules() {
        productionRules.put("module-decl", Arrays.asList("module-heading declarations block name ."));
        productionRules.put("module-heading", Arrays.asList("module name ;"));
        productionRules.put("block", Arrays.asList("begin stmt-list end"));
        productionRules.put("declarations", Arrays.asList("const-decl var-decl procedure-decl"));
        productionRules.put("const-decl", Arrays.asList("const const-list", "ε"));
        productionRules.put("const-list", Arrays.asList("name = value ; const-list", "ε"));
        productionRules.put("var-decl", Arrays.asList("var var-list", "ε"));
        productionRules.put("var-list", Arrays.asList("var-item ; var-list", "ε"));
        productionRules.put("var-item", Arrays.asList("name-list : data-type"));
        productionRules.put("name-list", Arrays.asList("name more-names"));
        productionRules.put("more-names", Arrays.asList(", name-list", "ε"));
        productionRules.put("data-type", Arrays.asList("integer", "real", "char"));
        productionRules.put("procedure-decl", Arrays.asList("procedure-heading declarations block name ; procedure-decl", "ε"));
        productionRules.put("procedure-heading", Arrays.asList("procedure name ;"));
        productionRules.put("stmt-list", Arrays.asList("statement ; stmt-list", "ε"));
        productionRules.put("statement", Arrays.asList("ass-stmt", "read-stmt", "write-stmt", "if-stmt", "while-stmt", "loop-stmt", "exit-stmt", "call-stmt", "block", "ε"));
        productionRules.put("ass-stmt", Arrays.asList("name := exp"));
        productionRules.put("exp", Arrays.asList("term exp-prime"));
        productionRules.put("exp-prime", Arrays.asList("add-oper term exp-prime", "ε"));
        productionRules.put("term", Arrays.asList("factor term-prime"));
        productionRules.put("term-prime", Arrays.asList("mul-oper factor term-prime", "ε"));
        productionRules.put("factor", Arrays.asList("( exp )", "name-value"));
        productionRules.put("add-oper", Arrays.asList("+", "-"));
        productionRules.put("mul-oper", Arrays.asList("*", "/", "mod", "div"));
        productionRules.put("read-stmt", Arrays.asList("readint ( name-list )", "readreal ( name-list )", "readchar ( name-list )", "readln"));
        productionRules.put("write-stmt", Arrays.asList("writeint ( write-list )", "writereal ( write-list )", "writechar ( write-list )", "writeln"));
        productionRules.put("write-list", Arrays.asList("write-item more-write-value"));
        productionRules.put("more-write-value", Arrays.asList(", write-list", "ε"));
        productionRules.put("write-item", Arrays.asList("name", "value"));
        productionRules.put("if-stmt", Arrays.asList("if condition then stmt-list else-part end"));
        productionRules.put("else-part", Arrays.asList("else stmt-list", "ε"));
        productionRules.put("while-stmt", Arrays.asList("while condition do stmt-list end"));
        productionRules.put("loop-stmt", Arrays.asList("loop stmt-list until condition"));
        productionRules.put("exit-stmt", Arrays.asList("exit"));
        productionRules.put("call-stmt", Arrays.asList("call name"));
        productionRules.put("condition", Arrays.asList("name-value relational-oper name-value"));
        productionRules.put("relational-oper", Arrays.asList("=", "|=", "<", "<=", ">", ">="));
        productionRules.put("name-value", Arrays.asList("name", "value"));
        productionRules.put("value", Arrays.asList("integer-value", "real-value"));
    }



    //This method is to get the first for non-terminals.
    static List<String> setOfFirst(String nonTerminal) {
        List<String> firstSet = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : firstForNonTerminals.entrySet()) {
            if (entry.getKey().equals(nonTerminal)) {
                firstSet.addAll(entry.getValue());
                break;
            }
        }
        return firstSet;
    }

    public void parsingFunction() {

        stack.push("module-decl");
        List<Token> input = tokens;
        int i = 0;
        String index = input.get(i).value;
        while (!stack.isEmpty()) {
            String top = stack.pop();
            System.out.println("top of the stack:" + top);
            System.out.println("current index:  " + index);
            if (isInTerminals(top)) {
                if (top.equals(index)) {    // check if the top of the stack and current index are equal and from the terminal
                    System.out.println(top + " ==" + index);
                    i++;
                    if (i < input.size()) {
                        index = input.get(i).value; //get new token
                        System.out.println("new index:" + index);
                    }
                } else if (top.equals(checkType(index))) {   // check if the top of the stack and current index are equal and their value is name or number.
                    System.out.println(top + " ====" + index);
                    i++;
                    if (i < input.size()) {
                        index = input.get(i).value;
                        System.out.println("new index:" + index);
                    }
                    System.out.println("now in the top of the stack" + stack.peek());
                } else if (!top.equals(checkType(index)) && !top.equals(index)) {
                    //If at any point the parser reaches a place where the input and the stack have 2 different terminal symbols, it throws a syntax error.
                    System.out.println("Syntax Error: Unexpected token '" + index + "', in line= " + input.get(i).lineNumber);
                    break;
                }
            } else if (isNonTerminals(top)) {
                System.out.println("isNonTerminal: " + top);
                //If the top of the stack is non-terminal and the current of the stack is terminal we will go to the parsing table
                //then get the production rule as this parsingTable[getNonTerminalIndex(top)][getTerminalIndex(index)]
                if (getTerminalIndex(index) != -1) {    //the current index is terminal
                    System.out.println(parsingTable[getNonTerminalIndex(top)][getTerminalIndex(index)]);
                    //We will go to the table and get the entry
                    Set<String> parsingResultSet = parsingTable[getNonTerminalIndex(top)][getTerminalIndex(index)];
                    if (parsingResultSet != null) {
                        String parsingResult = String.join(" ", parsingResultSet);
                        System.out.println(parsingResult);
                        String[] symbols = parsingResult.split(" ");

                        if (!parsingResult.equals("ε")) {

                            //push the result to the stack
                            if (symbols.length == 1) {
                                stack.push(symbols[0]);
                                System.out.print(symbols[0]);
                            } else {
                                for (int j = symbols.length - 1; j >= 0; j--) {
                                    stack.push(symbols[j]);
                                    System.out.print(symbols[j] + " , ");
                                }
                            }
                        }
                    }else{
                        System.out.println("Syntax Error: Unexpected token '" + index + "', in line= " + input.get(i).lineNumber);
                        break;

                    }
                } else if (getTerminalIndex(index) == -1) {  //the current index is not from terminals
                    String type = checkType(index);
                    // check if the top of the stack is name, integer, or real value. The return value will be a value related to terminal list
                    //then we will go to the parsingTable and do as we did in the above.
                    getTerminalIndex(type);
                    Set<String> parsingResultSet = parsingTable[getNonTerminalIndex(top)][getTerminalIndex(type)];
                    if (parsingResultSet != null) {
                        String actions = String.join(" ", parsingResultSet);
                        String[] symbols = actions.split(" ");
                        if (!actions.equals("ε")) {
                            if (symbols.length == 1) {
                                stack.push(symbols[0]);
                                System.out.print(symbols[0]);
                            } else {
                                for (int j = symbols.length - 1; j >= 0; j--) {
                                    stack.push(symbols[j]);
                                    System.out.print(symbols[j] + " , ");
                                }
                            }
                        }
                    } else {
                        System.out.println("Syntax Error: Unexpected token '" + index + "', in line= " + input.get(i).lineNumber);
                        break;
                    }

                }
            } else if (top.equals(index)) {
                i++;
                if (i < input.size()) {
                    index = input.get(i).value;
                    System.out.println("new index:" + index);
                }

            } else {
                System.out.println("Syntax Error: Unexpected token '" + index + "', in line= " + input.get(i).lineNumber);
                break;
            }

        }

        if( stack.isEmpty() && i==input.size()) {
            System.out.println("Input is syntactically correct!");
        }

    }


    public void printTable() {
        for (int i = 0; i < parsingTable.length; i++) {
            System.out.print(parsingTable[i].toString());
            for (int j = 0; j < parsingTable[i].length; j++) {
                System.out.print(parsingTable[i][j] + " ");
            }
            System.out.println();
        }
    }


    //Check if the value is in Terminal list.
    public static boolean isInTerminals(String value) {
        for (String terminal : terminals) {
            if (terminal.equals(value)) {
                return true;
            }
        }

        return false;
    }

    //Check if the value is in non-terminal list.
    public static boolean isNonTerminals(String value) {
        for (String nonTerminal : nonTerminals) {
            if (nonTerminal.equals(value)) {
                return true;
            }
        }

        return false;
    }


    //get terminal index
    public int getTerminalIndex(String value) {
        for (int i = 0; i < terminals.length; i++) {
            if (terminals[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    //get non terminal index
    public int getNonTerminalIndex(String value) {
        for (int i = 0; i < nonTerminals.length; i++) {
            if (nonTerminals[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }


    //Check the type of the value.
    public String checkType(String value) {
        String type = "";
        Pattern namePattern = Pattern.compile("[a-zA-Z_]+[a-zA-Z0-9_]*");
        Pattern integerPattern = Pattern.compile("[0-9]+");
        Pattern realNumPattern = Pattern.compile("[0-9]+(\\.[0-9]+)?");
        if (namePattern.matcher(value).matches()) {
            type = "name";
        } else if (integerPattern.matcher(value).matches()) {
            type = "integer-value";
        } else if (realNumPattern.matcher(value).matches()) {
            type = "real-value";
        }
        return type;
    }


}

