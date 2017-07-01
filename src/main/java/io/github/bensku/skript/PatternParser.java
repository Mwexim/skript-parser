package io.github.bensku.skript;

import java.util.ArrayList;
import java.util.List;

import io.github.bensku.skript.pattern.PatternElement;

/**
 * Parses Skript's special patters into pattern elements.
 *
 */
public class PatternParser {
    
    public static PatternElement parse(String pattern) {
        List<PatternElement> elements = new ArrayList<>();
        
        int pos = 0;
        while (true) {
            // TODO do this and avoid INSANE recursion
        }
    }
}
