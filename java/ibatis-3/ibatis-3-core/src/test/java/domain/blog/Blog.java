package domain.blog;

import java.util.List;

public class Blog {

  private int id;
  private Author author;
  private String title;
  private List<Post> posts;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Author getAuthor() {
    return author;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Post> getPosts() {
    return posts;
  }

  public void setPosts(List<Post> posts) {
    this.posts = posts;
  }
}
