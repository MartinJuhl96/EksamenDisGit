package com.cbsexam;

import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    try {
      // Use the ID to get the user from the controller.
      User user = UserController.getUser(idUser);

      // TODO: Add Encryption to JSON : FIX
      // Convert the user object to json in order to return the object
      String json = new Gson().toJson(user);
      json = Encryption.encryptDecryptXOR(json);
      // Return the user with the status code 200
      // TODO: What should happen if something breaks down? : FIX for now
      if (user == null) {
        return Response.status(404).entity("User not found").build();
      }else {
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      }
    }catch(Exception e){
        return Response.status(404).build();
      }
    }



  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserController.getUsers();

    // TODO: Add Encryption to JSON : FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    json=Encryption.encryptDecryptXOR(json);
    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {
  
    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }


  // TODO: Make the system able to delete users : FIX for NOW
  @POST
  @Path("/deleteUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(String userID) {

// Read the json from body and transfer it to a user class
    User chosenUserID = new Gson().fromJson(userID, User.class);

   if (doesUserExist(chosenUserID.getId())){

     UserController.deleteUser((chosenUserID.getId()));

     return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User with the UserID " +chosenUserID.getId()+ "has been removed").build();


   }else {
     //Print error message if user is not found
     return Response.status(400).entity("User not found").build();
   }
  }
  //Get the user if it exists
  public boolean doesUserExist(int userID){
    return UserController.getUser(userID) != null;
}

  // TODO: Make the system able to update users :FIX for now
  @POST
  @Path("/updateUser/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("idUser") int idUser, String browserData) {

    //Henter brugeren ud på id
   User chosenUser = UserController.getUser(idUser);

   //Henter data indtastet i browseren og gemmer det i user objektet userDataToUpdate
   User userDataToUpdate=new Gson().fromJson(browserData, User.class);

  //Kalder usercontrolleren og sender userid og browserdata med over i usercontrolleren.
   UserController.updateUser(userDataToUpdate, idUser);

   //Henter brugeres ID med så det skrives ud
   User gettingUserId=UserController.getUser(idUser);

   String json=new Gson().toJson(gettingUserId);

    // Return the data to the user
    if (chosenUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not update user").build();
    }


  //bør laves med token for user dre er logget ind, for så kan vi hente den token og bruge den i stedet for id til at opdatere
    // Return a response with status 200 and JSON as type
  //  return Response.status(400).entity("Endpoint not implemented yet").build();
  }
}
