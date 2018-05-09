package ysaak.imeji.utils;

import com.google.common.base.Preconditions;
import ysaak.imeji.model.Wallpaper;

public final class WallpaperUtils {
	private WallpaperUtils() {
	}

	public static String getWallpaperFileName(Wallpaper wallpaper) {
		Preconditions.checkNotNull(wallpaper, "Wallpaper is null");
		return wallpaper.getId() + "." + wallpaper.getType();
	}
}
