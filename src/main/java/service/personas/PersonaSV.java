/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.personas;

import database.commons.ErrorCodes;
import database.personas.PersonaDBV;
import static database.personas.PersonaDBV.REGISTER;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import service.commons.Constants.*;
import static service.commons.Constants.*;
import service.commons.ServiceVerticle;
import utils.UtilsJWT;
import utils.UtilsResponse;
import static utils.UtilsResponse.*;
import static utils.UtilsValidation.*;

/**
 *
 * @author sergioc
 */
public class PersonaSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return PersonaDBV.ADDRESS;
    }

    @Override
    protected String getEndpointAddress() {
        return "/personas";
    }

    @Override
    protected boolean isValidUpdateData(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        try {
            isName(body, "nombre");
        } catch (PropertyValueException ex) {
            return UtilsResponse.responsePropertyValue(context, ex);
        }
        return super.isValidUpdateData(context);
    }

    @Override
    protected boolean isValidCreateData(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        try {
            isNameAndNotNull(body, "nombre");
        } catch (PropertyValueException ex) {
            return UtilsResponse.responsePropertyValue(context, ex);
        }
        return super.isValidCreateData(context);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        this.router.post("/register").handler(BodyHandler.create());
        this.router.post("/register").handler(this::register);
    }

    private void register(RoutingContext context) {
        String token = context.request().headers().get("Authorization");
        if (UtilsJWT.isTokenValid(token)) {
            JsonObject body = context.getBodyAsJson();
            try {
                isName(body, "nombre");
                JsonArray mascotas = body.getJsonArray("mascotas");
                for (int i = 0; i < mascotas.size(); i++) {
                    JsonObject mascota = mascotas.getJsonObject(i);
                    isName(mascota, "nombre");
                }
            } catch (PropertyValueException ex) {
                responsePropertyValue(context, ex);
            }
            // Mandar a guardar los datos
            this.vertx.eventBus().send(
                    PersonaDBV.ADDRESS,
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
