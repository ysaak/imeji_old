package ysaak.imeji.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ysaak.framework.router.Router;
import ysaak.imeji.data.Rating;
import ysaak.imeji.model.Tag;
import ysaak.imeji.data.TagType;
import ysaak.imeji.model.Wallpaper;
import ysaak.imeji.dto.WallpaperDto;
import ysaak.imeji.exception.ImejiException;
import ysaak.imeji.exception.NoDataFoundException;
import ysaak.imeji.service.TagService;
import ysaak.imeji.service.UploadService;
import ysaak.imeji.service.WallpaperService;
import ysaak.imeji.utils.WallpaperUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/wallpapers")
public class WallpaperController {

	public static final Comparator<Tag> TAG_COMPARATOR = (o1, o2) -> {
		Objects.requireNonNull(o1);
		Objects.requireNonNull(o2);

		TagType o1Type = o1.getType() != null ? o1.getType() : TagType.GENERAL;
		TagType o2Type = o2.getType() != null ? o2.getType() : TagType.GENERAL;

		int typeCompare = o1Type.compareTo(o2Type);
		if (typeCompare == 0) {
			return o1.getCode().compareTo(o2.getCode());
		}

		return typeCompare;
	};

	private final UploadService uploadService;

	private final WallpaperService wallpaperService;

	private final TagService tagService;

	private final Router router;

	@Autowired
	public WallpaperController(final UploadService uploadService, final WallpaperService wallpaperService, final TagService tagService, final Router router) {
		this.uploadService = uploadService;
		this.wallpaperService = wallpaperService;
		this.tagService = tagService;
		this.router = router;
	}

	@GetMapping("/new")
	public ModelAndView upload() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("wallpaper/new");
		return mav;
	}

	@PostMapping
	public ModelAndView create(@RequestParam("file[]") List<MultipartFile> fileList, @RequestParam("tags") String tags, RedirectAttributes redirectAttributes) {

		ModelAndView mav = new ModelAndView();

		try {
			List<Wallpaper> wallpaperList = new ArrayList<>();

			for (MultipartFile file : fileList) {
				Wallpaper wallpaper = uploadService.createImage(file, tags);
				wallpaperList.add(wallpaper);

				redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
			}

			if (wallpaperList.size() == 1) {
				mav.setViewName("redirect:" + router.getPath(wallpaperList.get(0)));
			} else {
				mav.setViewName("redirect:" + router.getPath("root"));
			}
		} catch (ImejiException e) {
			mav.addObject("error", e.getMessage());
			mav.addObject("tags", tags);
			mav.setViewName("wallpaper/new");
		}

		return mav;

	}

	@GetMapping("/{id}")
	public ModelAndView show(@PathVariable("id") String id) throws NoDataFoundException {
		Wallpaper wallpaper = wallpaperService.get(id);

		List<Tag> tagList = tagService.findByCode(wallpaper.getTags());

		tagList.sort(TAG_COMPARATOR);

		String wallpaperPath = "/wallpapers/" + WallpaperUtils.getWallpaperFileName(wallpaper);

		ModelAndView mav = new ModelAndView();
		mav.setViewName("wallpaper/show");
		mav.addObject("wallpaper", wallpaper);
		mav.addObject("tags", tagList);
		mav.addObject("wallpaperPath", wallpaperPath);
		return mav;
	}

	@GetMapping("/{id}/edit")
	public ModelAndView edit(@PathVariable("id") String id) throws NoDataFoundException {
		Wallpaper wallpaper = wallpaperService.get(id);
		String wallpaperPath = "/wallpapers/" + WallpaperUtils.getWallpaperFileName(wallpaper);

		ModelAndView mav = new ModelAndView();
		mav.setViewName("wallpaper/edit");
		mav.addObject("wallpaper", wallpaper);
		mav.addObject("ratingList", Arrays.asList(Rating.values()));
		mav.addObject("wallpaperPath", wallpaperPath);
		return mav;
	}

	@PutMapping("/{id}")
	public ModelAndView update(@ModelAttribute WallpaperDto wallpaperUpdateDto) throws NoDataFoundException {


		Wallpaper wallpaper = wallpaperService.update(wallpaperUpdateDto.getId(), wallpaperUpdateDto.getTags(), wallpaperUpdateDto.getRating());

		ModelAndView mav = new ModelAndView();
		mav.setViewName("redirect:" + router.getPath(wallpaper));
		return mav;
	}

	@DeleteMapping("/{id}")
	public ModelAndView deleteWallpaper(@PathVariable("id") String id, RedirectAttributes redirectAttributes) throws NoDataFoundException {
		try {
			System.err.println("Deleting " + id + " ...");
			Wallpaper wallpaper = wallpaperService.get(id);
			wallpaperService.delete(wallpaper);
			redirectAttributes.addFlashAttribute("message", "Wallpaper successfully deleted!");
		} catch (ImejiException e) {
			redirectAttributes.addFlashAttribute("error", "Error while deleting wallpaper");
		}

		return new ModelAndView("redirect:" + router.getPath("root"));
	}
}
