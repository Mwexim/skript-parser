package io.github.bensku.skript.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all different types.
 *
 */
public class TypeManager {
    
    private Map<String,Type<?>> nameToType;
    
    public TypeManager() {
        this.nameToType = new HashMap<>();
    }
    
    public Type<?> getForName(String name) {
        return nameToType.get(name);
    }
    
    public Type<?> convertPlural(Type<?> singular) {
        // TODO grammar -es or other languages...
        return new Type<>(singular.getC(), singular.getName() + "s", false);
    }
}
