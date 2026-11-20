package dev.rinchan.sharedlootr;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Automated architecture hard guardrails scanning real compiled JAR artifacts and source trees:
 * 1. Dedicated Server isolation redline: zero client/GUI references in any non-client package class constant pool.
 * 2. Mixin bidirectional closure: every *.mixins.json entry exists in the JAR, and every @Mixin source class is registered.
 * 3. Pure artifact redline: zero rinlib or com/terraformersmc/modmenu stub leaks in packaged JARs.
 */
class ArchitectureContractTest {
    private static final List<String> FORBIDDEN_SERVER_CP_PREFIXES = List.of(
            "net/minecraft/client/",
            "net/neoforged/neoforge/client/",
            "net/minecraftforge/client/",
            "com/terraformersmc/modmenu/",
            "net/fabricmc/api/ClientModInitializer"
    );

    private static final List<String> REQUIRED_LANG_FILES = List.of(
            "assets/shared_lootr/lang/en_us.json",
            "assets/shared_lootr/lang/zh_cn.json",
            "assets/shared_lootr/lang/zh_tw.json",
            "assets/shared_lootr/lang/ja_jp.json"
    );

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;");
    private static final Pattern MIXIN_ANNOTATION_PATTERN = Pattern.compile("@Mixin\\b");

    @Test
    void dedicatedServerClassesHaveZeroClientOrGuiReferencesInConstantPool() throws IOException {
        List<Path> jars = resolveTargetJars();
        assertFalse(jars.isEmpty(), "At least one built mod JAR must be audited");

        List<String> violations = new ArrayList<>();
        for (Path jarPath : jars) {
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                var entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.endsWith(".class") || isClientPackageClass(name)) {
                        continue;
                    }
                    byte[] bytecode = jar.getInputStream(entry).readAllBytes();
                    List<String> utf8Constants = readConstantPoolUtf8Entries(bytecode);
                    for (String constant : utf8Constants) {
                        for (String forbidden : FORBIDDEN_SERVER_CP_PREFIXES) {
                            if (constant.contains(forbidden)) {
                                violations.add(jarPath.getFileName() + " -> " + name + " references " + constant);
                            }
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Dedicated Server isolation redline violated:\n" + String.join("\n", violations));
    }

    @Test
    void mixinDeclarationsAndSourceAnnotationsFormBidirectionalClosure() throws IOException {
        // 1. Forward check: every *.mixins.json entry in every built JAR must exist as a .class in that JAR
        List<Path> jars = resolveTargetJars();
        assertFalse(jars.isEmpty(), "At least one built mod JAR must be audited");

        List<String> missingInJar = new ArrayList<>();
        for (Path jarPath : jars) {
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                Set<String> allEntries = new HashSet<>();
                List<JarEntry> mixinJsons = new ArrayList<>();
                var entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    allEntries.add(entry.getName());
                    if (entry.getName().endsWith(".mixins.json")) {
                        mixinJsons.add(entry);
                    }
                }
                assertFalse(mixinJsons.isEmpty(), jarPath.getFileName() + " must contain a *.mixins.json");

                for (JarEntry jsonEntry : mixinJsons) {
                    String raw = new String(jar.getInputStream(jsonEntry).readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                    String pkg = obj.get("package").getAsString().replace('.', '/');

                    for (String section : List.of("mixins", "client", "server")) {
                        JsonArray arr = obj.getAsJsonArray(section);
                        if (arr == null) {
                            continue;
                        }
                        for (JsonElement elem : arr) {
                            String relClass = elem.getAsString().replace('.', '/');
                            String classPath = pkg + "/" + relClass + ".class";
                            if (!allEntries.contains(classPath)) {
                                missingInJar.add(jarPath.getFileName() + " (" + jsonEntry.getName() + "): missing " + classPath);
                            }
                            if ("mixins".equals(section) && classPath.contains("/client/")) {
                                missingInJar.add(jarPath.getFileName() + " (" + jsonEntry.getName() + "): client mixin in common section: " + relClass);
                            }
                        }
                    }
                }
            }
        }
        assertTrue(missingInJar.isEmpty(),
                "Mixin forward closure failed (JSON -> JAR):\n" + String.join("\n", missingInJar));

        // 2. Reverse check: every @Mixin class in source must be registered in its corresponding *.mixins.json
        Path root = resolveProjectRoot();
        Map<Path, Path> sourceTreeToMixinJson = Map.of(
                root.resolve("common/src/v121/java"), root.resolve("common/src/v121/resources/shared_lootr.mixins.json"),
                root.resolve("common/src/v26/java"), root.resolve("common/src/v26/resources/shared_lootr.mixins.json"),
                root.resolve("fabric/src/v120/java"), root.resolve("fabric/src/v120/resources/shared_lootr.mixins.json"),
                root.resolve("forge/src/main/java"), root.resolve("forge/src/main/resources/shared_lootr.mixins.json")
        );

        Map<Path, Set<String>> registeredAllByJson = new HashMap<>();
        Map<Path, Set<String>> registeredClientByJson = new HashMap<>();
        for (Path jsonPath : new LinkedHashSet<>(sourceTreeToMixinJson.values())) {
            assertTrue(Files.exists(jsonPath), "Expected mixin json at " + jsonPath);
            JsonObject obj = JsonParser.parseString(Files.readString(jsonPath, StandardCharsets.UTF_8)).getAsJsonObject();
            String pkg = obj.get("package").getAsString();
            Set<String> allSet = new HashSet<>();
            Set<String> clientSet = new HashSet<>();
            for (String section : List.of("mixins", "client", "server")) {
                JsonArray arr = obj.getAsJsonArray(section);
                if (arr == null) {
                    continue;
                }
                for (JsonElement elem : arr) {
                    String fqcn = pkg + "." + elem.getAsString();
                    allSet.add(fqcn);
                    if ("client".equals(section)) {
                        clientSet.add(fqcn);
                    }
                }
            }
            registeredAllByJson.put(jsonPath, allSet);
            registeredClientByJson.put(jsonPath, clientSet);
        }

        List<String> unregisteredMixins = new ArrayList<>();
        int totalSourceMixinsFound = 0;
        for (String moduleDir : List.of("common", "fabric", "forge", "neoforge")) {
            Path srcRoot = root.resolve(moduleDir).resolve("src");
            if (!Files.isDirectory(srcRoot)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(srcRoot)) {
                List<Path> javaFiles = walk
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !p.toString().contains("/src/test/") && !p.toString().contains("/src/e2e/"))
                        .toList();
                for (Path javaFile : javaFiles) {
                    String content = Files.readString(javaFile, StandardCharsets.UTF_8);
                    if (!MIXIN_ANNOTATION_PATTERN.matcher(content).find()) {
                        continue;
                    }
                    totalSourceMixinsFound++;
                    Matcher m = PACKAGE_PATTERN.matcher(content);
                    assertTrue(m.find(), "Missing package declaration in " + javaFile);
                    String pkg = m.group(1);
                    String simpleName = javaFile.getFileName().toString().replace(".java", "");
                    String fqcn = pkg + "." + simpleName;

                    Path matchedJson = sourceTreeToMixinJson.entrySet().stream()
                            .filter(e -> javaFile.startsWith(e.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(null);

                    if (matchedJson == null) {
                        unregisteredMixins.add(fqcn + " (no matching shared_lootr.mixins.json for " + javaFile + ")");
                        continue;
                    }
                    if (!registeredAllByJson.get(matchedJson).contains(fqcn)) {
                        unregisteredMixins.add(fqcn + " not registered in " + root.relativize(matchedJson));
                    } else if (pkg.contains(".client") && !registeredClientByJson.get(matchedJson).contains(fqcn)) {
                        unregisteredMixins.add(fqcn + " is in a client package but not in \"client\" array of " + root.relativize(matchedJson));
                    }
                }
            }
        }

        assertTrue(totalSourceMixinsFound > 0, "Expected to discover @Mixin classes in repository sources");
        assertTrue(unregisteredMixins.isEmpty(),
                "Mixin reverse closure failed (Source -> JSON):\n" + String.join("\n", unregisteredMixins));
    }

    @Test
    void packagedJarsContainZeroRinlibOrModMenuStubLeaksAndHaveRequiredMetadata() throws IOException {
        List<Path> jars = resolveTargetJars();
        assertFalse(jars.isEmpty(), "At least one built mod JAR must be audited");

        List<String> violations = new ArrayList<>();
        for (Path jarPath : jars) {
            Set<String> names = new HashSet<>();
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                var entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    names.add(name);
                    String lower = name.toLowerCase(Locale.ROOT);
                    if (lower.contains("rinlib")) {
                        violations.add(jarPath.getFileName() + " leaked rinlib entry: " + name);
                    }
                    if (name.contains("com/terraformersmc/modmenu")) {
                        violations.add(jarPath.getFileName() + " leaked ModMenu compile-time stub: " + name);
                    }
                }
            }

            if (!names.contains("icon.png")) {
                violations.add(jarPath.getFileName() + " missing icon.png");
            }
            if (!names.contains("shared_lootr.mixins.json")) {
                violations.add(jarPath.getFileName() + " missing shared_lootr.mixins.json");
            }
            for (String lang : REQUIRED_LANG_FILES) {
                if (!names.contains(lang)) {
                    violations.add(jarPath.getFileName() + " missing " + lang);
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Pure artifact redline violated:\n" + String.join("\n", violations));
    }

    private static boolean isClientPackageClass(String internalEntryName) {
        return internalEntryName.contains("/client/");
    }

    private static List<Path> resolveTargetJars() throws IOException {
        Set<Path> jars = new LinkedHashSet<>();
        String primaryProp = System.getProperty("sharedlootr.verify.jar");
        if (primaryProp != null && !primaryProp.isBlank()) {
            Path primary = Path.of(primaryProp).toAbsolutePath().normalize();
            assertTrue(Files.isRegularFile(primary), "Configured verify JAR does not exist: " + primary);
            jars.add(primary);
        }

        Path distDir = resolveProjectRoot().resolve("build/dist");
        if (Files.isDirectory(distDir)) {
            try (Stream<Path> stream = Files.list(distDir)) {
                stream.filter(p -> p.toString().endsWith(".jar"))
                        .sorted()
                        .forEach(p -> jars.add(p.toAbsolutePath().normalize()));
            }
        }
        return new ArrayList<>(jars);
    }

    private static Path resolveProjectRoot() {
        String prop = System.getProperty("sharedlootr.project.root");
        if (prop != null && !prop.isBlank()) {
            Path root = Path.of(prop).toAbsolutePath().normalize();
            assertTrue(Files.isDirectory(root), "Configured project root does not exist: " + root);
            return root;
        }
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.exists(current.resolve("profiles.json"))) {
            current = current.getParent();
        }
        assertNotNull(current, "Could not locate repository root containing profiles.json");
        return current;
    }

    /**
     * Parses a JVM .class file constant pool (JVMS §4.4) and returns all CONSTANT_Utf8 strings.
     */
    private static List<String> readConstantPoolUtf8Entries(byte[] bytecode) throws IOException {
        List<String> utf8s = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytecode))) {
            int magic = in.readInt();
            if (magic != 0xCAFEBABE) {
                throw new IOException("Invalid class magic: 0x" + Integer.toHexString(magic));
            }
            in.readUnsignedShort(); // minor_version
            in.readUnsignedShort(); // major_version
            int cpCount = in.readUnsignedShort();
            for (int i = 1; i < cpCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1 -> utf8s.add(in.readUTF()); // CONSTANT_Utf8
                    case 3, 4 -> in.readInt(); // Integer, Float
                    case 5, 6 -> {
                        in.readLong(); // Long, Double occupy 2 constant pool slots
                        i++;
                    }
                    case 7, 8, 16, 19, 20 -> in.readUnsignedShort(); // Class, String, MethodType, Module, Package
                    case 9, 10, 11, 12, 17, 18 -> {
                        in.readUnsignedShort();
                        in.readUnsignedShort();
                    }
                    case 15 -> { // MethodHandle
                        in.readUnsignedByte();
                        in.readUnsignedShort();
                    }
                    default -> throw new IOException("Unsupported JVM constant pool tag: " + tag);
                }
            }
        }
        return utf8s;
    }
}
