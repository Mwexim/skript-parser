package io.github.bensku.skript.type;

import java.util.HashMap;
import java.util.Map;

public class TypeManager {
    
    private Map<String,Type<?>> nameToType;
    
    public TypeManager() {
        this.nameToType = new HashMap<>();
    }
    
    public Type<?> getForName(String name) {
        return nameToType.get(name);
    }
}
