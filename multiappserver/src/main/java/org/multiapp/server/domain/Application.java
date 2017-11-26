package org.multiapp.server.domain;

import com.google.common.base.Verify;

import java.nio.file.Path;

public class Application {

	private final ApplicationName applicationName;
	private final Path path;

	public Application(ApplicationName applicationName, Path path) {
		Verify.verifyNotNull(applicationName);
		Verify.verifyNotNull(path);
		this.applicationName = applicationName;
		this.path = path;
	}

	public ApplicationName getApplicationName() {
		return applicationName;
	}

	public Path getPath() {
		return path;
	}
}
