package ysaak.imeji.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ysaak.imeji.model.Tag;

@Repository
public interface TagRepository extends ElasticsearchRepository<Tag, String> {
}
