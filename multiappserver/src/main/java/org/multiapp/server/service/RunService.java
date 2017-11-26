package org.multiapp.server.service;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import org.multiapp.server.domain.ApplicationName;
import org.multiapp.server.domain.DirectoryType;
import org.multiapp.server.domain.Processus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class RunService {

	public static Logger LOGGER = LoggerFactory.getLogger(RunService.class);

	@Autowired
	private Configuration configuration;

	private AtomicInteger compteur = new AtomicInteger(1);

	private List<Processus> processList = new CopyOnWriteArrayList<>();

	public void run(ApplicationName nomApp) {
		Verify.verifyNotNull(nomApp);
		Optional<Path> pathOpt = configuration.getLocalDirectory(DirectoryType.INSTALL, nomApp);

		if (pathOpt.isPresent()) {
			try {
				Path path = pathOpt.get();
				List<Path> liste = Files.list(path)
						.filter(x -> x.getFileName().toString().endsWith(".jar"))
						.collect(Collectors.toList());

				if (liste.isEmpty()) {
					LOGGER.error("Error: Impossible de trouver l'executable");
				} else {
					Path p2 = liste.get(0);
					LOGGER.info("run {}", p2);

					Runnable run = () -> {
						try {
							runProcess(nomApp, path, p2);
						} catch (IOException e) {
							LOGGER.error("Error", e);
						}
					};
					new Thread(run).start();
				}

			} catch (IOException e) {
				LOGGER.error("Error", e);
			}
		} else {
			LOGGER.error("Error: Impossible de trouver l'executable {}", nomApp);
		}
	}

	private void runProcess(ApplicationName nomApp, Path workingDir, Path exec) throws IOException {
		List<String> liste = new ArrayList<>();
		liste.add("java");
		liste.add("-jar");
		liste.add(exec.toString());

		Path localDir = configuration.getLocalDirectory();
		Optional<Path> pathOpt = configuration.getLocalDirectory(DirectoryType.CONFIGURATION, nomApp);
		if (pathOpt.isPresent()) {
			Path appliConf = pathOpt.get().resolve("application.properties");
			if (Files.exists(appliConf)) {
				liste.add("--spring.config.location=file:/" + appliConf.toString());
			}

			LOGGER.info("run {} : {}", nomApp, liste);

			ProcessBuilder pb = new ProcessBuilder(liste);
			Map<String, String> env = pb.environment();

			long timestamp = System.currentTimeMillis();
			Path tempdirBase = localDir.resolve("temp").resolve(nomApp + "_" + timestamp);

			if (Files.exists(tempdirBase)) {
				int i = 2;
				do {
					tempdirBase = localDir.resolve("temp").resolve(nomApp + "_" + timestamp + "_" + i);
					i++;
				} while (Files.exists(tempdirBase));
			}
			Files.createDirectory(tempdirBase);
			env.put("TEMP", tempdirBase.toString());
//				env.put("VAR1", "myValue");
//				env.remove("OTHERVAR");
//				env.put("VAR2", env.get("VAR1") + "suffix");
			pb.directory(workingDir.toFile());
			Optional<Path> logDirOpt = configuration.getLocalDirectory(DirectoryType.LOGGING, nomApp);
			if (logDirOpt.isPresent()) {
				Path logDir = logDirOpt.get();
				File logErr = logDir.resolve("stderr.log").toFile();
				pb.redirectError(ProcessBuilder.Redirect.appendTo(logErr));
				File logOut = logDir.resolve("stdout.log").toFile();
				pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logOut));
				Process p3 = null;
				Processus processus = null;
				try {
					LOGGER.info("start {} ...", exec);
					p3 = pb.start();
					LOGGER.info("start {} OK", exec);

					processus = new Processus(p3, getCompteur(), nomApp);

					processList.add(processus);

					try {
						int codeRetour = p3.waitFor();
						LOGGER.info("code retour : {}", codeRetour);
					} catch (InterruptedException e) {
						LOGGER.error("Error", e);
					}
				} finally {
					if (p3 != null) {
						processList.remove(processus);
					}
					LOGGER.info("process {} end", exec);
					try {
						Files.delete(tempdirBase);
					} catch (Exception e) {
						LOGGER.error("Impossible de supprimer le répertoire temporaire : {}", tempdirBase);
					}
				}
			} else {
				LOGGER.error("Impossible de trouver le répertoire de log");
			}
		} else {
			LOGGER.error("Impossible de trouver le répertoire de configuration");
		}
	}

	private int getCompteur() {
		return compteur.getAndIncrement();
	}

	public List<Processus> getProcessList() {
		return ImmutableList.copyOf(processList);
	}

	public void kill(int idProc) {
		Processus p = getProcessusById(idProc);
		if (p == null) {
			LOGGER.error("Impossible de trouver le processus {}", idProc);
		} else {
			if (p.getProcess().isAlive()) {
				p.getProcess().destroy();
			}
		}
	}

	private Processus getProcessusById(int id) {
		for (int i = 0; i < processList.size(); i++) {
			if (processList.get(i).getId() == id) {
				return processList.get(i);
			}
		}
		return null;
	}
}
