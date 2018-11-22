package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.gson.Gson;
import controllers.UserController;

import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.User;
import utils.Config;
import utils.Encryption;
import utils.Hashing;
import utils.Log;

import java.security.Key;
import java.util.Date;

@Path("user")
public class UserEndpoints {
    UserCache userCache = new UserCache();

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
            } else {
                return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
            }
        } catch (Exception e) {
            return Response.status(404).build();
        }
    }


    /**
     * @return Responses
     */
    @GET
    @Path("/")
    public Response getUsers() {

        // Write to log that we are here
        Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

        // Get a list of users
        ArrayList<User> users = userCache.getUsers(false);

        // TODO: Add Encryption to JSON : FIX
        // Transfer users to json in order to return it to the user
        String json = new Gson().toJson(users);
        json = Encryption.encryptDecryptXOR(json);
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

        //Updates the user cache when a new user is created
        userCache.getUsers(true);

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

    // TODO: Make the system able to login users and assign them a token to use throughout the system. : FIX
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginUser(String userDataToValidate) {

        //Saves the data from the browser (userDataToValidate) and transfers it to a user class (userToValidate)
        User userToValidate = new Gson().fromJson(userDataToValidate, User.class);

        //Gets the email and password of the user trying to login, and sends it to checkuser method in UserController.
        //Hashes the password as well as passwords in DB are hashed strings, so we have no plain text passwords
        User checkedUser = UserController.checkUser(userToValidate.getEmail(), Hashing.md5(userToValidate.getPassword())); //TODO when should we hash password (endpoint or controller)
        //Hashing.md5(userToValidate.getPassword()));

        try {
            if (checkedUser != null) {

                Algorithm algorithm = Algorithm.HMAC256(Config.getTokenSecret());
                String token = JWT.create()
                        .withIssuer("auth0")
                        .withIssuedAt(new Date(System.currentTimeMillis()))
                        .withExpiresAt(new Date(System.currentTimeMillis() + 900000)) //15 minutes
                        .withSubject(Integer.toString(checkedUser.getId())) //Sets the token subject to the users ID. used later for verification
                        .sign(algorithm);

                checkedUser.setToken(token);
                String json = new Gson().toJson(token);

                return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You are now logged in. Your Token is as follows \n" + json).build();
            } else {
                return Response.status(400).entity("Access denied. Email or password is wrong. Please try again").build();
            }
        } catch (JWTCreationException exception) {
            return null;
        }
    }


    // TODO: Make the system able to delete users : FIX
    @POST
    @Path("/deleteUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(String body) {

        // Read the json from body and transfer it to a user class
        User chosenUser = new Gson().fromJson(body, User.class);

        try {
            //Verifies if the token coresponds to the user trying to execute delete statement
            if (verifyToken(chosenUser.getToken(), chosenUser)) {

                UserController.deleteUser((chosenUser.getId()));

                //Updates the user cache when a user is deleted
               userCache.getUsers(true);

                return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(" User with the UserID " + chosenUser.getId() + " has been removed ").build();

            } else {
                //Print error message if user is not found
                return Response.status(400).entity("Error: Wrong ID or Token. Please try again").build();
            }
        } catch (NullPointerException e) {
            return Response.status(400).build();
        }
    }


    // TODO: Make the system able to update users :FIX
    @PUT
    @Path("/updateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(String browserData) {

        // Read the json from body and transfer it to a user class
        User userDataToUpdate = new Gson().fromJson(browserData, User.class);
        //get the token from the browserData
        String token = userDataToUpdate.getToken();
        //get the id from the browserData
        int idUser = userDataToUpdate.getId();

        try {
            //Verifies if the token coresponds to the user trying to execute update statement
            if (verifyToken(token, userDataToUpdate)) {
                //checks if the field values are empty. We do not wish to update if empty
                if (userDataToUpdate.getFirstname().isEmpty() || userDataToUpdate.getLastname().isEmpty() || userDataToUpdate.getPassword().isEmpty() || userDataToUpdate.getEmail().isEmpty()) {
                    return Response.status(400).entity("Please make sure that what you wish to update is correct, and that they all have values. They can't be empty. Should you not wish to update something, please enter the same value again").build();

                } else {
                    //updates the user with the new data from the borwser/postman and saves it in a new user object
                    User updatedUser = UserController.updateUser(userDataToUpdate, idUser);

                    String json = new Gson().toJson(updatedUser);

                    //Updates the user cache when a user is updated
                    userCache.getUsers(true);

                    // Return the data to the user
                    // Return a response with status 200 and JSON as type
                    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User has been updated to the following: " + json).build();
                }
            } else {
                return Response.status(400).entity("Could not verify Token. Please try again").build();
            }
        } catch (NullPointerException e) {
            return Response.status(400).build();
        }
    }


    //Test token verification method
 /* @POST
  @Path("/verifyTester")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response verifyUserTester(String token){

    User user1 =new Gson().fromJson(token, User.class);

    if (verifyToken(user1.getToken())){
      return Response.status(200).entity("Din token virker ").build();
    }

   return Response.status(400).entity("Din token er udløbet").build();
  }*/


    //Verify token
    private boolean verifyToken(String token, User user) {
        try {

            Algorithm algorithm = Algorithm.HMAC256(Config.getTokenSecret());
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .withSubject(Integer.toString(user.getId()))    //hænger users id i token ikke sammen med det id på den user vi sender med, så sletter vi ikke
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

}

