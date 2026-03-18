/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

def prop = { String name, String fallback ->
    if (!binding.hasVariable(name)) {
        return fallback
    }

    Object raw = binding.getVariable(name)
    if (raw == null) {
        return fallback
    }

    String text = raw.toString().trim()
    return text.isEmpty() ? fallback : text
}

boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
Path moduleDir = Paths.get(project.basedir.toString()).toAbsolutePath().normalize()
Path outerProjectRoot = moduleDir.resolve("..").resolve("..").normalize()

String securityTckVersion = prop("securityTckVersion", "4.0.0")
String securityTckUrl = prop("securityTckUrl", "https://download.eclipse.org/jakartaee/security/4.0/jakarta-security-tck-${securityTckVersion}.zip")
Path securityTckWorkDir = Paths.get(prop("securityTckWorkDir", moduleDir.resolve("target").toString())).toAbsolutePath().normalize()
String securityTckModules = prop("securityTckModules", "!old-tck,!old-tck/build,!old-tck/run")
String securityTckClassifier = prop("securityTckClassifier", "plus")
int preferredTomeeHttpPort = Integer.parseInt(prop("securityTckTomeeHttpPort", "8080"))
String securityTckTomeeVerbose = prop("securityTckTomeeVerbose", "true")
String securityTckExtraMavenArgs = prop("securityTckExtraMavenArgs", "")
String securityTckMavenCommand = prop("securityTckMavenCommand", windows ? "mvn" : "mvn")
String tomeeVersion = prop("tomeeVersion", "11.0.0-SNAPSHOT")
String sigtestApiGroupId = prop("sigtestApiGroupId", "jakarta.security.enterprise")
String sigtestApiArtifactId = prop("sigtestApiArtifactId", "jakarta.security.enterprise-api")
String sigtestApiVersion = prop("sigtestApiVersion", securityTckVersion)

Path securityTckArchive = securityTckWorkDir.resolve("jakarta-security-tck-${securityTckVersion}.zip")
Path securityTckDir = securityTckWorkDir.resolve("security-tck-${securityTckVersion}")
Path extractedTckDir = securityTckDir.resolve("tck")
Path truststore = extractedTckDir.resolve("target").resolve("glassfish8").resolve("glassfish").resolve("domains").resolve("domain1").resolve("config").resolve("cacerts.jks")

def deleteRecursively = { Path path ->
    if (path == null || !Files.exists(path)) {
        return
    }

    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.deleteIfExists(file)
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc
            }
            Files.deleteIfExists(dir)
            return FileVisitResult.CONTINUE
        }
    })
}

def runCommand = { List<String> command, Path workingDirectory, Map<String, String> environment = [:], Long timeoutSeconds = null, boolean ignoreFailure = false ->
    List<String> commandLine = command.collect { it.toString() }

    ProcessBuilder builder = new ProcessBuilder(commandLine)
    builder.directory(workingDirectory.toFile())
    builder.inheritIO()
    environment.each { key, value ->
        builder.environment().put(key.toString(), value.toString())
    }

    Process process
    try {
        process = builder.start()
    } catch (IOException ioe) {
        if (ignoreFailure) {
            return -1
        }
        throw ioe
    }

    boolean finished
    if (timeoutSeconds == null) {
        process.waitFor()
        finished = true
    } else {
        finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
    }

    if (!finished) {
        process.destroy()
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            process.waitFor(5, TimeUnit.SECONDS)
        }

        if (ignoreFailure) {
            return -1
        }
        throw new IllegalStateException("Timed out waiting for command: ${commandLine.join(' ')}")
    }

    int exitCode = process.exitValue()
    if (exitCode != 0 && !ignoreFailure) {
        throw new IllegalStateException("Command failed (${exitCode}): ${commandLine.join(' ')}")
    }

    return exitCode
}

def terminateProcess = { ProcessHandle process ->
    if (process == null || process.pid() == ProcessHandle.current().pid()) {
        return
    }

    try {
        if (!process.isAlive()) {
            return
        }

        process.destroy()
        try {
            process.onExit().get(2, TimeUnit.SECONDS)
        } catch (Exception ignored) {
        }

        if (process.isAlive()) {
            process.destroyForcibly()
            try {
                process.onExit().get(2, TimeUnit.SECONDS)
            } catch (Exception ignored) {
            }
        }
    } catch (Exception ignored) {
    }
}

def stopOpenidProvider = { Path tckDir ->
    if (tckDir == null || !Files.exists(tckDir)) {
        return
    }

    Path targetDir = tckDir.resolve("target")
    if (Files.exists(targetDir)) {
        Files.newDirectoryStream(targetDir, "apache-tomcat-*").withCloseable { directoryStream ->
            directoryStream.each { Path tomcatDir ->
                Path pidFile = tomcatDir.resolve("pidfile")
                Path shutdownSh = tomcatDir.resolve("bin").resolve("shutdown.sh")
                Path shutdownBat = tomcatDir.resolve("bin").resolve("shutdown.bat")

                List<String> shutdownCommand = null
                if (windows && Files.exists(shutdownBat)) {
                    shutdownCommand = ["cmd", "/c", shutdownBat.toString(), "30", "-force"]
                } else if (Files.exists(shutdownSh)) {
                    shutdownCommand = [shutdownSh.toString(), "30", "-force"]
                } else if (Files.exists(shutdownBat)) {
                    shutdownCommand = ["cmd", "/c", shutdownBat.toString(), "30", "-force"]
                }

                if (shutdownCommand != null) {
                    runCommand(shutdownCommand, tomcatDir, ["CATALINA_PID": pidFile.toString()], 30L, true)
                }

                if (Files.exists(pidFile)) {
                    String pidText = Files.readString(pidFile).trim()
                    if (pidText ==~ /[0-9]+/) {
                        ProcessHandle.of(Long.parseLong(pidText)).ifPresent { ProcessHandle process ->
                            terminateProcess(process)
                        }
                    }

                    Files.deleteIfExists(pidFile)
                }
            }
        }
    }

    String processMarker = tckDir.resolve("target").resolve("apache-tomcat-").toString().replace('\\', '/')
    ProcessHandle.allProcesses().forEach { ProcessHandle process ->
        if (process.pid() == ProcessHandle.current().pid()) {
            return
        }

        String commandLine = process.info().commandLine().orElse("")
        if (!commandLine.isEmpty() && commandLine.replace('\\', '/').contains(processMarker)) {
            terminateProcess(process)
        }
    }
}

def portIsListening = { int port ->
    Socket socket = new Socket()
    try {
        socket.connect(new InetSocketAddress("127.0.0.1", port), 400)
        return true
    } catch (IOException ignored) {
        return false
    } finally {
        try {
            socket.close()
        } catch (IOException ignored) {
        }
    }
}

def pickAvailableTomeePort = { int preferredPort ->
    if (!portIsListening(preferredPort)) {
        return preferredPort
    }

    for (int fallbackPort = 9080; fallbackPort <= 9180; fallbackPort++) {
        if (!portIsListening(fallbackPort)) {
            return fallbackPort
        }
    }

    return -1
}

def patchTomeeProfile = { Path pomFile ->
    if (!Files.exists(pomFile)) {
        return
    }

    String text = Files.readString(pomFile)
    String updated = text
    updated = updated.replaceAll(
        "(?s)\\n\\s*<artifactItem>\\s*<groupId>org\\.apache\\.tomcat</groupId>\\s*<artifactId>tomcat-catalina</artifactId>.*?</artifactItem>",
        ""
    )
    updated = updated.replaceAll("\\n\\s*target/catalina\\.jar\\s*", "\n")

    updated = updated.replaceAll(
        "(?s)\\n\\s*<artifactItem>\\s*<groupId>org\\.omnifaces</groupId>\\s*<artifactId>jacc-provider-tomee-bridge</artifactId>.*?</artifactItem>",
        ""
    )
    updated = updated.replaceAll(
        "(?s)\\n\\s*<artifactItem>\\s*<groupId>javax\\.security\\.jacc</groupId>\\s*<artifactId>javax.security.jacc-api</artifactId>.*?</artifactItem>",
        ""
    )
    updated = updated.replaceAll(
        "(?s)\\n\\s*<execution>\\s*<id>Patch TomEE</id>.*?</execution>",
        ""
    )

    updated = updated.replaceAll("(?m)^\\s*target/jacc-provider-tomee-bridge-0\\.3\\.jar\\s*\\n?", "")
    updated = updated.replaceAll("(?m)^\\s*target/jacc-provider-0\\.3\\.jar\\s*\\n?", "")
    updated = updated.replaceAll("(?m)^\\s*target/javax.security.jacc-api-1\\.6\\.jar\\s*\\n?", "")
    updated = updated.replaceAll("(?m)^\\s*javax\\.security\\.jacc\\.policy\\.provider=.*\\n?", "")
    updated = updated.replaceAll("(?m)^\\s*javax\\.security\\.jacc\\.PolicyConfigurationFactory\\.provider=.*\\n?", "")
    updated = updated.replaceAll(
        "(?s)\\n\\s*<repository>\\s*<id>payara-patched-eclipse</id>\\s*<url>https://raw\\.github\\.com/payara/Payara_PatchedProjects/eclipse/</url>\\s*</repository>",
        ""
    )

    if (updated != text) {
        Files.writeString(pomFile, updated, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    }
}

def patchOpenid3Module = { Path tckRoot ->
    if (!Files.exists(tckRoot)) {
        return
    }

    Path openid3Pom = tckRoot
        .resolve("app-openid3")
        .resolve("pom.xml")

    if (!Files.exists(openid3Pom)) {
        return
    }

    String text = Files.readString(openid3Pom)
    String updated = text.replace("<alias>tomcat</alias>", "<alias>tomcat-openid3</alias>")

    if (updated != text) {
        Files.writeString(openid3Pom, updated, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    }
}

def patchSignatureTestModule = { Path tckRoot ->
    if (!Files.exists(tckRoot)) {
        return
    }

    Path signaturePom = tckRoot
        .resolve("security-signaturetest")
        .resolve("pom.xml")

    if (!Files.exists(signaturePom)) {
        return
    }

    String text = Files.readString(signaturePom)
    String updated = text.replace("<version>2.0.1.MR</version>", "<version>2.0.1</version>")

    if (updated != text) {
        Files.writeString(signaturePom, updated, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    }
}

def patchOpenidHttpReferences = { Path tckRoot, int httpPort ->
    if (!Files.exists(tckRoot)) {
        return
    }

    Map<String, String> replacements = [
        "http://localhost:8080/openid-client/Callback": "http://localhost:${httpPort}/openid-client/Callback",
        "http://localhost:8080/openid-server/webresources/oidc-provider-demo": "http://localhost:${httpPort}/openid-server/webresources/oidc-provider-demo"
    ]

    Files.walk(tckRoot).withCloseable { stream ->
        stream.filter { Path file ->
            if (!Files.isRegularFile(file)) {
                return false
            }

            String normalized = file.toString().replace('\\', '/')
            String relative = tckRoot.relativize(file).toString().replace('\\', '/')
            if (relative.contains("/target/") || relative.startsWith("target/") || normalized.endsWith(".jks") || normalized.endsWith(".war") || normalized.endsWith(".jar") || normalized.endsWith(".class")) {
                return false
            }

            return normalized.contains("/app-openid/") ||
                normalized.contains("/app-openid2/") ||
                normalized.contains("/app-openid3/") ||
                normalized.contains("/app-multiple-store-backup/app-openid2/")
        }.forEach { Path file ->
            String text = Files.readString(file)
            String updated = text

            replacements.each { String source, String replacement ->
                updated = updated.replace(source, replacement)
            }

            if (updated != text) {
                Files.writeString(file, updated, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
            }
        }
    }
}

def patchCommonTomeeServerXml = { Path tckRoot ->
    if (!Files.exists(tckRoot)) {
        return
    }

    Path serverXml = tckRoot
        .resolve("common")
        .resolve("src")
        .resolve("main")
        .resolve("resources")
        .resolve("tomee")
        .resolve("server.xml")

    Path contextXml = tckRoot
        .resolve("common")
        .resolve("src")
        .resolve("main")
        .resolve("resources")
        .resolve("tomee")
        .resolve("context.xml")

    if (!Files.exists(serverXml)) {
        return
    }

    String text = Files.readString(serverXml)
    String updated = text

    updated = updated.replaceAll(
        "(?m)^\\s*<Connector\\s+port=\"8009\"\\s+protocol=\"AJP/1\\.3\"\\s+redirectPort=\"8443\"\\s*/>\\n?",
        ""
    )
    updated = updated.replaceAll(
        "(?m)^\\s*<Valve\\s+className=\"org\\.omnifaces\\.jaccprovidertomee\\.catalina\\.JaccBridgeValve\"\\s*/>\\n?",
        ""
    )
    updated = updated.replaceAll(
        "(?m)^\\s*<Listener\\s+className=\"org\\.omnifaces\\.jaccprovidertomee\\.catalina\\.ContextIdThreadContextListener\"\\s*/>\\n?",
        ""
    )

    if (updated != text) {
        Files.writeString(serverXml, updated, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    }

    if (!Files.exists(contextXml)) {
        return
    }

    String contextText = Files.readString(contextXml)
    String updatedContextText = contextText.replaceAll(
        "(?m)^\\s*<Listener\\s+className=\"org\\.omnifaces\\.jaccprovidertomee\\.catalina\\.ParsedWebXmlContextListener\"\\s*/>\\n?",
        ""
    )

    if (updatedContextText != contextText) {
        Files.writeString(contextXml, updatedContextText, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
    }
}

def unzipArchive = { Path archive, Path destination ->
    Files.createDirectories(destination)

    ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archive))
    try {
        ZipEntry entry
        byte[] buffer = new byte[8192]

        while ((entry = zipInputStream.nextEntry) != null) {
            Path resolved = destination.resolve(entry.getName()).normalize()
            if (!resolved.startsWith(destination)) {
                throw new IOException("Zip entry escapes destination: ${entry.getName()}")
            }

            if (entry.isDirectory()) {
                Files.createDirectories(resolved)
            } else {
                Files.createDirectories(resolved.getParent())
                OutputStream outputStream = Files.newOutputStream(
                    resolved,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                )
                try {
                    int read
                    while ((read = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, read)
                    }
                } finally {
                    outputStream.close()
                }

                if (!windows && resolved.getFileName().toString().endsWith(".sh")) {
                    resolved.toFile().setExecutable(true, false)
                }
            }

            zipInputStream.closeEntry()
        }
    } finally {
        zipInputStream.close()
    }
}

def parseArguments = { String argsLine ->
    List<String> args = []
    if (argsLine == null || argsLine.trim().isEmpty()) {
        return args
    }

    def matcher = argsLine =~ /"((?:[^"\\]|\\.)*)"|'((?:[^'\\]|\\.)*)'|(\S+)/
    matcher.each { match ->
        if (match[1] != null) {
            args << match[1].replaceAll(/\\(["\\])/, '$1')
        } else if (match[2] != null) {
            args << match[2].replaceAll(/\\(['\\])/, '$1')
        } else {
            args << match[3]
        }
    }

    return args
}

def resolveMavenCommand = { String configuredCommand ->
    String command = configuredCommand == null ? "" : configuredCommand.trim()
    if (command.isEmpty()) {
        command = "mvn"
    }

    if (!windows) {
        return [command]
    }

    String lower = command.toLowerCase(Locale.ROOT)
    if (lower.endsWith(".cmd") || lower.endsWith(".bat") || lower.endsWith(".exe")) {
        return [command]
    }

    Path cmdVariant = Paths.get(command + ".cmd")
    if (Files.exists(cmdVariant)) {
        return [cmdVariant.toString()]
    }

    Path pathVariant = Paths.get(command)
    if (Files.exists(pathVariant)) {
        return [command]
    }

    return ["cmd", "/c", command]
}

try {
    println "Preparing Jakarta Security TCK ${securityTckVersion}"

    Files.createDirectories(securityTckWorkDir)

    stopOpenidProvider(extractedTckDir)
    stopOpenidProvider(outerProjectRoot)
    deleteRecursively(securityTckDir)

    if (!Files.exists(securityTckArchive) || Files.size(securityTckArchive) == 0L) {
        println "Downloading ${securityTckUrl}"
        Files.deleteIfExists(securityTckArchive)

        URLConnection connection = new URL(securityTckUrl).openConnection()
        connection.setConnectTimeout(30000)
        connection.setReadTimeout(300000)
        connection.getInputStream().withCloseable { stream ->
            Files.copy(stream, securityTckArchive, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    unzipArchive(securityTckArchive, securityTckWorkDir)

    if (!Files.exists(extractedTckDir)) {
        throw new IllegalStateException("Extracted TCK directory not found: ${extractedTckDir}")
    }

    Files.createDirectories(truststore.getParent())
    patchTomeeProfile(extractedTckDir.resolve("pom.xml"))
    patchCommonTomeeServerXml(extractedTckDir)
    patchOpenid3Module(extractedTckDir)
    patchSignatureTestModule(extractedTckDir)

    if (portIsListening(8443)) {
        throw new IllegalStateException("Port 8443 is already in use. The OpenID provider requires this fixed port.")
    }

    int resolvedTomeeHttpPort = pickAvailableTomeePort(preferredTomeeHttpPort)
    if (resolvedTomeeHttpPort < 0) {
        throw new IllegalStateException("Unable to find a free HTTP port for TomEE in range 9080-9180.")
    }

    if (resolvedTomeeHttpPort != preferredTomeeHttpPort) {
        println "Port ${preferredTomeeHttpPort} is busy; using ${resolvedTomeeHttpPort} for TomEE HTTP."
    }

    patchOpenidHttpReferences(extractedTckDir, resolvedTomeeHttpPort)

    println "Running Jakarta Security TCK ${securityTckVersion} against TomEE ${tomeeVersion}"

    List<String> command = []
    command.addAll(resolveMavenCommand(securityTckMavenCommand))
    command.addAll([
        "-B",
        "-ntp",
        "clean",
        "install",
        "-Dmaven.multiModuleProjectDirectory=${extractedTckDir}",
        "-pl",
        securityTckModules,
        "-Ptomee",
        "--fail-at-end",
        "-Dtomee.version=${tomeeVersion}",
        "-Dglassfish.root=${extractedTckDir.resolve('target')}",
        "-Dtomee.classifier=${securityTckClassifier}",
        "-Dtomee.stopPort=-1",
        "-Dtomee.httpPort=${resolvedTomeeHttpPort}",
        "-Dtomee.httpsPort=-1",
        "-Dtomee.ajpPort=-1",
        "-Dverbose=${securityTckTomeeVerbose}",
        "-Djavax.net.ssl.trustStore=${truststore}",
        "-Djavax.net.ssl.trustStorePassword=changeit",
        "-Dsigtest.api.groupId=${sigtestApiGroupId}",
        "-Dsigtest.api.artifactId=${sigtestApiArtifactId}",
        "-Dsigtest.api.version=${sigtestApiVersion}",
        "-Dtomee.catalina_opts=-Djavax.net.ssl.trustStore=${truststore} -Djavax.net.ssl.trustStorePassword=changeit"
    ])
    command.addAll(parseArguments(securityTckExtraMavenArgs))

    runCommand(command, extractedTckDir)
} finally {
    stopOpenidProvider(extractedTckDir)
    stopOpenidProvider(outerProjectRoot)
}
