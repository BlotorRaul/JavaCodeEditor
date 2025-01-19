package org.example.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The `ConfigLoader` class is a utility for loading configuration properties
 * from a `config.properties` file located in the classpath.
 * <p>
 * This class uses a static initializer to load the properties once during the
 * class initialization and provides a method to retrieve property values by key.
 * <p>
 * Example usage:
 * <pre>
 *     String apiKey = ConfigLoader.get("api.key");
 * </pre>
 * <p>
 * Note: Ensure the `config.properties` file is placed in the `resources` directory
 * of the project so it can be loaded from the classpath.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class ConfigLoader {

    /**
     * The `Properties` object that holds the configuration values.
     */
    private static final Properties properties = new Properties();


    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * Retrieves the value of a property by its key.
     *
     * @param key The key of the property to retrieve.
     * @return The value associated with the specified key, or `null` if the key is not found.
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }
}
