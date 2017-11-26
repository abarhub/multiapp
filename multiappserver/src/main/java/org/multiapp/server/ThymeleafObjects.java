package org.multiapp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class ThymeleafObjects {

	public static Logger LOGGER = LoggerFactory.getLogger(ThymeleafObjects.class);

	@ModelAttribute("messages")
	public List<String> messages() {
		LOGGER.info("messages()");
		return Arrays.asList("Message 1", "Message 2", "Message 3");
	}

//	@GetMapping("/session-attr")
//	String sessionAttributes(HttpSession session) {
//		session.setAttribute("mySessionAttribute", "Session Attr 1");
//		return "th-objects";
//	}

	@GetMapping("/greeting")
	public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {
		model.addAttribute("name", name);
		LOGGER.info("model={}", model);
		return "greeting";
	}
}
