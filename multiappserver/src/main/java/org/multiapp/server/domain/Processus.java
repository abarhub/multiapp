package org.multiapp.server.domain;

public class Processus {

	private final Process process;
	private final int id;
	private final ApplicationName nomApp;

	public Processus(Process process, int id, ApplicationName nomApp) {
		this.process = process;
		this.id = id;
		this.nomApp = nomApp;
	}

	public Process getProcess() {
		return process;
	}

	public int getId() {
		return id;
	}

	public ApplicationName getNomApp() {
		return nomApp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Processus)) return false;

		Processus processus = (Processus) o;

		return id == processus.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "Processus{" +
				"id=" + id +
				", nomApp='" + nomApp + '\'' +
				'}';
	}
}
