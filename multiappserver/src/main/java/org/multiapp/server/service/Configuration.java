package org.multiapp.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

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

	public Path getLocalConfigDirectory(){
		return getLocalDirectory().resolve("config");
	}

	public Path getLocalLogDirectory(){
		return getLocalDirectory().resolve("log");
	}

}
