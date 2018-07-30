/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.commons;

import static database.commons.Action.*;
import database.commons.ErrorCodes;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.PropertyError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static service.commons.Constants.ACTION;
import static service.commons.Constants.CONFIG_HTTP_SERVER_PORT;
import utils.UtilsDate;
import utils.UtilsJWT;
import static utils.UtilsResponse.*;
import utils.UtilsRouter;
import utils.UtilsValidation;

/**
 * Base Verticle to work with LCRUD default operations
 *
 * @author kriblet
 */
public abstract class ServiceVerticle extends AbstractVerticle {

    /**
     * This logger works for debugging tasks, configured in /logback.xml
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);
    /**
     * The router for this verticle service instance
     */
    protected final Router router = Router.router(vertx);

    /**
     * Need to specifie the address of the verticles in the event bus with the access of the db that contains the table
     *
     * @return the name of the registered DBVerticle to work with
     */
    protected abstract String getDBAddress();

    /**
     * Need to especifie the endpoint domain for this verticles begining with "/", ex: return "/example";
     *
     * @return the name to register the verticle in the main router
     */
    protected abstract String getEndpointAddress();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServer server = vertx.createHttpServer();
        router.get("/").handler(this::findAll);
        router.get("/:id").handler(this::findById);
        router.post("/").handler(BodyHandler.create()); //needed to catch body of request
        router.post("/").handler(this::create);
        router.put("/").handler(BodyHandler.create()); //needed to catch body of request
        router.put("/").handler(this::update);
        router.delete("/:id").handler(this::deleteById);
        UtilsRouter.getInstance(vertx).mountSubRouter(getEndpointAddress(), router);
        Integer portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT);
        if (portNumber == null) {
            startFuture.fail(new Exception("No port speficied in configuration"));
            LOGGER.error("Could not start a HTTP server", "No port speficied in configuration");
        }
        server.requestHandler(UtilsRouter.getInstance(vertx)::accept)
                .listen(portNumber, ar -> {
                    if (ar.succeeded()) {
                        LOGGER.info("HTTP server running on port " + portNumber);
                        startFuture.complete();
                    } else {
                        LOGGER.error("Could not start a HTTP server", ar.cause());
                        startFuture.fail(ar.cause());
                    }
                });
    }

    /**
     * Sends a message to the verticle registered with DBAddress especified in this instance the action of "findAll"
     *
     * @param context the routing context running in the request
     */
    protected void findAll(RoutingContext context) {
        String jwt = context.request().getHeader("Authorization");
        if (UtilsJWT.isTokenValid(jwt)) {
            JsonObject message = new JsonObject()
                    .put("query", context.request().getParam("query"))
                    .put("from", context.request().getParam("from"))
                    .put("to", context.request().getParam("to"));
            DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, FIND_ALL.name());
            vertx.eventBus().send(this.getDBAddress(), message, options, reply -> {
                if (reply.succeeded()) {
                    responseOk(context, reply.result().body(), "Found");
                } else {
                    responseError(context, "Ocurrió un error inesperado, consulte con el proveedor de sistemas", reply.cause().getMessage());
                }
            });
        } else {
            responseInvalidToken(context);
        }
    }

    /**
     * Sends a message to the verticle registered with DBAddress especified in this instance the action of "findById"
     *
     * @param context the routing context running in the request
     */
    protected void findById(RoutingContext context) {
        String jwt = context.request().getHeader("Authorization");
        if (UtilsJWT.isTokenValid(jwt)) {
            JsonObject message = new JsonObject().put("id", context.request().getParam("id"));                    
            DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, FIND_BY_ID.name());
            vertx.eventBus().send(this.getDBAddress(), message, options, reply -> {
                if (reply.succeeded()) {
                    responseOk(context, reply.result().body(), "Found");
                } else {
                    responseError(context, "Ocurrió un error inesperado, consulte con el proveedor de sistemas", reply.cause().getMessage());
                }
            });
        } else {
            responseInvalidToken(context);
        }
    }

    /**
     * Sends a message to the verticle registered with DBAddress especified in this instance the action of "update"
     *
     * @param context the routing context running in the request
     */
    protected void update(RoutingContext context) {
        String jwt = context.request().getHeader("Authorization");
        if (UtilsJWT.isTokenValid(jwt)) {
            if (this.isValidUpdateData(context)) {
                DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, UPDATE.name());
                JsonObject reqBody = context.getBodyAsJson();
                //clean properties if exist any of this
                reqBody.remove("created_at");
                reqBody.remove("created_by");
                //set the user requesting to create
                reqBody.put("updated_at", UtilsDate.sdfDataBase(new Date()));
                reqBody.put("updated_by", UtilsJWT.getUserIdFrom(jwt));
                vertx.eventBus().send(this.getDBAddress(), reqBody, options, reply -> {
                    if (reply.succeeded()) {
                        if (reply.result().headers().contains(ErrorCodes.DB_ERROR.toString())) {
                            responseWarning(context, "Invalid data", "Some properties in the model are invalid, see details in data", reply.result().body());
                        } else {
                            responseOk(context, reply.result().body(), "Updated");
                        }
                    } else {
                        responseError(context, "Ocurrió un error inesperado, consulte con el proveedor de sistemas", reply.cause().getMessage());
                    }
                });
            }
        } else {
            responseInvalidToken(context);
        }
    }

    /**
     * Sends a message to the verticle registered with DBAddress especified in this instance the action of "create"
     *
     * @param context the routing context running in the request
     */
    protected void create(RoutingContext context) {
        String jwt = context.request().getHeader("Authorization");
        if (UtilsJWT.isTokenValid(jwt)) {
            if (this.isValidCreateData(context)) {
                DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, CREATE.name());

                JsonObject reqBody = context.getBodyAsJson();
                //clean properties if exist any of this
                reqBody.remove("created_at");
                reqBody.remove("updated_at");
                reqBody.remove("updated_by");
                //set the user requesting to create
                reqBody.put("created_by", UtilsJWT.getUserIdFrom(jwt));

                vertx.eventBus().send(this.getDBAddress(), reqBody, options, reply -> {
                    if (reply.succeeded()) {
                        if (reply.result().headers().contains(ErrorCodes.DB_ERROR.toString())) {
                            responseWarning(context, "Invalid data", "Some properties in the model are invalid, see details in data", reply.result().body());
                        } else {
                            responseOk(context, reply.result().body(), "Created");
                        }
                    } else {
                        responseError(context, "Ocurrió un error inesperado, consulte con el proveedor de sistemas", reply.cause().getMessage());
                    }
                });
            }
        } else {
            responseInvalidToken(context);
        }
    }

    /**
     * Sends a message to the verticle registered with DBAddress especified in this instance the action of "deleteById"
     *
     * @param context the routing context running in the request
     */
    protected void deleteById(RoutingContext context) {
        String jwt = context.request().getHeader("Authorization");
        if (UtilsJWT.isTokenValid(jwt)) {
            JsonObject reqBody = new JsonObject().put("id", context.request().getParam("id"));
            DeliveryOptions options = new DeliveryOptions().addHeader(ACTION, DELETE_BY_ID.name());
            vertx.eventBus().send(this.getDBAddress(), reqBody, options,
                    reply -> {
                        if (reply.succeeded()) {
                            responseOk(context, reply.result().body(), "Deleted");
                        } else {
                            responseError(context, "Ocurrió un error inesperado, consulte con el proveedor de sistemas", reply.cause().getMessage());
                        }
                    }
            );
        } else {
            responseWarning(context, "Out of session", "Sessión json web token is invalid");
        }
    }

    /**
     * Verifies is the data of the request is valid to create a record of this entity
     *
     * @param context context of the request
     * @return true if the data is valid, false othrewise
     */
    protected boolean isValidCreateData(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        List<PropertyError> errors = new ArrayList<>();
        if (body.getInteger("id") != null) {
            errors.add(new PropertyError("id", UtilsValidation.INVALID_PARAMETER));
        }
        if (!errors.isEmpty()) {
            responseWarning(context, "Invalid data", "Some properties in the model are invalid, see details in data", errors);
            return false;
        }
        return true;
    }

    /**
     * Verifies is the data of the request is valid to update a record of this entity
     *
     * @param context context of the request
     * @return true if the data is valid, false othrewise
     */
    protected boolean isValidUpdateData(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        List<PropertyError> errors = new ArrayList<>();
        if (body.getInteger("id") == null) {
            errors.add(new PropertyError("id", UtilsValidation.MISSING_REQUIRED_VALUE));
        }
        if (!errors.isEmpty()) {
            responseWarning(context, "Invalid data", "Some properties in the model are invalid, see details in data", errors);
            return false;
        }
        return true;
    }

}
