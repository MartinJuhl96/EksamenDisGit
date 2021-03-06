package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                null,
                    //    rs.getString("password"),
                rs.getString("email"));



        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                null,
                //rs.getString("password"),
                rs.getString("email"));


        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. : FIX
    //Hashes and sets the password here to aviod printing it in plain text
   user.setPassword(Hashing.md5(user.getPassword()));
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + user.getPassword()
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }
    // Return user
    return user;
  }

  public static void deleteUser(int userID){

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), userID, "Actually deleting user from database", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Delete DB user
    dbCon.delete("DELETE FROM user WHERE id=" +userID);
    }



    //update user method
    public static User updateUser(User userDataToUpdate, int idUser) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), userDataToUpdate, "Updating user in the database", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
      //hash the password before updating.
     userDataToUpdate.setPassword(Hashing.md5(userDataToUpdate.getPassword()));
      // Update the user in the DB
      dbCon.update(
               "UPDATE user SET first_name='"+userDataToUpdate.getFirstname()+"', last_name='"+userDataToUpdate.getLastname()+"', password='"+userDataToUpdate.getPassword()+"', email='"+userDataToUpdate.getEmail()+"' WHERE id='"+idUser+"'");


      // Return user
      return userDataToUpdate;
  }

  //checks if the user is in the system
  public static User checkUser(String email, String passWord) {

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }


    String sql_statement = "SELECT * FROM user WHERE email='" + email + "' AND password='" + passWord+"'";
    ResultSet resultSet = dbCon.query(sql_statement);

    //dbCon.query("SELECT * FROM user WHERE first_name='" + firstName + "', password='" + passWord);

    User user = null;
    try {
      while (resultSet.next()) {
        user = new User(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("password"),
                resultSet.getString("email"));


      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }


    return user;
  }

}

