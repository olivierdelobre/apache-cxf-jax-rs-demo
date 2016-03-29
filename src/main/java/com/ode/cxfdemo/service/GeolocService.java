package com.ode.cxfdemo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.ResponseWrapper;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ode.cxfdemo.model.Position;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.oauth.Authenticators;
import com.stormpath.sdk.oauth.Oauth2Requests;
import com.stormpath.sdk.oauth.OauthGrantAuthenticationResult;
import com.stormpath.sdk.oauth.PasswordGrantRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/") 
@Api(value = "/", description = "Geoloc service")
public class GeolocService {
	
	private Client mClient = Clients.builder().build();
	private Application mApplication =  mClient.getResource("https://api.stormpath.com/v1/applications/2L1DcJDyemjmJNPMMVctos", Application.class);
	
	
	@PermitAll
	@Path("/hello") 
	@Produces({ MediaType.TEXT_HTML })
    @GET
    @ApiOperation(
        value = "Say Hello to Olivier", 
        notes = "Get operation with Response and @Default value"
    )
    @ApiResponses(value = {
	    	@ApiResponse(code = 500, message = "Server error"),
	    	@ApiResponse(code = 200, message = "OK")
	    })
    public Response sayHello() {
        return Response.ok("Hello Olivier").build();
    }
	
	
	
	
	@PermitAll
	@Path("/oauth/token") 
	@Produces({ MediaType.APPLICATION_JSON })
    @POST
    @ApiOperation(
        value = "Get OAuth2 token", 
        notes = "Get operation with Response and @Default value"
    )
    @ApiResponses(value = {
	    	@ApiResponse(code = 500, message = "Server error"),
	    	@ApiResponse(code = 200, message = "OK")
	    })
    public Response getToken(
    		@ApiParam(value = "username", required = true) @FormParam("username") String username,
    		@ApiParam(value = "password", required = true) @FormParam("password") String password) {
		
		PasswordGrantRequest passwordGrantRequest = Oauth2Requests.PASSWORD_GRANT_REQUEST.builder()
			.setLogin(username)
			.setPassword(password)
			.build();
		
		OauthGrantAuthenticationResult oauthGrantAuthenticationResult = Authenticators.PASSWORD_GRANT_AUTHENTICATOR
				  .forApplication(mApplication)
				  .authenticate(passwordGrantRequest);
		
		JSONObject obj = new JSONObject();
		
		if (oauthGrantAuthenticationResult != null) {
			try {
				obj.append("access_token", oauthGrantAuthenticationResult.getAccessTokenString())
					.append("refresh_token", oauthGrantAuthenticationResult.getRefreshTokenString())
					.append("token_type", oauthGrantAuthenticationResult.getTokenType())
					.append("expires_in", oauthGrantAuthenticationResult.getExpiresIn())
					.append("stormpath_access_token_href", oauthGrantAuthenticationResult.getAccessTokenHref());
			} catch (JSONException jsonex) {
				jsonex.printStackTrace();
				return Response.serverError().build();
			}
		}
		else {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
        return Response.ok(obj.toString()).build();
    }
	
	
	
	@RolesAllowed("USER_AUTHENTICATED")
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/positions")
    @GET
    @ApiOperation(
        value = "Get all positions", 
        notes = "Get operation with Response and @Default value", 
        response = List.class, 
        responseContainer = "List"
    )
    @ApiResponses(value = {
	    	@ApiResponse(code = 500, message = "Server error"),
	    	@ApiResponse(code = 200, message = "OK")
	    })
    public Response getPositions (
        @ApiParam(value = "Page to fetch", required = true) @QueryParam("page") @DefaultValue("1") int page) {

        return Response.ok(new ArrayList<Position>()).build();
    }
    
    
    
    
	@RolesAllowed("USER_AUTHENTICATED")
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/positions/{id}")
    @GET
    @ApiOperation(
        value = "Get a position", 
        notes = "Get operation with type and headers",
        response = Position.class
    )
    @ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server error"),
	    	@ApiResponse(code = 200, message = "OK")
	    })
    public Response getPosition (
        @ApiParam(value = "language", required = true) @HeaderParam("Accept-Language") final String language,
        @ApiParam(value = "name", required = true) @PathParam("name") String name) {
    	
    	Position position = new Position();
    	position.setId(1);
    	position.setLat(1.0);
    	position.setLng(1.0);
    	
        return Response.ok().entity(position).build();
    }
    
    
    
    
	@RolesAllowed("USER_AUTHENTICATED")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("/positions")
    @POST
    @ApiOperation(
        value = "Add a position", 
        notes = "Post operation with entity in a body",
        response = Position.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server error"),
        	@ApiResponse(code = 201, message = "Created")
        })
    public Response createPosition (
        @Context final UriInfo uriInfo,
        @ApiParam(value = "position", required = true) final Position position) {
        
    	return Response.created(uriInfo.getBaseUriBuilder().path(String.valueOf(position.getId())).build()).entity(position).build();
    }
    
    
    
	
	@RolesAllowed("USER_AUTHENTICATED")
    @Path("/positions/{id}")
    @DELETE
    @ApiOperation(
        value = "Delete a position", 
        notes = "Delete operation with implicit header"
    )
    @ApiImplicitParams(
       @ApiImplicitParam(
           name = "Accept-Language", 
           value = "language", 
           required = true, 
           dataType = "String", 
           paramType = "header"
       )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server error"),
        	@ApiResponse(code = 204, message = "No content")
        })
    public Response delete(@ApiParam(value = "id", required = true) @PathParam("id") int id) {
    	
        return Response.noContent().build();
    }
}
