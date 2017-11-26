package org.multiapp.server.service;

import org.multiapp.server.domain.Application;
import org.multiapp.server.domain.Processus;
import org.multiapp.server.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryService {

	public static Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

	@Autowired
	private Configuration configuration;

	@Autowired
	private RunService runService;

	public List<Path> getInstallable() throws IOException {
		Path p = configuration.getDownloadDirectory();
		try {
			List<Path> listeRepertoires = Files.list(p)
					//.filter(x -> x.endsWith(".jar"))
					.collect(Collectors.toList());

			return listeRepertoires;
		} catch (IOException e) {
			LOGGER.error("Erreur pour lister les installeurs", e);
			throw e;
		}
	}

	public List<Application> getApplication() throws IOException {
		Path p = configuration.getInstallDirectory();
		try {
			List<Application> listeRepertoires = Files.list(p)
					.map(x -> new Application(AppUtil.appName(x), x))
					.collect(Collectors.toList());

			return listeRepertoires;
		} catch (IOException e) {
			LOGGER.error("Erreur pour lister les applications", e);
			throw e;
		}
	}

	public List<Processus> getProcessList() {
		return runService.getProcessList();
	}
}
