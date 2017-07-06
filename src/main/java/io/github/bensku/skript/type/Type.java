package io.github.bensku.skript.type;

/**
 * Represents a type.
 *
 */
public class Type<T> {

    private Class<T> c;
    private String name;
    
    private boolean single;
    
    public Type(Class<T> c, String name, boolean single) {
        this.c = c;
        this.name = name;
        this.single = single;
    }
    
    public Class<T> getC() {
        return c;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isSingle() {
        return single;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
