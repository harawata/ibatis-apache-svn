package testdomain;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {

  private String categoryId;
  private String name;
  private String description;
  private List productList;

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List getProductList() {
    return productList;
  }

  public void setProductList(List productList) {
    this.productList = productList;
  }
}
