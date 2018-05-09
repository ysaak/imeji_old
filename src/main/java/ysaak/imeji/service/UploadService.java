package ysaak.imeji.service;

import com.google.common.io.Files;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ysaak.imeji.data.Color;
import ysaak.imeji.data.Rating;
import ysaak.imeji.model.Tag;
import ysaak.imeji.model.Wallpaper;
import ysaak.imeji.exception.ImejiException;
import ysaak.imeji.exception.WallpaperError;
import ysaak.imeji.utils.ImagePHash;
import ysaak.imeji.utils.colorthief.ColorThief;
import ysaak.imeji.utils.validation.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UploadService {
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg");

	private final WallpaperService wallpaperService;

	private final TagService tagService;

	@Value("${imeji.upload.similarity-threshold}")
	private double wallpaperSimilarityThreshold;

	@Autowired
	public UploadService(WallpaperService wallpaperService, TagService tagService) {
		this.wallpaperService = wallpaperService;
		this.tagService = tagService;
	}

	public Wallpaper createImage(MultipartFile file, String tags) throws ImejiException {
		Validate.checkNotNull(file, "File");
		Validate.checkNotNull(file.getOriginalFilename(), "Original file");
		Validate.checkNotNull(tags, "tags");
		Validate.isFalse(tags.isEmpty(), "tags", "tag list is empty");

		Wallpaper wallpaper = new Wallpaper();
		wallpaper.setType(Files.getFileExtension(file.getOriginalFilename()));

		checkFileIsImage(file, wallpaper.getType());


		wallpaper.setFileSize(file.getSize());
		wallpaper.setUploadDate(new Date());
		wallpaper.setRating(Rating.QUESTIONABLE);

		// Load image
		BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(file.getInputStream());
		} catch (IOException e) {
			throw new ImejiException(WallpaperError.WALL_UPLOAD_IMAGE_LOADING_ERROR, e);
		}

		// Store size
		wallpaper.setWidth(bufferedImage.getWidth());
		wallpaper.setHeight(bufferedImage.getHeight());

		// Calculate hash and try to find similar wallpaper already uploaded
		wallpaper.setHash(ImagePHash.hash(bufferedImage));
		List<Wallpaper> similarWallpaper = wallpaperService.searchSimilar(wallpaper, wallpaperSimilarityThreshold);
		checkSimilarWallpaperUploaded(wallpaper, similarWallpaper);

		// Extract image palette
		List<Color> palette = new ArrayList<>(5);
		int[][] p = ColorThief.getPalette(bufferedImage, 5, 1, true);
		for (int[] c : p) {
			palette.add(new Color(c[0], c[1], c[2]));
		}
		wallpaper.setPalette(palette);


		List<Tag> tagList = tagService.createTags(tags);
		wallpaper.setTags(tagList.parallelStream().map(Tag::getCode).collect(Collectors.toList()));

		final Wallpaper newWallpaper = wallpaperService.create(wallpaper, bufferedImage);

		tagService.incrementCounter(wallpaper.getTags());

		return newWallpaper;
	}

	private void checkFileIsImage(MultipartFile file, String extension) throws ImejiException {
		Validate.checkNotNull(file, "file");
		Validate.checkNotNull(extension, "extension");

		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new ImejiException(WallpaperError.WALL_UPLOAD_EXTENSION_NOT_ALLOWED_ERROR, extension);
		}

		try {
			MagicMatch match = Magic.getMagicMatch(file.getBytes(), false);

			final String mimeType = (match == null || match.getMimeType() == null) ? "unknown" : match.getMimeType();
			if (!mimeType.startsWith("image/")) {
				throw new ImejiException(WallpaperError.WALL_UPLOAD_FILE_TYPE_NOT_ALLOWED_ERROR, mimeType);
			}
		} catch (Exception e) {
			throw new ImejiException(WallpaperError.WALL_UPLOAD_FILE_TYPE_DETECTION_ERROR, e);
		}
	}

	private void checkSimilarWallpaperUploaded(Wallpaper wallpaper, List<Wallpaper> similarWallpaperList) throws ImejiException {
		Validate.checkNotNull(wallpaper, "wallpaper");
		Validate.checkNotNull(similarWallpaperList, "similarWallpaperList");

		if (!similarWallpaperList.isEmpty()) {
			final String similarId = similarWallpaperList.get(0).getId();
			throw new ImejiException(WallpaperError.WALL_UPLOAD_SIMILAR_WALLPAPER_UPLOADED_ERROR, similarId);
		}
	}
}
