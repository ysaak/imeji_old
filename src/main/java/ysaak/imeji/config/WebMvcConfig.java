package ysaak.imeji.config;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring4.PebbleViewResolver;
import com.mitchellbosecke.pebble.spring4.extension.SpringExtension;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import ysaak.framework.annotations.ViewHelper;
import ysaak.framework.router.Router;
import ysaak.imeji.helper.RoutingHelper;
import ysaak.imeji.utils.validation.Validate;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import java.util.List;
import java.util.Set;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
	private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

	private static final String HELPER_PACKAGE = "ysaak.imeji.helper";

	private static final String TEMPLATE_LOCATION = "/templates";

	@Autowired
	private Router router;

	@Value("${pebble.enable-cache}")
	private boolean enableCache;

	@Value("${imeji.wallpaper.path}")
	private String wallpaperLocalPath;

	@Value("${imeji.thumbnail.path}")
	private String thumbnailLocalPath;

	@PostConstruct
	public void checkTemplateLocation() {
		Validate.checkNotNull(getApplicationContext(), "application context");

		TemplateLocation location = new TemplateLocation(TEMPLATE_LOCATION);
		if (!location.exists(getApplicationContext())) {
			logger.warn("Cannot find template location: " + location);
		}
	}

	@Bean
	public Filter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

	@Override
	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
		resolver.setOneIndexedParameters(false);
		resolver.setFallbackPageable(PageRequest.of(0, 20));
		argumentResolvers.add(resolver);
		super.addArgumentResolvers(argumentResolvers);
	}

	@Override
	protected void addResourceHandlers(final ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);

		registry.addResourceHandler("/wallpapers/full/**").addResourceLocations("file:///" + wallpaperLocalPath).setCachePeriod(0);
		registry.addResourceHandler("/wallpapers/small/**").addResourceLocations("file:///" + thumbnailLocalPath).setCachePeriod(0);
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/").setCachePeriod(0);
	}

	@Bean(name="pebbleViewResolver")
	public ViewResolver getPebbleViewResolver(){
		PebbleViewResolver resolver = new PebbleViewResolver();
		resolver.setPrefix(TEMPLATE_LOCATION);
		resolver.setSuffix(".html");
		resolver.setPebbleEngine(pebbleEngine());
		return resolver;
	}

	@Bean
	public PebbleEngine pebbleEngine() {
		PebbleEngine.Builder engineBuilder = new PebbleEngine.Builder()
				.loader(this.templatePebbleLoader())
				.extension(pebbleSpringExtension())
				.extension(customPebbleExtension())
				.extension(new RoutingHelper(router))
				.cacheActive(enableCache);

		loadHelpers(engineBuilder);

		return engineBuilder.build();
	}

	private void loadHelpers(PebbleEngine.Builder engineBuilder) {
		Validate.checkNotNull(getApplicationContext(), "application context");

		Reflections reflections = new Reflections(HELPER_PACKAGE);
		Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(ViewHelper.class);

		for (Class<?> clazz : classSet) {
			if (Extension.class.isAssignableFrom(clazz)) {
				final Extension helper = (Extension) getApplicationContext().getBean(clazz);
				engineBuilder.extension(helper);
			}
		}
	}

	@Bean
	public Loader templatePebbleLoader(){
		return new ServletLoader(getServletContext());
	}

	@Bean
	public SpringExtension pebbleSpringExtension() {
		return new SpringExtension();
	}

	@Bean
	public Extension customPebbleExtension() {
		return new PebbleExtension();
	}
}
