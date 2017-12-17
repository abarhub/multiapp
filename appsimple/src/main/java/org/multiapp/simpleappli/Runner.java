package org.multiapp.simpleappli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class Runner implements CommandLineRunner {

	public static Logger LOGGER = LoggerFactory.getLogger(Runner.class);

	@Value("${app.message}")
	private String message;

	@Override
	public void run(String... args) throws Exception {
		tache();
	}

	private void tache() {
		LOGGER.info("tache");

		LOGGER.info("message : {}", message);

		LOGGER.info("Debut :");

		boolean fin = false;

		Scanner scanner = new Scanner(System.in);
		do {
			try {
				String s = scanner.nextLine();
				LOGGER.info("message : {}", s);
				if (s == null || s.trim().isEmpty()) {
					fin = true;
				}
			} catch (Exception e) {
				LOGGER.error("Erreur : {}", e.getMessage());
			}
		} while (!fin);

		LOGGER.info("Fin");
	}
}
