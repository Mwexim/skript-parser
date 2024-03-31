package io.github.syst3ms.skriptparser.variables;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A VariableStorage that only saves into ram memory.
 */
public class RamStorage extends VariableStorage {

    public static final Map<String, SerializedVariable> VARIABLES = new HashMap<>();
    public static RamStorage SELF;

    public RamStorage(SkriptLogger logger) {
        super(logger, "ram");
        SELF = this;
    }

    @Override
    public void close() throws IOException { }

    @Override
    protected boolean load(FileSection section) {
        assert section.getElements().get(0).getLineContent().equals("test node: true");
        assert getConfigurationValue(section, "test node", Boolean.class) == true;
        return true;
    }

    @Override
    protected void allLoaded() {}

    @Override
    protected boolean requiresFile() {
        return false;
    }

    @Override
    @Nullable
    protected File getFile(String fileName) {
        return null;
    }

    @Override
    public Optional<?> deserialize(@NotNull String typeName, @NotNull byte[] value) {
        if (value == null || typeName == null)
            throw new IllegalArgumentException("value and/or typeName cannot be null");
        Type<?> type = TypeManager.getByExactName(typeName).orElse(null);
        if (type == null)
            throw new UnsupportedOperationException("Class '" + value.getClass().getName() + "' cannot be deserialized. No type registered.");
        TypeSerializer<?> serializer = type.getSerializer().orElse(null);
        if (serializer == null)
            throw new UnsupportedOperationException("Class '" + value.getClass().getName() + "' cannot be deserialized. No type serializer.");
        String json = new String(value);
        JsonReader reader = gson.newJsonReader(new StringReader(json));
        return Optional.ofNullable(serializer.deserialize(gson, JsonParser.parseReader(reader)));
    }

    @Override
    protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
        if (type == null || value == null) {
            VARIABLES.remove(name);
            return true;
        }
        SerializedVariable.Value v = new SerializedVariable.Value(type, value);
        VARIABLES.put(name, new SerializedVariable(name, v));
        return true;
    }

}
