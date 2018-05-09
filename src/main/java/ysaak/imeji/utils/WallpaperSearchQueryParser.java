package ysaak.imeji.utils;

import com.google.common.base.Splitter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import ysaak.imeji.data.Color;
import ysaak.imeji.data.Rating;
import ysaak.imeji.data.SortMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class WallpaperSearchQueryParser {
	private static final Pattern BETWEEN_PATTERN = Pattern.compile ("^(\\d+)\\.\\.(\\d+)$");
	private static final Pattern GREATER_PATTERN = Pattern.compile ("^>(\\d+)$");
	private static final Pattern GREATER_OR_EQUALS_PATTERN = Pattern.compile ("^>=(\\d+)$");

	private static final Pattern LOWER_PATTERN = Pattern.compile ("^<(\\d+)$");
	private static final Pattern LOWER_OR_EQUALS_PATTERN = Pattern.compile ("^<=(\\d+)$");

	private static final Pattern ORDER_COMMAND_PATTERN = Pattern.compile("^([a-z]+)(_(asc|desc))?$", Pattern.CASE_INSENSITIVE);

	private static Map<String, FilterCommand> FILTER_COMMAND_MAP = new HashMap<>();

	static {
		FILTER_COMMAND_MAP.put("width", new FilterWithOperatorCommand("width"));
		FILTER_COMMAND_MAP.put("height", new FilterWithOperatorCommand("height"));
		FILTER_COMMAND_MAP.put("rating", new RatingFilter());
		FILTER_COMMAND_MAP.put("hash", new SimpleFilterCommand("hash"));
		FILTER_COMMAND_MAP.put("id", new SimpleFilterCommand("id"));
	}

	private QueryBuilder filters;

	private SortMode sortMode = SortMode.DATE;
	private SortOrder sortOrder = SortOrder.DESC;

	private WallpaperSearchQueryParser() {
	}

	public static WallpaperSearchQueryParser parse(String searchQuery, boolean wildcardTags) {
		return new WallpaperSearchQueryParser().doParse(searchQuery, wildcardTags);
	}

	private WallpaperSearchQueryParser doParse(String searchQuery, boolean wildcardTags) {
		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			filters = QueryBuilders.matchAllQuery();
			return this;
		}

		final List<String> tagList = new ArrayList<>();

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

		String color = null;

		final Iterable<String> terms = Splitter.on(" ").split(searchQuery);
		for (String term : terms) {
			if (term.contains(":")) {
				String elements[] = term.split(":", 2);

				FilterCommand command = FILTER_COMMAND_MAP.get(elements[0]);
				if (command != null) {
					Optional<QueryBuilder> filter = command.apply(elements[1]);
					filter.ifPresent(queryBuilder::filter);
				}
				else if ("color".equals(elements[0])) {
					color = elements[1];
				}
				else if ("order".equals(elements[0])) {
					parseOrder(elements[1]);
				}
			} else {
				tagList.add(term);
			}
		}

		if (!tagList.isEmpty()) {
			if (wildcardTags) {
				BoolQueryBuilder tagQuery = QueryBuilders.boolQuery();
				for (String tag : tagList) {
					tagQuery.must(QueryBuilders.wildcardQuery("tags", "*" + tag + "*"));
				}

				queryBuilder.filter(tagQuery);
			} else {
				QueryBuilder query = QueryBuilders.queryStringQuery(String.join(" OR ", tagList)).field("tags");
				queryBuilder.filter(query);
			}
		}

		if (color != null) {
			ScriptScoreFunctionBuilder scoreFunction = ScoreFunctionBuilders.scriptFunction(colorScript(color));
			filters = QueryBuilders.functionScoreQuery(queryBuilder, scoreFunction).setMinScore(1);
		} else {
			filters = queryBuilder;
		}

		return this;
	}

	public SearchQuery buildQuery(Pageable pageable) {
		final NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
				.withPageable(pageable);


		if (sortMode == SortMode.RANDOM) {
			builder.withQuery(randomSortedQuery());
		}
		else {
			builder.withQuery(filters);
		}

		if (sortMode == SortMode.DATE) {
			builder.withSort(SortBuilders.fieldSort("uploadDate").unmappedType("long").order(sortOrder));
		}

		return builder.build();
	}

	private QueryBuilder randomSortedQuery() {
		// Fixme find a way to store random seed for pagination ...
		return QueryBuilders.functionScoreQuery(filters, ScoreFunctionBuilders.randomFunction(10));
	}

	private static QueryBuilder parseHelper(String key, String range) {
		Matcher m = BETWEEN_PATTERN.matcher(range);
		if (m.matches()) {
			return QueryBuilders.rangeQuery(key).from(Integer.parseInt(m.group(1)), true).to(Integer.parseInt(m.group(2)));
		}

		m = GREATER_PATTERN.matcher(range);
		if (m.matches()) {
			return QueryBuilders.rangeQuery(key).from(Integer.parseInt(m.group(1)), false);
		}

		m = GREATER_OR_EQUALS_PATTERN.matcher(range);
		if (m.matches()) {
			return QueryBuilders.rangeQuery(key).from(Integer.parseInt(m.group(1)), true);
		}

		m = LOWER_PATTERN.matcher(range);
		if (m.matches()) {
			return QueryBuilders.rangeQuery(key).to(Integer.parseInt(m.group(1)), false);
		}

		m = LOWER_OR_EQUALS_PATTERN.matcher(range);
		if (m.matches()) {
			return QueryBuilders.rangeQuery(key).to(Integer.parseInt(m.group(1)), true);
		}

		if (range.contains(",")) {
			List<String> values = Splitter.on(",").omitEmptyStrings().splitToList(range);
			return QueryBuilders.termsQuery(key, values.stream().map(Integer::parseInt).collect(Collectors.toList()));
		}

		// Equals
		return QueryBuilders.termQuery(key, Integer.parseInt(range));
	}

	private static Script colorScript(final String color) {
		Color searchColor = new Color( java.awt.Color.decode("#" + color));

		Map<String, Object> colors = new HashMap<>();
		colors.put("red", searchColor.getRed());
		colors.put("green", searchColor.getGreen());
		colors.put("blue", searchColor.getBlue());
		colors.put("xt2", searchColor.getXt2());
		colors.put("xt", searchColor.getXt());

		Map<String, Object> params = new HashMap<>();
		params.put("color", colors);
		params.put("threshold", 40d);
		params.put("deltaBrightness", 40d);

		return new Script(ScriptType.INLINE, "painless", "int colorOk = 0; double iMin = params.color.xt - params.deltaBrightness; double iMax = params.color.xt + params.deltaBrightness; for (color in params._source.palette) { double xv2 = Math.pow(params.color.red * color.red + params.color.green * color.green + params.color.blue * color.blue, 2); double p2 = xv2 / color.xt2; double colorDist = Math.sqrt(params.color.xt2 - p2); if (colorDist <= params.threshold && (iMin <= color.xt && color.xt <= iMax)) { colorOk++; } } return colorOk;", params);
	}

	private void parseOrder(final String order) {
		Matcher matcher = ORDER_COMMAND_PATTERN.matcher(order);
		if (matcher.matches()) {

			try {
				this.sortMode = SortMode.valueOf(matcher.group(1).toUpperCase());
			} catch (IllegalArgumentException e) {
				// TODO Log
				e.printStackTrace();
			}

			if (matcher.group(3) != null) {
				try {
					this.sortOrder = SortOrder.valueOf(matcher.group(3).toUpperCase());
				} catch (IllegalArgumentException e) {
					// TODO Log
					e.printStackTrace();
				}
			}
		}
	}

	private interface FilterCommand  {
		Optional<QueryBuilder> apply(String params);
	}

	private static class FilterWithOperatorCommand implements FilterCommand {
		private final String termKey;

		FilterWithOperatorCommand(String termKey) {
			this.termKey = termKey;
		}

		@Override
		public Optional<QueryBuilder> apply(String params) {
			return Optional.ofNullable(parseHelper(termKey, params));
		}
	}

	private static class SimpleFilterCommand implements FilterCommand {
		private final String termKey;

		SimpleFilterCommand(String termKey) {
			this.termKey = termKey;
		}

		@Override
		public Optional<QueryBuilder> apply(String params) {
			return Optional.of(QueryBuilders.termsQuery(termKey, params));
		}
	}

	private static class RatingFilter implements FilterCommand {
		@Override
		public Optional<QueryBuilder> apply(String params) {
			String lowerRating = params.toLowerCase();

			for (Rating rating : Rating.values()) {
				String r = rating.toString().toLowerCase();

				if (r.equals(lowerRating) || r.substring(0,1).equals(lowerRating)) {
					return Optional.of(QueryBuilders.termQuery("rating", rating));
				}
			}

			return Optional.empty();
		}
	}
}
