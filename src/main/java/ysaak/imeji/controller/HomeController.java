package ysaak.imeji.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ysaak.imeji.data.PagedData;
import ysaak.imeji.model.Tag;
import ysaak.imeji.model.Wallpaper;
import ysaak.imeji.service.TagService;
import ysaak.imeji.service.WallpaperService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/")
public class HomeController {

	private final WallpaperService wallpaperService;

	private final TagService tagService;

	@Autowired
	public HomeController(WallpaperService wallpaperService, TagService tagService) {
		this.wallpaperService = wallpaperService;
		this.tagService = tagService;
	}

	@GetMapping(path = "/")
	public ModelAndView welcome(Pageable pageable) {
		return search("", pageable);
	}

	@GetMapping("/search")
	public ModelAndView search(@RequestParam("q") String query, Pageable pageable) {
		Page<Wallpaper> wallpaperPage = wallpaperService.search(query, pageable, false);
		PagedData<Wallpaper> page = new PagedData<>(wallpaperPage, pageable);

		final List<Tag> tagList;
		if (page.hasContent()) {
			Set<String> tagCodeList = new HashSet<>();
			page.getContent().stream().map(Wallpaper::getTags).forEach(tagCodeList::addAll);

			tagList = tagService.findNonGeneralByCode(tagCodeList);

			tagList.sort(WallpaperController.TAG_COMPARATOR);
		} else {
			tagList = Collections.emptyList();
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName("index");
		mav.addObject("wallpaperPage", page);
		mav.addObject("tagList", tagList);
		mav.addObject("searchQuery", query);
		return mav;
	}
}
