package org.multiapp.server.service;

import com.google.common.base.Verify;
import org.multiapp.server.util.DirectoryType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class Configuration {

	@Value("${app.installdir}")
	private String installDirectory;

	@Value("${app.localdir}")
	private String localDirectory;


	public Path getInstallDirectory() {
		return Paths.get(installDirectory).toAbsolutePath();
	}

	public Path getLocalDirectory() {
		return Paths.get(localDirectory).toAbsolutePath();
	}

	public Path getLocalConfigDirectory() {
		return getLocalDirectory().resolve("config");
	}

	public Path getLocalLogDirectory() {
		return getLocalDirectory().resolve("log");
	}

	public Path getLocalTempDirectory() {
		return getLocalDirectory().resolve("temp");
	}

	public Optional<Path> getLocalDirectory(DirectoryType directoryType, String nameApp) {
		Path local = null;
		Verify.verifyNotNull(directoryType);
		Verify.verifyNotNull(nameApp);
		Verify.verify(!nameApp.trim().isEmpty(), "nameApp is empty");
		switch (directoryType) {
			case CONFIGURATION:
				local = getLocalConfigDirectory().resolve(nameApp);
				break;
			case LOGGING:
				local = getLocalLogDirectory().resolve(nameApp);
				break;
			default:
				Verify.verify(false, "directoryType invalide : " + directoryType);
		}
		if (Files.exists(local)) {
			return Optional.of(local);
		} else {
			return Optional.empty();
		}
	}

}
