package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppSettings {
    private static final String FILE_NAME = "app-settings.properties";
    private static final String ADMIN_NAME_KEY = "admin.name";
    private static final String DEFAULT_ADMIN_NAME = "Administrator";

    private AppSettings() {}

    public static String loadAdminName() {
        Path settingsFile = FileManager.getDataDirectory().resolve(FILE_NAME);
        if (!Files.exists(settingsFile)) return DEFAULT_ADMIN_NAME;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(settingsFile)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load application settings: " + ex.getMessage(), ex);
        }
        String name = properties.getProperty(ADMIN_NAME_KEY, DEFAULT_ADMIN_NAME).trim();
        return name.isEmpty() ? DEFAULT_ADMIN_NAME : name;
    }

    public static void saveAdminName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Administrator name is required.");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > 60) {
            throw new IllegalArgumentException("Administrator name must be 60 characters or fewer.");
        }

        Properties properties = new Properties();
        properties.setProperty(ADMIN_NAME_KEY, normalizedName);
        Path settingsFile = FileManager.getDataDirectory().resolve(FILE_NAME);
        try {
            Files.createDirectories(settingsFile.getParent());
            try (OutputStream output = Files.newOutputStream(settingsFile)) {
                properties.store(output, "Exam Management System settings");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save application settings: " + ex.getMessage(), ex);
        }
    }
}
