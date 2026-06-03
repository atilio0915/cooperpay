package cooperpay.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppDataDirectories {

    private AppDataDirectories() {
    }

    public static void ensureAppDataDirectories() {
        Path baseDir = resolveBaseDir();
        try {
            Files.createDirectories(baseDir.resolve("data"));
            Files.createDirectories(baseDir.resolve("logs"));
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel criar pastas de dados/logs em: " + baseDir, ex);
        }
    }

    private static Path resolveBaseDir() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, "CooperPay");
        }

        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome, ".cooperpay", "CooperPay");
    }
}
