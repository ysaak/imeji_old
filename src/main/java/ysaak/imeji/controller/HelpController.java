package ysaak.imeji.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ysaak.imeji.exception.ImejiException;
import ysaak.imeji.service.MarkdownService;

import java.io.IOException;

@Controller
@RequestMapping("/help")
public class HelpController {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelpController.class);

	private final MarkdownService markdownService;

	@Autowired
	public HelpController(MarkdownService markdownService) {
		this.markdownService = markdownService;
	}

	@GetMapping("/")
	public ModelAndView list() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("help/index");
		return mav;
	}

	@GetMapping("/{id}")
	public ModelAndView showPage(@PathVariable("id") String page, RedirectAttributes redirectAttributes) {
		final ModelAndView mav = new ModelAndView();

		final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
		final Resource resource = resolver.getResource("classpath:/help/" + page + ".md");

		try {
			final String renderedFile = markdownService.parseFile(resource.getFile());
			mav.setViewName("help/page");
			mav.addObject("page", renderedFile);
		}
		catch (IOException | ImejiException e) {
			LOGGER.error("Error while loading help file", e);
			mav.setViewName("help/index");
			mav.addObject("error", "Error while loading requested help file");
		}

		return mav;
	}
}
