package com.robsonbs.controller;

import com.robsonbs.dao.UserDao;
import com.robsonbs.model.User;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/users")
public class UserController {

    @Inject
    UserDao userDao;

    @Inject
    Template users;

    @Inject
    Template userForm;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listUsers() {
        List<User> allUsers = userDao.findAll();
        return users.data("users", allUsers);
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance newUserForm() {
        return userForm.data("user", null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createUser(@FormParam("name") String name, @FormParam("email") String email) {
        User user = new User(name, email);
        userDao.save(user);
        return Response.seeOther(URI.create("/users")).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getUser(@PathParam("id") Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new NotFoundException();
        }
        return userForm.data("user", user);
    }

    @POST
    @Path("/{id}/delete")
    public Response deleteUser(@PathParam("id") Long id) {
        userDao.delete(id);
        return Response.seeOther(URI.create("/users")).build();
    }

    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> listUsersJson() {
        return userDao.findAll();
    }
}
