package org.multiapp.server.web.dto;

public class ApplicationDto {

	private int id;
	private String name;
	private String path;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "ApplicationDto{" +
				"id=" + id +
				", name='" + name + '\'' +
				", path='" + path + '\'' +
				'}';
	}
}
