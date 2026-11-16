package utils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public final class FileManager {
    private FileManager() {}

    public static Path getDataDirectory() {
        return dataDirectory();
    }

    public static <T extends Serializable> void save(String fileName, List<T> records) {
        try {
            Path dataDirectory = dataDirectory();
            Files.createDirectories(dataDirectory);
            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(dataDirectory.resolve(fileName)))) {
                out.writeObject(new ArrayList<>(records));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save " + fileName + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> load(String fileName) {
        Path path = dataFile(fileName);
        if (!Files.exists(path)) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
            return (ArrayList<T>) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException("Could not load " + fileName + ": " + e.getMessage(), e);
        }
    }

    private static Path dataDirectory() {
        Path workingDirectory = Paths.get("").toAbsolutePath();
        Path workingData = workingDirectory.resolve("data");
        Path sourceData = workingDirectory.resolve("src").resolve("data");
        if (containsRecords(workingData)) return workingData;
        if (containsRecords(sourceData)) return sourceData;
        if (Files.isDirectory(workingData)) return workingData;
        if (Files.isDirectory(sourceData)) return sourceData;
        return workingData;
    }

    private static boolean containsRecords(Path directory) {
        return Files.isRegularFile(directory.resolve("students.dat"))
            || Files.isRegularFile(directory.resolve("subjects.dat"))
            || Files.isRegularFile(directory.resolve("exams.dat"))
            || Files.isRegularFile(directory.resolve("results.dat"));
    }

    private static Path dataFile(String fileName) {
        Path preferred = dataDirectory().resolve(fileName);
        if (Files.exists(preferred)) return preferred;
        Path workingDirectory = Paths.get("").toAbsolutePath();
        Path sourceData = workingDirectory.resolve("src").resolve("data").resolve(fileName);
        if (Files.exists(sourceData)) return sourceData;
        Path workingData = workingDirectory.resolve("data").resolve(fileName);
        return Files.exists(workingData) ? workingData : preferred;
    }
}
