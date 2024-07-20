package com.example.compilerfinalproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Scanner {


    //The Scanner (lexical analyzer), it's the first phase of the compiler process,
    // it converts the source code into a sequence of tokens.
     File file;
    Scanner(File file)  {
        this.file=file;
        Tokenizer();   //Call the method to generate tokens.
    }


    //List that contains reserved words.
    private static final List<String> RESERVED_WORDS = Arrays.asList(
            "module", "begin", "end", "const", "integer", "real", "char", "procedure", "mod", "div", "readint", " readreal",
            "readchar", "readln", "writeint", "writereal", "writechar", "writeln", "then", "end", "else", "while", "do",
            "loop", "until", "exit", "call"
    );


    public List<Token> Tokenizer() {
        List<Token> tokens = new ArrayList<>();
        StringBuilder content = new StringBuilder();

        //Read the file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String codeTxt= content.toString();
        String[] lines = codeTxt.split("\\r?\\n");
        int lineNumber=0;
        //in each line we will read character by character
        for (String line : lines) {
            lineNumber++;
            StringBuilder currentToken = new StringBuilder();  //store the character in string builder

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (Character.isWhitespace(c)) { //if a space occurs then add the current token to the list of tokens.
                    addToken(currentToken, tokens,lineNumber-1);
                } else if (Character.isLetter(c) || c == '_') {  //if a character occurs then append it to the current token.
                        currentToken.append(c);
                }
                else if (Character.isDigit(c) || (c == '.' && i < line.length() - 2 && Character.isDigit(line.charAt(i + 1)))) {
                    //if its real number
                    currentToken.append(c);
                } else if (c == '*' || c == '/' || c == '+' || c == '-' ){
                    if (!currentToken.isEmpty()) { //add the current token to the list
                        tokens.add(new Token(currentToken.toString(),lineNumber-1));
                        currentToken.setLength(0);
                    }
                    currentToken.append(c);   //add the operation as separate token
                    tokens.add(new Token(String.valueOf(c), lineNumber-1));
                    currentToken.setLength(0);
                }
                   else if( c == '<' || c == '>' || c == '|' || c==':') {
                    if (!currentToken.isEmpty()) { //add the current token to the list
                        tokens.add(new Token(currentToken.toString(),lineNumber-1));
                        currentToken.setLength(0);
                    }
                    currentToken.append(c); //append the character
                      if(i + 1 < line.length() && line.charAt(i + 1) == '='){        //if there's >= or <= or != or :=
                           currentToken.append(line.charAt(i + 1) );  //add the current token to the list
                           tokens.add(new Token(String.valueOf(c) + line.charAt(++i),  lineNumber-1));
                           currentToken.setLength(0);
                       }
                       else{  //if it's  > or < or ! or : add them as separate token
                           tokens.add(new Token(String.valueOf(c) ,  lineNumber-1));  //append the character
                           currentToken.setLength(0);
                       }

                } else {
                    addToken(currentToken, tokens,lineNumber-1);
                    tokens.add(new Token(String.valueOf(c),lineNumber-1));
                    currentToken.setLength(0);
                }
            }
            addToken(currentToken, tokens,lineNumber-1);

        }

        return tokens;
    }


    public void addToken(StringBuilder current, List<Token> tokens,int lineNumber) {

        //method to add token to the array list tokens

        if (!current.isEmpty()) {
            if(RESERVED_WORDS.contains(current.toString())){
            tokens.add(new Token(current.toString(),lineNumber));
                current.setLength(0);}

            else if (current.toString().matches("-?\\d+(\\.\\d+)?")){
                tokens.add(new Token(current.toString(),lineNumber));
                current.setLength(0);
            }
            else{
                tokens.add(new Token(current.toString(),lineNumber));
                current.setLength(0);
            }
        }
    }
}


