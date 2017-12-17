package org.multiapp.server.service;

import com.google.common.base.CharMatcher;
import com.google.common.base.Verify;
import com.google.common.collect.Maps;
import org.multiapp.server.domain.ApplicationName;
import org.multiapp.server.domain.DirectoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstallService {

	public static Logger LOGGER = LoggerFactory.getLogger(InstallService.class);

	@Autowired
	private Configuration configuration;

	public void install(Path path) {
		Verify.verifyNotNull(path);
		LOGGER.info("install {} ...", path);

		try {
			//extractAll(URI.create("jar:file:" + path.toUri().getPath()), null);
			final URI fileUri = URI.create("jar:file:" + path.toUri().getPath());
			byte[] buf = getFileFromZip(fileUri, "/BOOT-INF/classes/install/install.properties");

			if (buf != null) {
				Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(buf));
				String nom = properties.getProperty("nom");
				LOGGER.info("nom= {}", nom);
				initialiseApp(nom, properties, path);
			}
		} catch (IOException e) {
			LOGGER.error("Error", e);
		}

		LOGGER.info("install {} OK", path);
	}

	private void initialiseApp(String nom, Properties properties, Path fileToInstall) {
		String nom2 = trouveNom(nom);
		Path installDir = configuration.getInstallDirectory();
		Path p = installDir.resolve(nom2);
		try {
			LOGGER.info("creation du repertoire {} ...", p);
			Files.createDirectory(p);
			LOGGER.info("creation du repertoire {} OK", p);

			LOGGER.info("copie du fichier d'installation ...");
			Files.copy(fileToInstall, p.resolve(fileToInstall.getFileName()));
			LOGGER.info("copie du fichier d'installation OK");

			Path configDir = configuration.getLocalConfigDirectory().resolve(nom2);

			LOGGER.info("creation du repertoire de config {} ...", configDir);
			Files.createDirectory(configDir);
			LOGGER.info("creation du repertoire de config {} OK", configDir);

			Path logDir = configuration.getLocalLogDirectory().resolve(nom2);

			LOGGER.info("creation du repertoire de log {} ...", logDir);
			Files.createDirectory(logDir);
			LOGGER.info("creation du repertoire de log {} OK", logDir);

			Path fichierConfApp = configDir.resolve("app.properties");
			LOGGER.info("enregistrement du fichier de configuration {} ...", fichierConfApp);
			properties.store(Files.newBufferedWriter(fichierConfApp), "conf app " + nom2);
			LOGGER.info("enregistrement du fichier de configuration {} OK", fichierConfApp);

			final URI fileUri = URI.create("jar:file:" + fileToInstall.toUri().getPath());
			byte[] buf = getFileFromZip(fileUri, "/BOOT-INF/classes/install/application.properties");

			Path fichierConfApp2 = configDir.resolve("application.properties");
			LOGGER.info("enregistrement du fichier de configuration {} ...", fichierConfApp2);
			Files.write(fichierConfApp2, buf);
			LOGGER.info("enregistrement du fichier de configuration {} OK", fichierConfApp2);

			Path fichierConfPath = configDir.resolve("tvfsconfig.properties");
			LOGGER.info("enregistrement du fichier de configuration des répertoires {} ...", fichierConfPath);
			List<String> confPath = getConfPath(configDir, logDir);
			buf = getFileFromZip(fileUri, "/BOOT-INF/classes/install/tvfsconfig.properties");
			if (buf != null && buf.length > 0) {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								new ByteArrayInputStream(buf), StandardCharsets.UTF_8))) {
					List<String> lines = reader.lines().collect(Collectors.toList());
					confPath.addAll(lines);
				}
			}
			Files.write(fichierConfPath, confPath, StandardOpenOption.CREATE_NEW);
			LOGGER.info("enregistrement du fichier de configuration des répertoires {} OK", fichierConfPath);


			Path fichierConfLog = configDir.resolve("log4j2.xml");
			buf = getFileFromZip(fileUri, "/BOOT-INF/classes/install/log4j2.xml");
			if (buf != null && buf.length > 0) {
				LOGGER.info("enregistrement du fichier de configuration {} ...", fichierConfLog);
				Files.write(fichierConfLog, buf);
				LOGGER.info("enregistrement du fichier de configuration {} OK", fichierConfLog);
			}

		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}

	private List<String> getConfPath(Path configDir, Path logDir) {
		List<String> res = new ArrayList<>();
		res.add("# log dir");
		res.add("tvfs.dir1.name=log");
		res.add("tvfs.dir1.directory=" + logDir.toAbsolutePath());
		res.add("tvfs.dir1.readonly=false");
		res.add("# conf dir");
		res.add("tvfs.dir2.name=conf");
		res.add("tvfs.dir2.directory=" + configDir.toAbsolutePath());
		res.add("tvfs.dir2.readonly=true");
		return res;
	}

	private String trouveNom(String nom) {
		Verify.verifyNotNull(nom);
		Path installDir = configuration.getInstallDirectory();
		nom = nom.trim();
		nom = CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.digit()).retainFrom(nom);
		Path p = installDir.resolve(nom);
		if (Files.exists(p)) {
			String nom2;
			int i = 2;
			do {
				nom2 = nom + i;
				p = installDir.resolve(nom2);
				i++;
			} while (Files.exists(p));
			nom = nom2;
		}
		return nom;
	}

	private void extractAll(URI fromZip, Path toDirectory) throws IOException {
		FileSystems.newFileSystem(fromZip, Collections.emptyMap())
				.getRootDirectories()
				.forEach(root -> {
					// in a full implementation, you'd have to
					// handle directories
					try {
						Files.walk(root).forEach(path -> {
							LOGGER.info("path: {}", path);
//							try {
//								Files.copy(path, toDirectory);
//							} catch (IOException e) {
//								LOGGER.error("Error", e);
//							}
						});
					} catch (IOException e) {
						LOGGER.error("Error", e);
					}
				});
	}

	private byte[] getFileFromZip(URI fromZip, String fichier) throws IOException {
		//byte[][] res = new byte[0][];
		List<byte[]> res = new ArrayList<>();
		try (FileSystem fs = FileSystems.newFileSystem(fromZip, Maps.newHashMap())) {
			fs.getRootDirectories()
					.forEach(root -> {
						// in a full implementation, you'd have to
						// handle directories
						try {
							Files.walk(root).forEach(path -> {
								if (res.isEmpty()) {
									LOGGER.trace("path: {}", path);
									try {
										if (path.toString().equals(fichier)) {
											res.add(Files.readAllBytes(path));
											//Files.copy(path, toDirectory);
										}
									} catch (IOException e) {
										LOGGER.error("Error", e);
									}
								}
							});
						} catch (IOException e) {
							LOGGER.error("Error", e);
						}
					});
		}
		if (res.isEmpty()) {
			return null;
		} else {
			return res.get(0);
		}
	}

	public void uninstall(ApplicationName nomApp) {
		Verify.verifyNotNull(nomApp);
		LOGGER.info("uninstall {} ...", nomApp);

		try {
			Optional<Path> pathOpt = configuration.getLocalDirectory(DirectoryType.INSTALL, nomApp);

			if (pathOpt.isPresent()) {
				Path path = pathOpt.get();

				LOGGER.info("Suppression de l'application {} ...", nomApp);
				deleteAll(path);
				LOGGER.info("Suppression de l'application {} OK", nomApp);

				Optional<Path> configDirectoryOpt = configuration.getLocalDirectory(DirectoryType.CONFIGURATION, nomApp);
				if (configDirectoryOpt.isPresent()) {
					LOGGER.info("Suppression de la configuration de l'application {} ...", nomApp);
					deleteAll(configDirectoryOpt.get());
					LOGGER.info("Suppression de la configuration de l'application {} OK", nomApp);
				}

				Optional<Path> logDirectoryOpt = configuration.getLocalDirectory(DirectoryType.LOGGING, nomApp);
				if (logDirectoryOpt.isPresent()) {
					LOGGER.info("Suppression des logs de l'application {} ...", nomApp);
					deleteAll(logDirectoryOpt.get());
					LOGGER.info("Suppression des logs de l'application {} OK", nomApp);
				}
			} else {
				LOGGER.error("Impossible de trouver l'application {}", nomApp);
			}

		} catch (IOException e) {
			LOGGER.error("Erreur pour supprimer l'application {} :", nomApp, e);
		}
	}

	private void deleteAll(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			List<Path> liste = Files.list(path).collect(Collectors.toList());
			if (!liste.isEmpty()) {
				for (Path p : liste) {
					deleteAll(p);
				}
			}
			Files.delete(path);
		} else {
			Files.delete(path);
		}
	}
}
