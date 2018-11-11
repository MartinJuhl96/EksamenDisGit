package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    String sql = "SELECT * FROM orders where id=" + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an object instance of order from the database dataa
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
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
    // TODO: typo missing 's' in orders : FIX

      String sql = "SELECT \n" +
              "\tuser.id as user_id, user.first_name, user.last_name, user.email, \n" +
              "    o.id as order_id, o.billing_address_id, o.shipping_address_id,\n" +
              "    product.product_name,\n" +
              "    product.price,\n" +
              "    line_item.quantity,\n" +
              "    o.order_total,\n" +
              "    b.street_address as billing_address, b.city as billing_address_city, b.zipcode as billing_address_zipcode,\n" +
              "    s.street_address as shipping_address, s.city as shipping_address_city, s.zipcode as shipping_address_zipcode\n" +
              "    FROM user\n" +
              "\n" +
              "\tinner JOIN orders o ON user.id = o.user_id\n" +
              "    inner join line_item on line_item.order_id = o.id\n" +
              "    inner join product on product.id = line_item.product_id\n" +
              "\tinner join address b on o.billing_address_id=b.id\n" +
              "    inner join address s on o.shipping_address_id=s.id;";

      /*  String sql = "SELECT \n" +
                "\tuser.id as user_id, user.first_name, user.last_name, user.email, \n" +
                "    orders.id as order_id, orders.billing_address_id, orders.shipping_address_id,\n" +
                "    product.product_name,\n" +
                "    product.price,\n" +
                "    line_item.quantity,\n" +
                "    orders.order_total\n" +
                "    FROM user\n" +
                "    inner JOIN orders ON user.id = orders.user_id\n" +
                "    inner join line_item on line_item.order_id = orders.id\n" +
                "    inner join product on product.id = line_item.product_id;";
*/

      // String sql = "SELECT * FROM orders INNER jOIN first_name, last_name, email FROM user ON ";

    ResultSet rs = dbCon.query(sql);
   // ArrayList<Order> orders = new ArrayList<Order>();


      Map<Integer, Order> orders = new HashMap<>();

    try {
      while(rs.next()) {


          int orderId = rs.getInt("order_id");

          Order order;
          if (orders.containsKey(orderId)) {
              order = orders.get(orderId);
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
                      null,     //bør vi printe navn? det bliver vel oprettet når brugeren indtaster ordren
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
                      0,
                      rs.getString("product_name"),
                      null,
                      rs.getInt("price"),
                      null,
                      0
              );

              ArrayList<LineItem> lineItems = new ArrayList<>();
              LineItem lineItem = new LineItem(
                      0,
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


        // Create an order from the database data
     /*  Order order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));*/




        // Add order to our list
  //      orders.add(order);

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

      try{
        DatabaseController.getConnection().setAutoCommit(false);
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
      DatabaseController.getConnection().commit();


      // Return order
      return order;
    }
    catch (SQLException e){
      System.out.println(e.getMessage());
      //Chech if database connection is closed
      if (dbCon != null){
        try {
          System.out.println("Transaction is being rolled back");
          DatabaseController.getConnection().rollback();
        }catch (SQLException e1){
          System.out.println(e1.getMessage());
        }
      }
    }
    finally {
          try {
              DatabaseController.getConnection().setAutoCommit(true);
          }catch (SQLException e){
              System.out.println(e.getMessage());
          }
          }
    return null;

  }
}//end class