package io.github.bensku.skript;

import java.util.ArrayList;
import java.util.List;

import io.github.bensku.skript.pattern.ChoiceElement;
import io.github.bensku.skript.pattern.MultiElement;
import io.github.bensku.skript.pattern.PatternElement;

/**
 * Parses Skript's special patters into pattern elements.
 *
 */
public class PatternParser {
    
    public static List<PatternElement> parse(String pattern) {
        List<PatternElement> elements = new ArrayList<>();
        
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            // Check for special characters
            int notClosed = 1;
            boolean malformed = true; // If pattern has an error
            String error = "";
            
            switch (c) {
            case '(': // Choice element
                for (int j = i; j < chars.length; j++) {
                    int c2 = chars[j];
                    if (c2 == '(') {
                        notClosed++;
                    } else if (c2 == ')') {
                        notClosed--;
                    }
                    
                    if (notClosed == 0) {
                        malformed = false;
                        elements.add(parseChoice(pattern.substring(i + 1, j)));
                        break;
                    }
                }
                
                if (malformed) {
                    error = "Invalid choice element starting at " + i;
                }
                break;
            case '[': // Optional element
                for (int j = i; j < chars.length; j++) {
                    int c2 = chars[j];
                    if (c2 == '[') {
                        notClosed++;
                    } else if (c2 == ']') {
                        notClosed--;
                    }
                    
                    if (notClosed == 0) {
                        malformed = false;
                        elements.addAll(parse(pattern.substring(i + 1, j)));
                        break;
                    }
                }
                
                if (malformed) {
                    error = "Invalid optional element starting at " + i;
                }
                break;
            case '%': // Variable element
                break;
            }
        }
        
        return elements;
    }
    
    public static PatternElement parseChoice(String pattern) {
        // TODO: choices inside choices?
        String[] parts = pattern.split(" ");
        List<PatternElement> choices = new ArrayList<>();
        for (String choice : parts) {
            choices.add(new MultiElement(parse(choice)));
        }
        
        return new ChoiceElement(choices);
    }
}
