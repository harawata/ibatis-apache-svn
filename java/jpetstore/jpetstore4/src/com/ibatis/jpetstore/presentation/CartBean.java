package com.ibatis.jpetstore.presentation;

import com.ibatis.jpetstore.domain.Cart;
import com.ibatis.jpetstore.domain.CartItem;
import com.ibatis.jpetstore.domain.Item;
import com.ibatis.jpetstore.service.CatalogService;
import com.ibatis.struts.ActionContext;
import com.ibatis.struts.BaseBean;

import java.util.Iterator;
import java.util.Map;

public class CartBean extends BaseBean {

  /* Constants */

  private static final CatalogService catalogService = CatalogService.getInstance();

  /* Private Fields */

  private Cart cart = new Cart();
  private String workingItemId;
  private String pageDirection;

  /* JavaBeans Properties */

  public Cart getCart() {
    return cart;
  }

  public void setCart(Cart cart) {
    this.cart = cart;
  }

  public String getWorkingItemId() {
    return workingItemId;
  }

  public void setWorkingItemId(String workingItemId) {
    this.workingItemId = workingItemId;
  }

  public String getPageDirection() {
    return pageDirection;
  }

  public void setPageDirection(String pageDirection) {
    this.pageDirection = pageDirection;
  }

  /* Public Methods */

  public String addItemToCart() {
    if (cart.containsItemId(workingItemId)) {
      cart.incrementQuantityByItemId(workingItemId);
    } else {
      // isInStock is a "real-time" property that must be updated
      // every time an item is added to the cart, even if other
      // item details are cached.
      boolean isInStock = catalogService.isItemInStock(workingItemId);
      Item item = catalogService.getItem(workingItemId);
      cart.addItem(item, isInStock);
    }

    return "success";
  }

  public String removeItemFromCart() {

    Item item = cart.removeItemById(workingItemId);

    if (item == null) {
      ActionContext.getActionContext().setSimpleMessage("Attempted to remove null CartItem from Cart.");
      return "failure";
    } else {
      return "success";
    }
  }

  public String updateCartQuantities() {
    Map parameterMap = ActionContext.getActionContext().getParameterMap();

    Iterator cartItems = getCart().getAllCartItems();
    while (cartItems.hasNext()) {
      CartItem cartItem = (CartItem) cartItems.next();
      String itemId = cartItem.getItem().getItemId();
      try {
        int quantity = Integer.parseInt((String) parameterMap.get(itemId));
        getCart().setQuantityByItemId(itemId, quantity);
        if (quantity < 1) {
          cartItems.remove();
        }
      } catch (Exception e) {
        //ignore on purpose
      }
    }

    return "success";
  }

  public String switchCartPage() {
    if ("next".equals(pageDirection)) {
      cart.getCartItemList().nextPage();
    } else if ("previous".equals(pageDirection)) {
      cart.getCartItemList().previousPage();
    }
    return "success";
  }

  public String viewCart() {
    return "success";
  }

  public void clear() {
    cart = new Cart();
    workingItemId = null;
    pageDirection = null;
  }

}
