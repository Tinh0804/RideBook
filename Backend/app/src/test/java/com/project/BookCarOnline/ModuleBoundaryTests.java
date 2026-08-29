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

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void paymentProviderAdaptersBelongToFinanceModule() {
        Path current = Path.of("").toAbsolutePath();
        Path backend = Files.isDirectory(current.resolve("app")) ? current : current.getParent();
        Path financeSource = backend.resolve("modules/finance/src/main/java/com/project/BookCarOnline/finance");
        Path appSource = backend.resolve("app/src/main/java/com/project/BookCarOnline/app");

        assertTrue(Files.isRegularFile(financeSource.resolve("config/MoMoConfig.java")));
        assertTrue(Files.isRegularFile(financeSource.resolve("config/VNPayConfig.java")));
        assertTrue(Files.isRegularFile(financeSource.resolve("service/payment/MoMoService.java")));
        assertTrue(Files.isRegularFile(financeSource.resolve("service/payment/VNPayService.java")));

        assertFalse(Files.exists(appSource.resolve("config/MoMoConfig.java")));
        assertFalse(Files.exists(appSource.resolve("config/VNPayConfig.java")));
        assertFalse(Files.exists(appSource.resolve("service/payment/MoMoService.java")));
        assertFalse(Files.exists(appSource.resolve("service/payment/VNPayService.java")));
    }

    @Test
    void appDriverServiceOnlyCoordinatesCrossModuleWorkflows() throws IOException {
        Path current = Path.of("").toAbsolutePath();
        Path backend = Files.isDirectory(current.resolve("app")) ? current : current.getParent();
        String source = Files.readString(backend.resolve(
                "app/src/main/java/com/project/BookCarOnline/app/service/DriverService.java"));

        List<String> identityOnlyMethods = List.of(
                "getMyInfo(",
                "searchDrivers(",
                "getAllActiveDrivers(",
                "createDriver(",
                "updateDriver(",
                "deleteDriver(",
                "getDriverById(",
                "getDriversByArea(",
                "getDriversByVehicleType(",
                "toggleDriverAccountStatus(",
                "changePasswordByAdmin(");

        identityOnlyMethods.forEach(method -> assertFalse(
                source.contains(method),
                "DriverService must not own identity-only operation " + method));
    }
}
