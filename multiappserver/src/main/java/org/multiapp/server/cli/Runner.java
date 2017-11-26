package org.multiapp.server.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class Runner implements CommandLineRunner {

	public static Logger LOGGER = LoggerFactory.getLogger(Runner.class);

	@Autowired
	private Menu menu;

	@Override
	public void run(String... args) throws Exception {
		//tache();
		tache2();
	}

	private void tache2() {
		menu.menu();
	}


//	private void tache() {
//		LOGGER.info("tache");
//
//		LOGGER.info("Debut :");
//
//		boolean fin = false;
//
//		Scanner scanner = new Scanner(System.in);
//		do {
//			try {
//				String s = scanner.nextLine();
//				LOGGER.info("message : {}", s);
//				if (s == null || s.trim().isEmpty()) {
//					fin = true;
//				}
//			} catch (Exception e) {
//				LOGGER.error("Erreur : {}", e.getMessage());
//			}
//		} while (!fin);
//
//		LOGGER.info("Fin");
//	}
}
