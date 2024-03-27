package io.github.syst3ms.skriptparser.variables;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.entries.OptionLoader;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import io.github.syst3ms.skriptparser.variables.SerializedVariable.Value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A variable storage is holds the means and methods of storing variables.
 * <p>
 * This is usually some sort of database, and could be as simply as a text file.
 * 
 * Must contain a constructor of just SkriptLogger.
 */
public abstract class VariableStorage implements Closeable {

    /**
     * Whether this variable storage has been {@link #close() closed}.
     */
    protected volatile boolean closed;

    /**
     * The name of the database used in the configurations.
     */
    protected final String[] names;

    /**
     * The pattern of the variable name this storage accepts.
     * {@code null} for '{@code .*}' or '{@code .*}'.
     */
    @Nullable
    private Pattern variableNamePattern;

    private final SkriptLogger logger;
    protected final Gson gson;
    private File file;

    protected final LinkedBlockingQueue<SerializedVariable> changesQueue = new LinkedBlockingQueue<>(1000);

    /**
     * Creates a new variable storage with the given name.
     * Gson will be handled.
     *
     * @param logger the logger to print logs to.
     * @param name the name.
     */
    protected VariableStorage(SkriptLogger logger, @NotNull String... names) {
        this(logger, new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .serializeNulls()
                .setLenient()
                .create(),
        names);
    }

    /**
     * Creates a new variable storage with the given names.
     *
     * @param logger the logger to print logs to.
     * @param gson the gson that controls the serialization of the json elements.
     * @param name the name.
     */
    protected VariableStorage(SkriptLogger logger, @NotNull Gson gson, @NotNull String... names) {
        this.logger = logger;
        this.names = names;
        this.gson = gson;
        // TODO allow this runnable to be interupted.
        CompletableFuture.runAsync(() -> {
            while (!closed) {
                try {
                    SerializedVariable variable = changesQueue.take();
                    SerializedVariable.Value value = variable.value;

                    if (value != null) {
                        save(variable.name, value.type, value.data);
                    } else {
                        save(variable.name, null, null);
                    }
                } catch (InterruptedException ignored) {}
            }
        });
    }

    /**
     * Gets the string value at the given key of the given section node.
     *
     * @param section the file section.
     * @param key the key node.
     * @return the value, or {@code null} if the value was invalid,
     * or not found.
     */
    @Nullable
    protected String getConfigurationValue(FileSection section, String key) {
        return getConfigurationValue(section, key, String.class);
    }

    /**
     * Gets the value at the given key of the given section node,
     * parsed with the given class type.
     *
     * @param section the file section.
     * @param key the key node.
     * @param type the class type.
     * @return the parsed value, or {@code null} if the value was invalid,
     * or not found.
     * @param <T> the class type generic.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T getConfigurationValue(FileSection section, String key, Class<T> classType) {
        Optional<FileElement> value = section.get(key);
        if (!value.isPresent()) {
            logger.error("The configuration is missing the entry for '" + key + "' for the database '" + names[0] + "'", ErrorType.SEMANTIC_ERROR);
            return null;
        }
        String[] split = value.get().getLineContent().split(OptionLoader.OPTION_SPLIT_PATTERN);
        if (split.length < 2) {
            logger.error("The configuration entry '" + key + "' is not a option entry (key: value) for the database '" + names[0] + "'", ErrorType.SEMANTIC_ERROR);
            return null;
        }
        String content = split[1];
        if (classType.equals(String.class))
            return (T) content;

        Optional<? extends Type<T>> type = TypeManager.getByClassExact(classType);
        if (!type.isPresent()) {
            logger.error("The class type '" + classType.getName() + "' is not registered. Cannot parse node '" + key + "' for database '" + names[0] + "'", ErrorType.SEMANTIC_ERROR);
            return null;
        }

        Optional<Function<String, ? extends T>> parser = type.get().getLiteralParser();
        if (!parser.isPresent()) {
             logger.error("Type " + type.get().withIndefiniteArticle(true) + " cannot be parsed as a literal.", ErrorType.SEMANTIC_ERROR);
             return null;
        }

        T parsedValue = parser.get().apply(content);
        if (parsedValue == null) {
            logger.error("The entry for '" + key + "' in the database '" + names[0] + "' must be " +
                    type.get().withIndefiniteArticle(true), ErrorType.SEMANTIC_ERROR);
            return null;
        }
        return parsedValue;
    }

    /**
     * Loads the configuration for this variable storage
     * from the given section node.
     *
     * @param sectionNode the section node.
     * @return whether the loading succeeded.
     */
    public final boolean loadConfiguration(FileSection section) {
        String pattern = getConfigurationValue(section, "pattern");
        if (pattern == null)
            return false;

        try {
            // Set variable name pattern, see field javadoc for explanation of null value
            variableNamePattern = pattern.equals(".*") || pattern.equals(".+") ? null : Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            logger.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage(), ErrorType.SEMANTIC_ERROR);
            return false;
        }

        if (requiresFile()) {
            String fileName = getConfigurationValue(section, "file");
            if (fileName == null)
                return false;

            this.file = getFile(fileName).getAbsoluteFile();
            if (file.exists() && !file.isFile()) {
                logger.error("The database file '" + file.getName() + "' does not exist or is a directory.", ErrorType.SEMANTIC_ERROR);
                return false;
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("Cannot create the database file '" + file.getName() + "': " + e.getLocalizedMessage(), ErrorType.SEMANTIC_ERROR);
                return false;
            }

            // Check for read & write permissions to the file
            if (!file.canWrite()) {
                logger.error("Cannot write to the database file '" + file.getName() + "'!", ErrorType.SEMANTIC_ERROR);
                return false;
            }

            if (!file.canRead()) {
                logger.error("Cannot read from the database file '" + file.getName() + "'!", ErrorType.SEMANTIC_ERROR);
                return false;
            }
        }

        // Get the subclass to load it's part of the configuration section.
        return load(section);
    }

    /**
     * Loads configurations and should start loading variables too.
     *
     * @return Whether the database could be loaded successfully,
     * i.e. whether the configuration is correct and all variables could be loaded.
     */
    protected abstract boolean load(FileSection section);

    protected void loadVariable(String name, SerializedVariable variable) {
        Value value = variable.value;
        if (value == null) {
            Variables.queueVariableChange(name, null);
            return;
        }
        loadVariable(name, value.type, value.data);
    }

    /**
     * Loads a variable into Skript ram. Call this inside {@link #load(FileSection)}
     *
     * @param name the name of the variable.
     * @param type the type of the variable.
     * @param value the serialized value of the variable.
     */
    protected void loadVariable(String name, @NotNull String type, @NotNull byte[] value) {
        if (value == null || type == null)
            throw new IllegalArgumentException("value and/or typeName cannot be null");
        Variables.queueVariableChange(name, deserialize(type, value));
    }

    /**
     * Called after all storages have been loaded, and variables
     * have been redistributed if settings have changed.
     * This should commit the first transaction.
     */
    protected abstract void allLoaded();

    /**
     * Checks if this storage requires a file for storing data, like SQLite.
     *
     * @return if this storage needs a file.
     */
    protected abstract boolean requiresFile();

    /**
     * Gets the file needed for this variable storage from the given file name.
     * <p>
     * Will only be called if {@link #requiresFile()} is {@code true}.
     *
     * @param fileName the given file name.
     * @return the {@link File} object.
     */
    @Nullable
    protected abstract File getFile(String fileName);

    /**
     * Checks if this variable storage accepts the given variable name.
     *
     * @param var the variable name.
     * @return if this storage accepts the variable name.
     *
     * @see #variableNamePattern
     */
    boolean accept(@Nullable String variableName) {
        if (variableName == null)
            return false;
        return variableNamePattern == null || variableNamePattern.matcher(variableName).matches();
    }

    /**
     * Creates a {@link SerializedVariable} from the given variable name and value.
     * Can be overriden to add custom encryption in your implemented VariableStorage.
     * Call super.
     *
     * @param name the variable name.
     * @param value the variable value.
     * @return the serialized variable.
     */
    @SuppressWarnings("unchecked")
    public <T> SerializedVariable serialize(String name, @Nullable T value) {
        if (value == null)
            return new SerializedVariable(name, null);
        Type<T> type = (Type<T>) TypeManager.getByClassExact(value.getClass()).orElse(null);
        if (type == null)
            throw new UnsupportedOperationException("Class '" + value.getClass().getName() + "' cannot be serialized. No type registered.");
        TypeSerializer<T> serializer = type.getSerializer().orElse(null);
        if (serializer == null)
            throw new UnsupportedOperationException("Class '" + value.getClass().getName() + "' cannot be serialized. No type serializer.");
        JsonElement element = serializer.serialize(gson, value);
        return new SerializedVariable(name, new Value(type.getBaseName(), gson.toJson(element).getBytes()));
    }

    /**
     * Used by {@link #load(String, String, byte[]).
     * You don't need to use this method, but if you need to read the Object, this method allows for deserialization.
     * 
     * @param typeName The name of the type.
     * @param value The value that represents a object.
     * @return The Object after deserialization, not present if not possible to deserialize due to missing serializer on Type.
     */
    protected Optional<?> deserialize(@NotNull String typeName, @NotNull byte[] value) {
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

    private long lastError = Long.MIN_VALUE;
    private long ERROR_INTERVAL = 10;

    /**
     * Saves the given serialized variable.
     *
     * @param variable the serialized variable.
     */
    final void save(SerializedVariable variable) {
        if (!changesQueue.offer(variable)) {
            if (lastError < System.currentTimeMillis() - ERROR_INTERVAL * 1000) {
                // Inform console about overload of variable changes
                System.out.println("Skript cannot save any variables to the database '" + names[0] + "'. " +
                        "The thread will hang to avoid losing variable.");

                lastError = System.currentTimeMillis();
            }
            while (true) {
                try {
                    changesQueue.put(variable);
                    break;
                } catch (InterruptedException ignored) {}
            }
        }
    }

    protected void clearChangesQueue() {
        changesQueue.clear();
    }

    /**
     * Saves a variable.
     * <p>
     * {@code type} and {@code value} are <i>both</i> {@code null}
     * if this call is to delete the variable.
     *
     * @param name the name of the variable.
     * @param type the type of the variable.
     * @param value the serialized value of the variable.
     * @return Whether the variable was saved.
     */
    protected abstract boolean save(String name, @Nullable String type, @Nullable byte[] value);

}
