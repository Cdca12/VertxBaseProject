/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.personas;

import database.commons.ErrorCodes;
import database.personas.ComidaDBV;
import static database.personas.ComidaDBV.REGISTER;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import static service.commons.Constants.*;
import service.commons.ServiceVerticle;
import utils.UtilsJWT;
import static utils.UtilsJWT.isTokenValid;
import static utils.UtilsResponse.*;
//import utils.UtilsValidation.*;
import static utils.UtilsValidation.*;

/**
 *
 * @author sergioc
 */
public class ComidaSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return ComidaDBV.ADDRESS;
    }

    @Override
    protected String getEndpointAddress() {
        return "/comidas";
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        this.router.post("/register").handler(BodyHandler.create());
        this.router.post("/register").handler(this::register);
    }

    private void register(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        JsonObject body = context.getBodyAsJson();

        if (isTokenValid(token)) {
            try {
                JsonArray comidas = body.getJsonArray("comidas");
                for (int i = 0; i < comidas.size(); i++) {
                    JsonObject comida = comidas.getJsonObject(i);
                    isName(comida, "nombre");
                }
            } catch (PropertyValueException ex) {
                responsePropertyValue(context, ex);
            }
            // Mandar a guardar los datos
            this.vertx.eventBus().send(
                    ComidaDBV.ADDRESS,
                    body,
                    new DeliveryOptions().addHeader(ACTION, REGISTER),
                    reply -> {
                        if (reply.succeeded()) {
                            if (reply.result().headers().contains(ErrorCodes.DB_ERROR.toString())) {
                                responseWarning(context, INVALID_DATA, INVALID_DATA_MESSAGE, reply.result().body());
                            } else {
                                responseOk(context, reply.result().body(), "Created");
                            }
                        } else {
                            responseError(context, GENERIC_ERROR, reply.cause().getMessage());
                        }
                    });
        } else {
            responseInvalidToken(context);
        }
    }

}


