package org.multiapp.server.service;

import com.google.common.base.CharMatcher;
import com.google.common.base.Verify;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
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

		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
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
									LOGGER.info("path: {}", path);
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

	public void uninstall(Path path) {
		Verify.verifyNotNull(path);
		LOGGER.info("uninstall {} ...", path);

		String nomApp = path.getFileName().toString();

		try {
			LOGGER.info("Suppression de l'application {} ...", nomApp);
			deleteAll(path);
			LOGGER.info("Suppression de l'application {} OK", nomApp);

			Path configDirectory = configuration.getLocalConfigDirectory().resolve(nomApp);
			if (Files.exists(configDirectory)) {
				LOGGER.info("Suppression de la configuration de l'application {} ...", nomApp);
				deleteAll(configDirectory);
				LOGGER.info("Suppression de la configuration de l'application {} OK", nomApp);
			}


		} catch (IOException e) {
			LOGGER.error("Erreur pour supprimer l'application {} : ", nomApp, e.getMessage());
		}
	}

	private void deleteAll(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			List<Path> liste = Files.list(path).collect(Collectors.toList());
			if (!liste.isEmpty()) {
				for (Path p : liste) {
					deleteAll(p);
				}
				deleteAll(path);
			}
			Files.delete(path);
		} else {
			Files.delete(path);
		}
	}
}
