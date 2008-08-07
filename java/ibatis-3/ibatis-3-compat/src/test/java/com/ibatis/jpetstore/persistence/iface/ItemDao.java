package com.ibatis.jpetstore.persistence.iface;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.jpetstore.domain.*;

public interface ItemDao {

  void updateAllQuantitiesFromOrder(Order order);

  boolean isItemInStock(String itemId);

  PaginatedList getItemListByProduct(String productId);

  Item getItem(String itemId);

}
