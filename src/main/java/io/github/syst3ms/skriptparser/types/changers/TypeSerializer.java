package io.github.syst3ms.skriptparser.types.changers;

import com.google.gson.JsonElement;

/**
 * An interface for serializing type objects.
 */
public interface TypeSerializer<T> {

    /**
     * Serialize a value to a GSON json element.
     * 
     * @param value the value to serialize
     * @return the classes of the objects that the implementing object can be changed to
     */
    JsonElement serialize(T value);

    /**
     * Deserialize a GSON json element to object.
     * 
     * @param element the GSON json element.
     */
    T deserialize(JsonElement element);

}
