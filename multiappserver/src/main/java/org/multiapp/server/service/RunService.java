package org.multiapp.server.service;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
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
		//liste.add("-Dlog4j.debug");
		liste.add("-Dorg.apache.logging.log4j.simplelog.StatusLogger.level=TRACE");
		//liste.add("-Ddebug");
//		liste.add("-jar");
//		liste.add(exec.toString());

		Path localDir = configuration.getLocalDirectory();
		Optional<Path> pathOpt = configuration.getLocalDirectory(DirectoryType.CONFIGURATION, nomApp);
		if (pathOpt.isPresent()) {

			Path logConf = pathOpt.get().resolve("log4j2.xml");
			if (Files.exists(logConf)) {
				//liste.add("-Dlog4j.configurationFile=file://" + logConf.normalize().toAbsolutePath());
				//liste.add("-Dlog4j.configurationFile=file://C:\\projet\\tvfs\\multiapp\\multiappserver\\workspace\\local\\config\\simpleapp\\log4j2.xml");
				liste.add("-Dlog4j.configurationFile=file://C:/projet/tvfs/multiapp/multiappserver/workspace/local/config/simpleapp/log4j2.xml");
			}

			Path dirConf = pathOpt.get().resolve("tvfsconfig.properties");
			if (Files.exists(dirConf)) {
				liste.add("-DTVFS_CONFIG_FILE=file:/" + dirConf.normalize().toAbsolutePath());
			}

			liste.add("-jar");
			liste.add(exec.toString());

			Path appliConf = pathOpt.get().resolve("application.properties");
			if (Files.exists(appliConf)) {
				liste.add("--spring.config.location=file:/" + appliConf.normalize().toString());
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
				LocalDateTime date = LocalDateTime.now();
				appendStart(logErr, date);
				pb.redirectError(ProcessBuilder.Redirect.appendTo(logErr));
				File logOut = logDir.resolve("stdout.log").toFile();
				appendStart(logOut, date);
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

	private void appendStart(File logErr, LocalDateTime date) throws IOException {
		Files.write(logErr.toPath(), Lists.newArrayList("Start at " + date), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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
