package com.project.BookCarOnline;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleBoundaryTests {

    private static final List<String> MODULES = List.of(
            "catalog", "identity", "promotion", "finance", "booking", "communication", "app");
    private static final Pattern INTERNAL_IMPORT = Pattern.compile(
            "import com\\.project\\.BookCarOnline\\.([^.]+)\\.(repository|entity)\\.([^;]+);");
    private static final Pattern MODULE_IMPORT = Pattern.compile(
            "import com\\.project\\.BookCarOnline\\.([^.]+)\\.");
    private static final Map<String, Set<String>> ALLOWED_DEPENDENCIES = Map.of(
            "catalog", Set.of("catalog", "shared"),
            "identity", Set.of("identity", "catalog", "shared"),
            "promotion", Set.of("promotion", "identity", "shared"),
            "finance", Set.of("finance", "identity", "shared"),
            "booking", Set.of("booking", "catalog", "identity", "promotion", "finance", "shared"),
            "communication", Set.of("communication", "identity", "shared"),
            "app", Set.of("app", "catalog", "identity", "promotion", "finance", "booking", "communication", "shared"));

    @Test
    void modulesDoNotUseOtherModulesRepositoriesOrEntities() throws IOException {
        Path current = Path.of("").toAbsolutePath();
        Path backend = Files.isDirectory(current.resolve("app")) ? current : current.getParent();
        var violations = new ArrayList<String>();

        for (String module : MODULES) {
            Path moduleRoot = "app".equals(module)
                    ? backend.resolve(module)
                    : backend.resolve("modules").resolve(module);
            Path source = moduleRoot.resolve("src/main/java");
            try (var files = Files.walk(source)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    try {
                        var matcher = INTERNAL_IMPORT.matcher(Files.readString(path));
                        while (matcher.find()) {
                            boolean ownModule = matcher.group(1).equals(module);
                            boolean publicEnum = matcher.group(2).equals("entity")
                                    && matcher.group(3).startsWith("enums.");
                            if (!ownModule && !publicEnum) {
                                violations.add(backend.relativize(path) + " -> " + matcher.group());
                            }
                        }
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void modulesFollowDeclaredDependencyDirection() throws IOException {
        Path current = Path.of("").toAbsolutePath();
        Path backend = Files.isDirectory(current.resolve("app")) ? current : current.getParent();
        var violations = new ArrayList<String>();

        for (String module : MODULES) {
            Path moduleRoot = "app".equals(module)
                    ? backend.resolve(module)
                    : backend.resolve("modules").resolve(module);
            try (var files = Files.walk(moduleRoot.resolve("src/main/java"))) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    try {
                        var matcher = MODULE_IMPORT.matcher(Files.readString(path));
                        while (matcher.find()) {
                            String dependency = matcher.group(1);
                            if (!ALLOWED_DEPENDENCIES.get(module).contains(dependency)) {
                                violations.add(backend.relativize(path) + " -> " + dependency);
                            }
                        }
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }
}
