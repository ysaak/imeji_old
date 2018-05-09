package ysaak.framework.router;

import ysaak.framework.data.Resource;
import ysaak.imeji.utils.Inflections;

import java.util.HashMap;
import java.util.Map;

public class Router {
	public static final String ID_PLACEHOLDER = "{id}";

	private static final String NEW_PREFIX = "new_";
	private static final String EDIT_PREFIX = "edit_";
	private static final String PATH_SUFFIX = "_path";

	private final Map<String, String> routeMap = new HashMap<>();

	public void registerStaticRoute(String name, String route) {
		routeMap.put(name + PATH_SUFFIX, route);
	}

	public void registerResource(Class<? extends Resource> resourceClass) {
		if (resourceClass != null) {

			final String resourceName = resourceToName(resourceClass);
			final String pluralResourceName = Inflections.plurialize(resourceName);

			registerStaticRoute(NEW_PREFIX + resourceName, "/" + pluralResourceName + "/new");
			registerStaticRoute(pluralResourceName, "/" + pluralResourceName);
			registerStaticRoute(resourceName, "/" + pluralResourceName + "/" + ID_PLACEHOLDER);
			registerStaticRoute(EDIT_PREFIX + resourceName, "/" + pluralResourceName + "/" + ID_PLACEHOLDER + "/edit");
		}
	}

	private String resourceToName(Class<? extends Resource> resourceClass) {
		return Inflections.underscore(resourceClass.getSimpleName().toLowerCase());
	}

	public String getPath(Object object) {
		if (object == null) {
			throw new NullPointerException("object is null");
		}

		if (object instanceof String) {
			return getPath(object + PATH_SUFFIX, null);
		} else if (object instanceof Resource) {
			final Resource resource = (Resource) object;
			return getPath(resourceToName(resource.getClass()) + PATH_SUFFIX, resource.getId());
		} else {
			throw new IllegalArgumentException("object must be a route name or a resource object");
		}
	}

	public String getPath(String name, String id) {
		String path = routeMap.get(name);

		if (path == null) {
			throw new IllegalArgumentException("'" + name + "' is not a valid route name");
		}

		if (id != null && path.contains(ID_PLACEHOLDER)) {
			path = path.replace(ID_PLACEHOLDER, id);
		}

		return path;
	}

	public Map<String, String> getRouteMap() {
		return routeMap;
	}
}
