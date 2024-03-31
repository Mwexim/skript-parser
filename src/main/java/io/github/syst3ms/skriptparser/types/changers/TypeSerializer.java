package io.github.syst3ms.skriptparser.types.changers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * An interface for serializing type objects.
 */
public interface TypeSerializer<T> {

    /**
     * Serialize a value to a GSON json element.
     * The Gson object is preserved from the VariableStorage.
     * 
     * @param gson Gson context.
     * @param value the value to serialize
     * @return the classes of the objects that the implementing object can be changed to
     */
    JsonElement serialize(Gson gson, T value);

    /**
     * Deserialize a GSON json element to object.
     * The Gson object is preserved from the VariableStorage.
     * 
     * @param gson Gson context.
     * @param element the GSON json element.
     */
    T deserialize(Gson gson, JsonElement element);

}
