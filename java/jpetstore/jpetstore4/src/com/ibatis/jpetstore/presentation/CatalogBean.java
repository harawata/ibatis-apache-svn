package com.ibatis.jpetstore.presentation;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.jpetstore.domain.Category;
import com.ibatis.jpetstore.domain.Item;
import com.ibatis.jpetstore.domain.Product;
import com.ibatis.jpetstore.service.CatalogService;
import com.ibatis.struts.ActionContext;
import com.ibatis.struts.BaseBean;

/**
 * <p/>
 * Date: Mar 9, 2004 10:16:59 PM
 * 
 * @author Clinton Begin
 */
public class CatalogBean extends BaseBean {

  /* Constants */

  private static final CatalogService catalogService = CatalogService.getInstance();

  /* Private Fields */

  private String keyword;
  private String pageDirection;

  private String categoryId;
  private Category category;
  private PaginatedList categoryList;

  private String productId;
  private Product product;
  private PaginatedList productList;

  private String itemId;
  private Item item;
  private PaginatedList itemList;

  /* JavaBeans Properties */

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getPageDirection() {
    return pageDirection;
  }

  public void setPageDirection(String pageDirection) {
    this.pageDirection = pageDirection;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public PaginatedList getCategoryList() {
    return categoryList;
  }

  public void setCategoryList(PaginatedList categoryList) {
    this.categoryList = categoryList;
  }

  public PaginatedList getProductList() {
    return productList;
  }

  public void setProductList(PaginatedList productList) {
    this.productList = productList;
  }

  public PaginatedList getItemList() {
    return itemList;
  }

  public void setItemList(PaginatedList itemList) {
    this.itemList = itemList;
  }

  /* Public Methods */

  public String viewCategory() {
    if (categoryId != null) {
      productList = catalogService.getProductListByCategory(categoryId);
      category = catalogService.getCategory(categoryId);
    }
    return "success";
  }

  public String searchProducts() {
    if (keyword == null || keyword.length() < 1) {
      ActionContext.getActionContext().setSimpleMessage("Please enter a keyword to search for, then press the search button.");
      return "failure";
    } else {
      productList = catalogService.searchProductList(keyword.toLowerCase());
      return "success";
    }
  }

  public String switchProductListPage() {
    if ("next".equals(pageDirection)) {
      productList.nextPage();
    } else if ("previous".equals(pageDirection)) {
      productList.previousPage();
    }
    return "success";
  }

  public String viewProduct() {
    if (productId != null) {
      itemList = catalogService.getItemListByProduct(productId);
      product = catalogService.getProduct(productId);
    }
    return "success";
  }

  public String switchItemListPage() {
    if ("next".equals(pageDirection)) {
      itemList.nextPage();
    } else if ("previous".equals(pageDirection)) {
      itemList.previousPage();
    }
    return "success";
  }

  public String viewItem() {
    item = catalogService.getItem(itemId);
    product = item.getProduct();
    return "success";
  }

  public void clear () {
    keyword = null;
    pageDirection = null;

    categoryId = null;
    category = null;
    categoryList = null;

    productId = null;
    product = null;
    productList = null;

    itemId = null;
    item = null;
    itemList = null;
  }

}
