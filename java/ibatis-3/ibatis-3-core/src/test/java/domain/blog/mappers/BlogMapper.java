package domain.blog.mappers;

import java.util.List;
import java.util.Map;

public interface BlogMapper {

  List<Map> selectAllPosts(Object param);

  List<Map> selectAllPosts(int offset, int limit);

  List<Map> selectAllPosts(Object param, int offset, int limit);

}
