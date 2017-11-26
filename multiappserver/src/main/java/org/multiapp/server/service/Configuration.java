package org.multiapp.server.service;

import com.google.common.base.Verify;
import org.multiapp.server.domain.ApplicationName;
import org.multiapp.server.domain.DirectoryType;
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

	public Path getLocalAppDirectory() {
		return Paths.get(installDirectory).toAbsolutePath();
	}

	public Optional<Path> getLocalDirectory(DirectoryType directoryType, ApplicationName nameApp) {
		Path local = null;
		Verify.verifyNotNull(directoryType);
		Verify.verifyNotNull(nameApp);
		switch (directoryType) {
			case CONFIGURATION:
				local = getLocalConfigDirectory().resolve(nameApp.getName());
				break;
			case LOGGING:
				local = getLocalLogDirectory().resolve(nameApp.getName());
				break;
			case INSTALL:
				local = getLocalAppDirectory().resolve(nameApp.getName());
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
