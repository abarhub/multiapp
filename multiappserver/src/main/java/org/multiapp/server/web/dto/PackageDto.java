package org.multiapp.server.web.dto;

public class PackageDto {

	private int id;
	private String filename;
	private String path;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "PackageDto{" +
				"id=" + id +
				", filename='" + filename + '\'' +
				", path='" + path + '\'' +
				'}';
	}
}
