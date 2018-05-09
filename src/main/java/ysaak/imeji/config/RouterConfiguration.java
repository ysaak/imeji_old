package ysaak.imeji.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ysaak.imeji.model.Wallpaper;
import ysaak.framework.router.Router;

@Configuration
public class RouterConfiguration {

	@Bean
	public Router router() {
		final Router router = new Router();
		registerStaticRoutes(router);
		registerResources(router);
		return router;
	}

	private void registerStaticRoutes(final Router router) {
		router.registerStaticRoute("root", "/");
		router.registerStaticRoute("help", "/help/");
		router.registerStaticRoute("search", "/search");
	}

	private void registerResources(final Router router) {
		router.registerResource(Wallpaper.class);
	}
}
