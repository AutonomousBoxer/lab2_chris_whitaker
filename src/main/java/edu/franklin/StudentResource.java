/**
 * Purpose: CRUD endpoints for Student.
 * Author: Chris Whitaker
 */
package edu.franklin;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;

import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static jakarta.transaction.Transactional.TxType.SUPPORTS;

@Path("/api/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {
    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{id}")
    @Transactional(SUPPORTS)
    @Tag(name = "Read")
    @Operation(summary = "Get a student by id", description = "Returns the student if found.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "OK"),
            @APIResponse(responseCode = "404", description = "Not found")
    })
    public Response getStudentById(@RestPath @Min(1) Long id) {
        Student student = Student.findById(id);
        if (student == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(student).link(uriInfo.getRequestUri(), "self").build();
    }

    @GET
    @Path("/search/{name}")
    @Transactional(SUPPORTS)
    @Tag(name = "Read")
    @Operation(summary = "Get a student by name", description = "Returns the student if found.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "OK"),
            @APIResponse(responseCode = "404", description = "Not found")
    })
    public Response getStudentByname(@RestPath @NotBlank String name) {
        // Convert name column and the first argument to lower, compare, and return the first match.
        Student student = Student.find("LOWER(name) = LOWER(?1)", name).firstResult();
        if (student == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(student).link(uriInfo.getRequestUri(), "self").build();
    }

    @GET
    @Transactional(SUPPORTS)
    @Tag(name = "Read")
    @Operation(summary = "Get all students", description = "Returns a list of all students.")
    @APIResponse(responseCode = "200", description = "OK")
    public Response getAllStudents() {
        return Response.ok(Student.listAll()).link(uriInfo.getRequestUri(), "self").build();
    }

    @GET
    @Path("/random")
    @Transactional(SUPPORTS)
    @Tag(name = "Read")
    @Operation(summary = "Get a random student", description = "Returns a random student if found.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "OK"),
            @APIResponse(responseCode = "404", description = "Not found")
    })
    public Response getRandomStudent() {
        // Order by db random() and return the first result.
        Student student = Student.find("order by function('random')").firstResult();
        if (student == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(student).link(uriInfo.getRequestUri(), "self").build();
    }

    @POST
    @Transactional(REQUIRED)
    @Tag(name = "Create")
    @Operation(summary = "Create a new student", description = "Accepts JSON without id.")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Created"),
            @APIResponse(responseCode = "400", description = "Bad request")
    })
    public Response createStudent(@Valid Student student) {
        // Reject supplied id.
        if (student.id != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Don't include an id when creating a student.").type(MediaType.TEXT_PLAIN).build();
        }

        // Assign id on persist
        student.persist();

        // Build location based on current uri.
        return Response.created(uriInfo.getBaseUriBuilder().path(StudentResource.class).path(student.id.toString()).build()).entity(student).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional(REQUIRED)
    @Tag(name = "Delete")
    @Operation(summary = "Delete a student by id", description = "Deletes the student with the given id.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Response deleteStudent(@RestPath @Min(1) Long id) {
        boolean deleted = Student.deleteById(id);
        if (deleted) {
            Response.noContent().link(uriInfo.getRequestUri(), "self").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional(REQUIRED)
    @Tag(name = "Update")
    @Operation(summary = "Update a student by id", description = "Updates the student identified by the URL id. The id cannot be changed.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "OK"),
            @APIResponse(responseCode = "400", description = "Bad request"),
            @APIResponse(responseCode = "404", description = "Not found")
    })
    public Response updateStudent(@RestPath @Min(1) Long id, @Valid Student student) {
        // Prevent id changes/mismatches.
        if (student.id != null && !student.id.equals(id)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Path id and body id must match.").type(MediaType.TEXT_PLAIN).build();
        }

        Student existing = Student.findById(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Update target fields.
        existing.name = student.name;
        existing.phone = student.phone;
        existing.grade = student.grade;
        existing.license = student.license;

        return Response.ok(existing).link(uriInfo.getRequestUri(), "self").build();
    }
}
