package ysaak.imeji.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ysaak.imeji.model.Wallpaper;

import java.util.List;

@Repository
public interface WallpaperRepository extends ElasticsearchRepository<Wallpaper, String> {

	List<Wallpaper> findByHash(String hash);
}
