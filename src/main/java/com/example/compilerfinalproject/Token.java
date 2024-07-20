package com.example.compilerfinalproject;

public class Token{
    
    String value;
    int lineNumber;

    public Token(String value,int lineNumber) {
        this.value = value;

        this.lineNumber=lineNumber;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
