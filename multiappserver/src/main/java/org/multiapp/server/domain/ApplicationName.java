package org.multiapp.server.domain;

import com.google.common.base.Verify;
import org.multiapp.server.util.AppUtil;

public class ApplicationName {

	private final String name;

	public ApplicationName(String name) {
		Verify.verifyNotNull(name);
		Verify.verify(!name.trim().isEmpty(), "name is empty");
		Verify.verify(AppUtil.isNameValide(name), "name is invalide");
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ApplicationName)) return false;

		ApplicationName that = (ApplicationName) o;

		return name != null ? name.equals(that.name) : that.name == null;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public String toString() {
		return name;
	}
}
