package ysaak.imeji.helper;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import org.springframework.stereotype.Component;
import ysaak.framework.annotations.ViewHelper;
import ysaak.imeji.data.Color;
import ysaak.imeji.model.Wallpaper;
import ysaak.imeji.utils.WallpaperUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewHelper
@Component
public class WallpaperHelper extends AbstractExtension {

	@Override
	public Map<String, Filter> getFilters() {
		Map<String, Filter> filterMap = new HashMap<>();
		filterMap.put("fileSizeFormat", new FileSizeFormat());
		filterMap.put("colorToHex", new ColorToHex());
		return filterMap;
	}

	@Override
	public Map<String, Function> getFunctions() {
		Map<String, Function> functionMap = new HashMap<>();
		functionMap.put("getWallpaperPath", new GetWallpaperPath(false));
		functionMap.put("getThumbnailPath", new GetWallpaperPath(true));
		return functionMap;
	}

	private class FileSizeFormat implements Filter {

		@Override
		public List<String> getArgumentNames() {
			return null;
		}

		@Override
		public Object apply(final Object input, final Map<String, Object> map) {
			if (input == null) {
				return null;
			}

			Long bytes = (Long) input;
			return humanReadableByteCount(bytes, false);
		}

		private String humanReadableByteCount(long bytes, boolean si) {
			int unit = si ? 1000 : 1024;
			if (bytes < unit) return bytes + " B";
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
	}

	private class ColorToHex implements Filter {

		@Override
		public List<String> getArgumentNames() {
			return null;
		}

		@Override
		public Object apply(final Object input, final Map<String, Object> map) {
			if (input == null) {
				return null;
			}

			Color color = (Color) input;
			return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		}
	}

	private class GetWallpaperPath implements Function {
		private final boolean forThumbnail;

		GetWallpaperPath(final boolean forThumbnail) {
			this.forThumbnail = forThumbnail;
		}

		@Override
		public List<String> getArgumentNames() {
			return Collections.singletonList("wallpaper");
		}

		@Override
		public Object execute(final Map<String, Object> map) {
			Wallpaper wallpaper = (Wallpaper) map.get("wallpaper");

			if (forThumbnail) {
				return "/wallpapers/small/" + WallpaperUtils.getWallpaperFileName(wallpaper);
			} else {
				return "/wallpapers/full/" + WallpaperUtils.getWallpaperFileName(wallpaper);
			}
		}
	}
}
