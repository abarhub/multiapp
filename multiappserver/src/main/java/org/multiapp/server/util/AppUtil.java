package org.multiapp.server.util;

import com.google.common.base.Verify;
import org.multiapp.server.domain.ApplicationName;

import java.nio.file.Path;

public class AppUtil {

	public static boolean isNameValide(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		if (!name.matches("^[a-zA-Z][a-zA-Z0-9]+")) {
			return false;
		}
		return true;
	}

	public static ApplicationName appName(Path path) {
		Verify.verifyNotNull(path);
		String appName = path.getFileName().toString();
		Verify.verifyNotNull(appName);
		Verify.verify(!appName.trim().isEmpty(), "path '" + appName + "' invalide");
		return new ApplicationName(appName);
	}
}
