package org.multiapp.server;

import com.google.common.collect.Lists;
import org.multiapp.server.bean.Processus;
import org.multiapp.server.service.CommandService;
import org.multiapp.server.service.RunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class Menu {

	public static Logger LOGGER = LoggerFactory.getLogger(Menu.class);

	@Value("${app.downloaddir}")
	private String downloadDirectory;

	@Value("${app.installdir}")
	private String installDirectory;

	private Scanner scanner;

	@Autowired
	private CommandService command;

	@Autowired
	private RunService runService;

	public void menu() {

		boolean fin = false;
		do {
			int menu;
			menu = menu(Lists.newArrayList("install", "uninstall", "run", "kill"));
			LOGGER.info("choix={}", menu);
			switch (menu) {
				case 0:
					fin = true;
					break;
				case 1:
					menuInstall();
					break;
				case 2:
					menuUninstall();
					break;
				case 3:
					menuRun();
					break;
				case 4:
					menuKill();
					break;
			}
		} while (!fin);
	}

	private void menuKill() {
		List<Processus> processusList = runService.getProcessList();

		if (processusList.isEmpty()) {
			System.out.println("Il n'y a aucun processus en cours");
		} else {
			List<String> listeProcessus = new ArrayList<>();
			List<Integer> listeIdProcessus = new ArrayList<>();

			for (Processus p : processusList) {
				listeProcessus.add(p.getNomApp() + " (" + p.getId() + ")");
				listeIdProcessus.add(p.getId());
			}
			int menu;
			menu = menu(Lists.newArrayList(listeProcessus));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				int idProc = listeIdProcessus.get(menu - 1);
				command.kill(idProc);
			}
		}
	}

	private void menuRun() {
		int menu;
		Path p = Paths.get(installDirectory);
		try {
			List<String> listeRepertoires = Files.list(p)
					//.filter(x -> x.endsWith(".jar"))
					.map(x -> x.toString())
					.collect(Collectors.toList());

			menu = menu(Lists.newArrayList(listeRepertoires));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				String s = listeRepertoires.get(menu - 1);
				Path p2 = Paths.get(s);
				command.run(p2.getFileName().toString());
			}
		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}

	private void menuInstall() {
		int menu;
		Path p = Paths.get(downloadDirectory);
		try {
			List<String> listeRepertoires = Files.list(p)
					//.filter(x -> x.endsWith(".jar"))
					.map(x -> x.toString())
					.collect(Collectors.toList());

			menu = menu(Lists.newArrayList(listeRepertoires));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				String s = listeRepertoires.get(menu - 1);
				Path p2 = Paths.get(s);
				command.install(p2.toAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}

	private void menuUninstall() {
		int menu;
		Path p = Paths.get(downloadDirectory);
		try {
			List<String> listeRepertoires = Files.list(p)
					//.filter(x -> x.endsWith(".jar"))
					.map(x -> x.toString())
					.collect(Collectors.toList());

			menu = menu(Lists.newArrayList(listeRepertoires));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				String s = listeRepertoires.get(menu - 1);
				Path p2 = Paths.get(s);
				command.uninstall(p2.toAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}


	private int menu(List<String> listeOptions) {
		boolean fin = false;
		int menuChoisi = 0;
		Scanner scanner = getScanner();

		do {
			int noMenu = 1, menuQuitter;
			for (int i = 0; i < listeOptions.size(); i++) {
				System.out.printf("%d) %s\n", noMenu, listeOptions.get(i));
				noMenu++;
			}
			System.out.printf("%d) quitter\n", noMenu);
			menuQuitter = noMenu;
			String s = scanner.nextLine();
			if (s != null && !s.trim().isEmpty()) {
				s = s.trim();
				if (s.matches("^[0-9]$")) {
					try {
						int noChoix = Integer.parseInt(s);
						if (noChoix > 0 && noChoix <= menuQuitter) {
							if (noChoix == menuQuitter) {
								menuChoisi = 0;
							} else {
								menuChoisi = noChoix;
							}
							fin = true;
						} else {
							System.err.println("valeur incorrecte");
						}
					} catch (NumberFormatException e) {
						System.err.println("valeur incorrecte");
					}
				} else {
					System.err.println("valeur incorrecte");
				}
			}
		} while (!fin);
		return menuChoisi;
	}

	private Scanner getScanner() {
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}
		return scanner;
	}
}
