package ysaak.imeji.service;

import com.google.common.collect.Lists;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import ysaak.imeji.data.Rating;
import ysaak.imeji.model.Tag;
import ysaak.imeji.model.Wallpaper;
import ysaak.imeji.exception.ImejiException;
import ysaak.imeji.exception.NoDataFoundException;
import ysaak.imeji.exception.WallpaperError;
import ysaak.imeji.repository.WallpaperRepository;
import ysaak.imeji.utils.CollectionUtils;
import ysaak.imeji.utils.WallpaperSearchQueryParser;
import ysaak.imeji.utils.WallpaperUtils;
import ysaak.imeji.utils.validation.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WallpaperService {

	private static final Logger LOGGER = LoggerFactory.getLogger(WallpaperService.class);

	private final WallpaperRepository wallpaperRepository;

	private final TagService tagService;

	@Value("${imeji.wallpaper.path}")
	private String wallpaperLocalPath;

	@Value("${imeji.thumbnail.path}")
	private String thumbnailLocalPath;

	@Value("${imeji.thumbnail.width}")
	private int thumbnailWidth;

	@Value("${imeji.thumbnail.height}")
	private int thumbnailHeight;

	@Autowired
	public WallpaperService(WallpaperRepository wallpaperRepository, TagService tagService) {
		this.wallpaperRepository = wallpaperRepository;
		this.tagService = tagService;
	}

	public Wallpaper create(Wallpaper wallpaper, BufferedImage fullImage) throws ImejiException {
		Validate.checkNotNull(wallpaper, "wallpaper");
		Validate.checkNotNull(fullImage, "fullImage");

		// TODO control required fields

		wallpaper = wallpaperRepository.save(wallpaper);

		final Path fullImagePath = Paths.get(wallpaperLocalPath, WallpaperUtils.getWallpaperFileName(wallpaper));
		writeImage(fullImage, wallpaper.getType(), fullImagePath);

		// Create thumbnail
		final BufferedImage thumbnailImage = createThumbnailImage(fullImage);


		final Path thumbnailFile = Paths.get(thumbnailLocalPath, WallpaperUtils.getWallpaperFileName(wallpaper));
		writeImage(thumbnailImage, wallpaper.getType(), thumbnailFile);

		return wallpaper;
	}

	private BufferedImage createThumbnailImage(final BufferedImage fullImage) {
		final BufferedImage thumbnailImage;
		if (fullImage.getWidth() > fullImage.getHeight()) {
			thumbnailImage = Scalr.resize(fullImage, Scalr.Mode.FIT_TO_WIDTH, thumbnailWidth, thumbnailHeight);
		} else {

			// Portrait image, scale without defining height then crop to desired height

			final BufferedImage tmpThumbnailImage = Scalr.resize(fullImage, Scalr.Mode.FIT_TO_WIDTH, thumbnailWidth);

			final int x = 0;
			final int y = (int) ((tmpThumbnailImage.getHeight() / 2.0) - (thumbnailHeight / 2.0));

			thumbnailImage = Scalr.crop(tmpThumbnailImage, x, y, thumbnailWidth, thumbnailHeight);
		}

		return thumbnailImage;
	}

	private void writeImage(BufferedImage image, String type, Path targetPath) throws ImejiException {
		try {
			ImageIO.write(image, type, targetPath.toFile());
		} catch (IOException e) {
			throw new ImejiException(WallpaperError.WALL_UPLOAD_IMAGE_WRITING_ERROR, e);
		}
	}

	public List<Wallpaper> searchSimilar(Wallpaper wallpaper, double threshold) {
		Map<String, Object> params = new HashMap<>();
		params.put("sourceHash", wallpaper.getHash());
		params.put("threshold", threshold);

		Script script = new Script(ScriptType.INLINE, "painless", "(1 - (BigInteger.valueOf(doc['hash'].value).xor(BigInteger.valueOf(params.sourceHash)).bitCount() / 64.0)) > params.threshold", params);


		BoolQueryBuilder q =  QueryBuilders.boolQuery();
		q.must(QueryBuilders.scriptQuery(script));
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(q)
				.build();

		Iterable<Wallpaper> similarWallpapers = wallpaperRepository.search(searchQuery);
		for (Wallpaper w : similarWallpapers) {
			System.err.println(w.getId());
		}

		return Lists.newArrayList(similarWallpapers);
	}

	public Wallpaper get(String id) throws NoDataFoundException {
		Optional<Wallpaper> wallpaper = wallpaperRepository.findById(id);

		if (!wallpaper.isPresent()) {
			throw new NoDataFoundException("No wallpaper found with id " + id);
		}

		return wallpaper.get();
	}

	public Page<Wallpaper> search(final String search, final Pageable pageable, final boolean wildcardTags) {

		SearchQuery searchQuery = WallpaperSearchQueryParser.parse(search, wildcardTags).buildQuery(pageable);

		LOGGER.info("\n search(): searchContent [" + "" + "] \n DSL  = \n " + searchQuery.getQuery().toString());


		Page<Wallpaper> page = wallpaperRepository.search(searchQuery);

		if (!page.hasContent() && !wildcardTags) {
			return search(search, pageable, true);
		} else {
			return page;
		}
	}

	public Wallpaper update(String id, String tags, Rating rating) throws NoDataFoundException {
		Wallpaper wallpaper = get(id);
		wallpaper.setRating(rating);

		// Update tags
		List<String> currentTagList = wallpaper.getTags();
		List<String> tagList = tagService.createTags(tags).parallelStream().map(Tag::getCode).collect(Collectors.toList());

		List<String> removedTagList = wallpaper.getTags().parallelStream().filter(tag -> !tagList.contains(tag)).collect(Collectors.toList());
		List<String> addedTagList = tagList.parallelStream().filter(tag -> !currentTagList.contains(tag)).collect(Collectors.toList());

		wallpaper.setTags(tagList);
		wallpaper = wallpaperRepository.save(wallpaper);

		if (CollectionUtils.notEmpty(addedTagList)) {
			tagService.incrementCounter(addedTagList);
		}
		if (CollectionUtils.notEmpty(removedTagList)) {
			tagService.decrementCounter(removedTagList);
		}

		return wallpaper;
	}

	public void delete(Wallpaper wallpaper) throws ImejiException {
		wallpaperRepository.delete(wallpaper);

		final Path fullImagePath = Paths.get(wallpaperLocalPath, WallpaperUtils.getWallpaperFileName(wallpaper));
		deleteImage(fullImagePath);
		final Path thumbnailPath = Paths.get(thumbnailLocalPath, WallpaperUtils.getWallpaperFileName(wallpaper));
		deleteImage(thumbnailPath);

	}

	private void deleteImage(Path imagePath) throws ImejiException {
		try {
			Files.delete(imagePath);
		}
		catch (IOException e) {
			throw new ImejiException(WallpaperError.WALL_DELETE_IMAGE_ERROR, e);
		}
	}
}
