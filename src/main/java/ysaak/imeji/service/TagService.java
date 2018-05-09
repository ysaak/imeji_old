package ysaak.imeji.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ysaak.imeji.model.Tag;
import ysaak.imeji.data.TagType;
import ysaak.imeji.repository.TagRepository;
import ysaak.imeji.utils.CollectionUtils;
import ysaak.imeji.utils.validation.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TagService {

	private static final String ALLOWED_CHAR_PATTERN_WITH_MODIFIER = "[^a-z0-9_/\\-() :]";
	private static final String COMMAND_SEPARATOR = ":";

	private static final Map<String, TagType> CODE_TO_TYPE_MAP;
	static {
		CODE_TO_TYPE_MAP = new HashMap<>();
		CODE_TO_TYPE_MAP.put("character", TagType.CHARACTER);
		CODE_TO_TYPE_MAP.put("char", TagType.CHARACTER);
		CODE_TO_TYPE_MAP.put("circle", TagType.CIRCLE);
		CODE_TO_TYPE_MAP.put("copyright", TagType.COPYRIGHT);
		CODE_TO_TYPE_MAP.put("copy", TagType.COPYRIGHT);
		CODE_TO_TYPE_MAP.put("style", TagType.STYLE);
		CODE_TO_TYPE_MAP.put("general", TagType.GENERAL);
	}


	private final TagRepository tagRepository;

	private final Client esClient;

	@Autowired
	public TagService(TagRepository tagRepository, Client esClient) {
		this.tagRepository = tagRepository;
		this.esClient = esClient;
	}

	public List<Tag> createTags(String tagCode) {
		Validate.checkNotNull(tagCode, "tagCode");

		final String cleanedList = tagCode.toLowerCase().replaceAll(ALLOWED_CHAR_PATTERN_WITH_MODIFIER, "");
		if (StringUtils.isBlank(tagCode)) {
			return Collections.emptyList();
		}

		final List<String> tagCodeList = Splitter.on(" ").omitEmptyStrings().trimResults().splitToList(cleanedList);

		final Map<String, TagType> tagWithCommand = new HashMap<>();

		final List<String> cleanedTagCodeList = new ArrayList<>();

		for (String tag : tagCodeList) {
			if (tag.contains(COMMAND_SEPARATOR)) {
				String tagParts[] = tag.split(COMMAND_SEPARATOR, 2);

				final TagType type = CODE_TO_TYPE_MAP.get(tagParts[0]);
				final String code = tagParts[1].replaceAll(COMMAND_SEPARATOR, "");

				if (type != null) {
					tagWithCommand.put(code, type);
				}
				cleanedTagCodeList.add(code);
			} else {
				cleanedTagCodeList.add(tag.replaceAll(":", ""));
			}
		}

		final List<Tag> tagList = new ArrayList<>();

		if (CollectionUtils.notEmpty(tagCodeList)) {
			final Set<String> tagSet = new HashSet<>(cleanedTagCodeList);

			final List<Tag> storedTags = findByCode(tagSet);
			if (storedTags != null) {
				final List<Tag> tagToUpdate = new ArrayList<>();

				for (Tag tag : storedTags) {
					tagSet.remove(tag.getCode());

					// Check if tag type need to be updated
					if (tagWithCommand.containsKey(tag.getCode())) {
						TagType newType = tagWithCommand.get(tag.getCode());
						if (tag.getType() != newType) {
							tag.setType(newType);
							tagToUpdate.add(tag);
						}
					}
				}
				tagList.addAll(storedTags);

				if (CollectionUtils.notEmpty(tagToUpdate)) {
					tagRepository.saveAll(tagToUpdate);
				}
			}

			if (!tagSet.isEmpty()) {
				final List<Tag> tagsToCreate = new ArrayList<>();
				for (String code : tagSet) {
					Tag tag = new Tag(code);
					if (tagWithCommand.containsKey(tag.getCode())) {
						tag.setType(tagWithCommand.get(tag.getCode()));
					}
					tagsToCreate.add(tag);
				}

				final Iterable<Tag> createdTags = tagRepository.saveAll(tagsToCreate);
				tagList.addAll(Lists.newArrayList(createdTags));
			}
		}

		return tagList;
	}
	public void incrementCounter(List<String> tagCodeList) {
		counterUpdateQuery(tagCodeList, true);
	}

	public void decrementCounter(List<String> tagCodeList) {
		counterUpdateQuery(tagCodeList, false);
	}

	private void counterUpdateQuery(List<String> tagCodeList, boolean increment) {
		Validate.checkNotNull(tagCodeList, "tagCodeList");
		Validate.isFalse(tagCodeList.isEmpty(), "tagCodeList", "Tag code list is empty");

		final String scriptCode = (increment) ? "ctx._source.wallpaperCount++" : "ctx._source.wallpaperCount--";

		final Script script = new Script(ScriptType.INLINE, "painless", scriptCode, Collections.emptyMap());
		final BoolQueryBuilder filter = QueryBuilders.boolQuery();
		for (String code : tagCodeList) {
			filter.should(QueryBuilders.termQuery("code", code));
		}

		UpdateByQueryRequestBuilder requestBuilder = UpdateByQueryAction.INSTANCE.newRequestBuilder(esClient);
		requestBuilder.script(script).filter(filter).refresh(true);
		requestBuilder.get();
	}

	public List<Tag> findByCode(Collection<String> codeList) {
		if (codeList == null || codeList.isEmpty()) {
			return Collections.emptyList();
		}

		final BoolQueryBuilder filter = QueryBuilders.boolQuery();
		for (String code : codeList) {
			filter.should(QueryBuilders.termQuery("code", code));
		}

		Iterable<Tag> tagList = tagRepository.search(filter);
		return Lists.newArrayList(tagList);
	}

	public List<Tag> findNonGeneralByCode(Collection<String> codeList) {
		if (codeList == null || codeList.isEmpty()) {
			return Collections.emptyList();
		}

		final BoolQueryBuilder filter = QueryBuilders.boolQuery();
		final BoolQueryBuilder codeFilter = QueryBuilders.boolQuery();
		for (String code : codeList) {
			codeFilter.should(QueryBuilders.termQuery("code", code));
		}
		filter.must(codeFilter);
		filter.mustNot(QueryBuilders.termQuery("type.keyword", TagType.GENERAL.toString()));

		Iterable<Tag> tagList = tagRepository.search(filter);
		return Lists.newArrayList(tagList);
	}
}
