package org.multiapp.server.web.dto;

public class PackageDto {

	private String filename;
	private String path;

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
				"filename='" + filename + '\'' +
				", path=" + path +
				'}';
	}
}
