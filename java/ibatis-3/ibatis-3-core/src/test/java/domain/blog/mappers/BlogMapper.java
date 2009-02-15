package domain.blog.mappers;

import java.util.Map;
import java.util.List;

public interface BlogMapper {

  List<Map> selectAllPosts(Object param);
  List<Map> selectAllPosts(int offset, int limit);
  List<Map> selectAllPosts(Object param, int offset, int limit);

}
