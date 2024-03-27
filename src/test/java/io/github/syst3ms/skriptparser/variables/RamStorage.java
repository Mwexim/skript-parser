package io.github.syst3ms.skriptparser.variables;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RamStorage extends VariableStorage {

    public static final Map<String, SerializedVariable> VARIABLES = new HashMap<>();

    public RamStorage(SkriptLogger logger) {
        super(logger, "ram");
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
