package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import model.*;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

      // check for connection
      if (dbCon == null) {
          dbCon = new DatabaseController();
      }

      // Build SQL string to query
      String sql = "SELECT \n" +
              "\tuser.id as user_id, user.first_name, user.last_name, user.email, \n" +
              "    o.id as order_id, o.billing_address_id, o.shipping_address_id,\n" +
              "    product.id as product_id,\n" +
              "    line_item.id as line_item_id,\n" +
              "    product.product_name,\n" +
              "    product.price,\n" +
              "    line_item.quantity, \n" +
              "    o.order_total,\n" +
              "    b.street_address as billing_address, b.city as billing_address_city, b.zipcode as billing_address_zipcode,\n" +
              "    s.street_address as shipping_address, s.city as shipping_address_city, s.zipcode as shipping_address_zipcode\n" +
              "    FROM user\n" +
              "\n" +
              "\tinner JOIN orders o ON user.id = o.user_id\n" +
              "    inner join line_item on line_item.order_id = o.id\n" +
              "    inner join product on product.id = line_item.product_id\n" +
              "\tinner join address b on o.billing_address_id=b.id\n" +
              "    inner join address s on o.shipping_address_id=s.id where order_id=" + id;

      // Do the query in the database and create an empty object for the results
      ResultSet rs = dbCon.query(sql);
      Order order = null;

      try {
          if (rs.next()) {

                  User user = new User(
                          rs.getInt("user_id"),
                          rs.getString("first_name"),
                          rs.getString("last_name"),
                          null,
                          rs.getString("email")
                  );

                  Address billing_address = new Address(
                          rs.getInt("billing_address_id"),
                          null,
                          rs.getString("billing_address"),
                          rs.getString("billing_address_city"),
                          rs.getString("billing_address_zipcode")
                  );

                  Address shipping_address = new Address(
                          rs.getInt("shipping_address_id"),
                          null,
                          rs.getString("shipping_address"),
                          rs.getString("shipping_address_city"),
                          rs.getString("shipping_address_zipcode")
                  );

                      ArrayList<LineItem> lineItems = new ArrayList<>();

              order = new Order(
                      rs.getInt("order_id"),
                      user,
                      lineItems,
                      billing_address,
                      shipping_address,
                      rs.getFloat("order_total"),
                      0,
                      0
              );

              do {
                  Product product = new Product(
                          rs.getInt("product_id"),
                          rs.getString("product_name"),
                          null,
                          rs.getInt("price"),
                          null,
                          0
                  );

                  LineItem lineItem = new LineItem(
                          rs.getInt("line_item_id"),
                          product,
                          rs.getInt("quantity"),
                          0
                  );
                  lineItems.add(lineItem);

              }while (rs.next());



              return order;

              } else {
                  System.out.println("No order found");
              }
          } catch(SQLException ex){
              System.out.println(ex.getMessage());
          }

          // Returns null
          return null;
      }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

   String sql = "SELECT \n" +
           "\tuser.id as user_id, user.first_name, user.last_name, user.email, \n" +
           "    o.id as order_id, o.billing_address_id, o.shipping_address_id,\n" +
           "    product.id as product_id,\n" +
           "    line_item.id as line_item_id,\n" +
           "    product.product_name,\n" +
           "    product.price,\n" +
           "    line_item.quantity, \n" +
           "    o.order_total,\n" +
           "    b.street_address as billing_address, b.city as billing_address_city, b.zipcode as billing_address_zipcode,\n" +
           "    s.street_address as shipping_address, s.city as shipping_address_city, s.zipcode as shipping_address_zipcode\n" +
           "    FROM user\n" +
           "\n" +
           "\tinner JOIN orders o ON user.id = o.user_id\n" +
           "    inner join line_item on line_item.order_id = o.id\n" +
           "    inner join product on product.id = line_item.product_id\n" +
           "\tinner join address b on o.billing_address_id=b.id\n" +
           "    inner join address s on o.shipping_address_id=s.id";

    ResultSet rs = dbCon.query(sql);

      Map<Integer, Order> orders = new HashMap<>();

    try {
      while(rs.next()) {


          int orderId = rs.getInt("order_id");

          Order order;
          if (orders.containsKey(orderId)) {
              //gets the order with the order_ID
              order = orders.get(orderId);

              Product product =new Product(
                      rs.getInt("product_id"),
                      rs.getString("product_name"),
                      null,
                      rs.getInt("price"),
                      null,
                      0
              );


              LineItem lineItem = new LineItem(
                      rs.getInt("line_item_id"),
                      product,
                      rs.getInt("quantity"),
                      0
              );
              //add a new lineItem to the order
                order.getLineItems().add(lineItem);


          } else {

              User user = new User(
                      rs.getInt("user_id"),
                      rs.getString("first_name"),
                      rs.getString("last_name"),
                      null,
                      rs.getString("email")
              );

              Address billing_address =new Address(
                      rs.getInt("billing_address_id"),
                      null,
                      rs.getString("billing_address"),
                      rs.getString("billing_address_city"),
                      rs.getString("billing_address_zipcode")
              );

              Address shipping_address = new Address(
                      rs.getInt("shipping_address_id"),
                      null,
                      rs.getString("shipping_address"),
                      rs.getString("shipping_address_city"),
                      rs.getString("shipping_address_zipcode")
              );

              Product product =new Product(
                      rs.getInt("product_id"),
                      rs.getString("product_name"),
                      null,
                      rs.getInt("price"),
                      null,
                      0
              );

              ArrayList<LineItem> lineItems = new ArrayList<>();
              LineItem lineItem = new LineItem(
                      rs.getInt("line_item_id"),
                      product,
                      rs.getInt("quantity"),
                     0
              );
              lineItems.add(lineItem);


              order = new Order(
                      rs.getInt("order_id"),
                      user,
                      lineItems,
                      billing_address,
                      shipping_address,
                      rs.getFloat("order_total"),
                      0,
                      0
              );

              orders.put(orderId, order);
          }

        //TODO: Perhaps we could optimize things a bit here and get rid of nested queries.

      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return new ArrayList<Order>(orders.values());
  }

  public static Order createOrder(Order order) {



      // Write in log that we've reach this step
      Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

      // Set creation and updated time for order.
      order.setCreatedAt(System.currentTimeMillis() / 1000L);
      order.setUpdatedAt(System.currentTimeMillis() / 1000L);

      // Check for DB Connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      // Save addresses to database and save them back to initial order instance
      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      // Save the user to the database and save them back to initial order instance
      order.setCustomer(UserController.createUser(order.getCustomer()));

      // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts. : FIX
        // kilde (try-catch)https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");

      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for (LineItem item : order.getLineItems()) {
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      order.setLineItems(items);



      // Return order
      return order;
    }

}//end class