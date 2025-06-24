package ws.siri.jscore.behaviour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import net.fabricmc.loader.api.FabricLoader;
import ws.siri.jscore.Core;

public class Snapshot {
    private static HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static String snap() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedTime = now.format(formatter);

        snap(formattedTime);

        return formattedTime + ".zip";
    }

    public static boolean snap(String name) {
        Path source = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID);
        Path target = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-snapshots")
                .resolve(name + ".zip");

        setupDir();

        boolean overwrite = Files.exists(target);

        try {
            if (overwrite) {
                FileUtils.deleteQuietly(target.toFile());
            }

            FileOutputStream fos = new FileOutputStream(target.toFile());
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            File fileToZip = source.toFile();
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Error creating snapshot: " + e);
        }

        return overwrite;
    }

    public static boolean load(String fileName) {
        Path snapshotPath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-snapshots")
                .resolve(fileName);
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID);

        if (!Files.exists(snapshotPath)) {
            return false;
        }

        if (Files.isDirectory(snapshotPath)) {
            try {
                if (Files.exists(configPath))
                    FileUtils.deleteQuietly(configPath.toFile());
                Files.move(snapshotPath, configPath);

                return true;
            } catch (Exception e) {
                throw new RuntimeException("Error when loading snapshot " + fileName + ": " + e);
            }

        }

        Path tempConfigPath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-restoring");

        try {
            if (Files.exists(tempConfigPath))
                FileUtils.deleteQuietly(tempConfigPath.toFile());

            Files.createDirectories(tempConfigPath);
            InputStream is = new FileInputStream(snapshotPath.toFile());

            unzip(is, tempConfigPath);

            if (Files.exists(configPath))
                FileUtils.deleteQuietly(configPath.toFile());

            if (Files.list(tempConfigPath).count() == 1 && Files.exists(tempConfigPath.resolve("jscore"))) {
                Files.move(tempConfigPath.resolve("jscore"), configPath);
            } else {
                Files.move(tempConfigPath, configPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error when loading snapshot " + fileName + ": " + e);
        } finally {
            if (Files.exists(tempConfigPath))
                FileUtils.deleteQuietly(tempConfigPath.toFile());
        }

        return true;
    }

    private static void unzip(InputStream is, Path targetDir) throws IOException {
        targetDir = targetDir.toAbsolutePath();
        try (ZipInputStream zipIn = new ZipInputStream(is)) {
            for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null;) {
                Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
                if (!resolvedPath.startsWith(targetDir)) {
                    // see: https://snyk.io/research/zip-slip-vulnerability
                    throw new RuntimeException("Entry with an illegal path: "
                            + ze.getName());
                }
                if (ze.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zipIn, resolvedPath);
                }
            }
        }
    }

    public static List<String> list() {
        try {
            setupDir();
            return Files.list(FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-snapshots"))
                    .map((path) -> path.getFileName().toString()).toList();
        } catch (IOException e) {
            throw new RuntimeException("Error listings snapshots: " + e);
        }
    }

    private static void setupDir() {
        FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-snapshots").toFile().mkdirs();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public static boolean delete(String name) {
        Path filePath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-snapshots").resolve(name);

        if(Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (Exception e) {
                throw new RuntimeException("Error when deleting file " + name + ": " + e);
            }

            return true;
        } else {
            return false;
        }
    }

    public static void pull(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID);
        Path tempConfigPath = FabricLoader.getInstance().getConfigDir().resolve(Core.MOD_ID + "-pulling");

        try {
            if (Files.exists(configPath))
                FileUtils.deleteQuietly(configPath.toFile());
            InputStream is = client.send(request, BodyHandlers.ofInputStream()).body();

            if (Files.exists(tempConfigPath))
                FileUtils.deleteQuietly(tempConfigPath.toFile());

            Files.createDirectories(tempConfigPath);
            unzip(is, tempConfigPath);

            if (Files.exists(configPath))
                FileUtils.deleteQuietly(configPath.toFile());

            if (Files.list(tempConfigPath).count() == 1 && Files.exists(tempConfigPath.resolve("jscore"))) {
                Files.move(tempConfigPath.resolve("jscore"), configPath);
            } else {
                Files.move(tempConfigPath, configPath);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error when pulling snapshot: " + e);
        } finally {
            if (Files.exists(tempConfigPath))
                FileUtils.deleteQuietly(tempConfigPath.toFile());
        }
    }
}
