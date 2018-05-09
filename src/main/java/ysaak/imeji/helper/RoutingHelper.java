package ysaak.imeji.helper;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;
import org.springframework.beans.factory.annotation.Autowired;
import ysaak.framework.annotations.ViewHelper;
import ysaak.framework.router.Router;
import ysaak.framework.data.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewHelper
public class RoutingHelper extends AbstractExtension {
	private final Router router;

	private final Map<String, Object> staticRouteMap;
	private final Map<String, Function> routeWithIdMap;

	@Autowired
	public RoutingHelper(final Router router) {
		this.router = router;

		staticRouteMap = new HashMap<>();
		routeWithIdMap = new HashMap<>();

		Map<String, String> routeMap = router.getRouteMap();
		if (routeMap != null) {
			for (Map.Entry<String, String> route : routeMap.entrySet()) {
				if (route.getValue().contains(Router.ID_PLACEHOLDER)) {
					routeWithIdMap.put(route.getKey(), new RouteWithIdFunction(route.getKey()));
				} else {
					staticRouteMap.put(route.getKey(), route.getValue());
				}
			}
		}
	}

	@Override
	public Map<String, Object> getGlobalVariables() {
		return staticRouteMap;
	}

	@Override
	public Map<String, Function> getFunctions() {
		return routeWithIdMap;
	}

	private class RouteWithIdFunction implements Function {

		private final String name;

		private RouteWithIdFunction(final String name) {
			this.name = name;
		}

		@Override
		public List<String> getArgumentNames() {
			return Collections.singletonList("id");
		}

		@Override
		public Object execute(final Map<String, Object> map) {
			Object resource = map.get("id");
			final String id;

			if (resource instanceof Resource) {
				id = ((Resource) resource).getId();
			} else {
				id = resource.toString();
			}

			return router.getPath(name, id);
		}
	}
}
