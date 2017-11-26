package org.multiapp.server.service;

import com.google.common.base.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class CommandService {

	public static Logger LOGGER = LoggerFactory.getLogger(CommandService.class);

	@Autowired
	private InstallService installService;

	@Autowired
	private RunService runService;

	public void install(Path path) {
		Verify.verifyNotNull(path);

		LOGGER.info("install {}", path);

		installService.install(path);
	}

	public void run(String nomApp) {
		Verify.verifyNotNull(nomApp);

		LOGGER.info("run {}", nomApp);

		runService.run(nomApp);
	}

	public void kill(int idProc) {
		Verify.verify(idProc>0);

		LOGGER.info("kill {}", idProc);

		runService.kill(idProc);

	}

	public void uninstall(Path path) {
		Verify.verifyNotNull(path);

		LOGGER.info("uninstall {}", path);

		installService.uninstall(path);
	}
}
