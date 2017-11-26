package org.multiapp.server.cli;

import com.google.common.collect.Lists;
import org.multiapp.server.domain.Application;
import org.multiapp.server.domain.ApplicationName;
import org.multiapp.server.domain.Processus;
import org.multiapp.server.service.CommandService;
import org.multiapp.server.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class Menu {

	public static Logger LOGGER = LoggerFactory.getLogger(Menu.class);

	private Scanner scanner;

	@Autowired
	private CommandService command;

	@Autowired
	private QueryService queryService;

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
		List<Processus> processusList = queryService.getProcessList();

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
		try {
			List<Application> liste = queryService.getApplication();

			menu = menu(convertApplication(liste));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				Application p2 = liste.get(menu - 1);
				command.run(p2.getApplicationName());
			}
		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}

	private List<Object> convertApplication(List<Application> liste) {
		return liste.stream().map(Application::getApplicationName).collect(Collectors.toList());
	}

	private void menuInstall() {
		int menu;
		try {
			List<Path> liste = queryService.getInstallable();

			menu = menu(Lists.newArrayList(liste));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				Path p2 = liste.get(menu - 1);
				command.install(p2.toAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}

	private void menuUninstall() {
		int menu;
		try {
			List<Application> liste = queryService.getApplication();

			menu = menu(convertApplication(liste));
			LOGGER.info("choix={}", menu);
			if (menu > 0) {
				ApplicationName appName = liste.get(menu - 1).getApplicationName();
				command.uninstall(appName);
			}
		} catch (IOException e) {
			LOGGER.error("Erreur : {}", e.getMessage(), e);
		}
	}


	private int menu(List<Object> listeOptions) {
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
