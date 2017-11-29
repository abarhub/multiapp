package org.multiapp.server.web;

import org.multiapp.server.domain.Application;
import org.multiapp.server.service.QueryService;
import org.multiapp.server.web.dto.ApplicationDto;
import org.multiapp.server.web.dto.PackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

@Controller
public class AppWeb {

	public static Logger LOGGER = LoggerFactory.getLogger(ThymeleafObjects.class);

	@Autowired
	private QueryService queryService;

	@GetMapping("/app")
	public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name,
	                       Model model) throws IOException {
		List<Path> appInstallable = queryService.getInstallable();
		List<PackageDto> appInstallable2 = convertPackage(appInstallable);
		List<Application> listApplication = queryService.getApplication();
		List<ApplicationDto> listApplication2 = convertApplication(listApplication);
		model.addAttribute("installable", appInstallable2);
		model.addAttribute("installe", listApplication2);
		LOGGER.info("model={}", model);
		return "app";
	}

	@GetMapping("/app/install")
	public ResponseEntity<Void> install(
			@RequestParam(value = "id", required = true) int id) throws IOException {
		LOGGER.info("install:{}", id);
		return ResponseEntity.noContent().build();
	}

	private List<ApplicationDto> convertApplication(List<Application> listApplication) {
		AtomicInteger compteur = new AtomicInteger(0);
		return listApplication.stream()
				.map(x -> {
					ApplicationDto applicationDto = new ApplicationDto();
					applicationDto.setName(x.getApplicationName().getName());
					applicationDto.setPath(x.getPath().toString());
					return applicationDto;
				})
				.sorted(Comparator.comparing(ApplicationDto::getName))
				.map(x -> {
					x.setId(compteur.getAndIncrement());
					return x;
				})
				.collect(toList());
	}

	private List<PackageDto> convertPackage(List<Path> appInstallable) {
		AtomicInteger compteur = new AtomicInteger(0);
		return appInstallable.stream()
				.map(x -> {
					PackageDto packageDto = new PackageDto();
					packageDto.setFilename(x.getFileName().toString());
					packageDto.setPath(x.toString());
					return packageDto;
				})
				.sorted(Comparator.comparing(PackageDto::getFilename))
				.map(x -> {
					x.setId(compteur.getAndIncrement());
					return x;
				})
				.collect(toList());
	}
}
