/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.personas;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import utils.UtilsJWT;
import utils.UtilsRouter;

/**
 *
 * @author sergioc
 */
public class HeaderSV extends AbstractVerticle {
    
    @Override
    public void start() throws Exception {
        super.start();
        
//        Router router = Router.router(this.vertx);
        
        Router router = UtilsRouter.getInstance(vertx);
        
        router.get("/headers").handler(this::getRaiz);
        
        
        this.vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(8480, reply -> {
            if (reply.succeeded()) {
                System.out.println("Servidor arrancado en: " + 8480);
            } else {
                System.out.println("Servidor no pudo arrancar, pellisquín.");
            }
        });

    }
    
    private void getRaiz(RoutingContext context) {
//        HttpServerRequest request = context.request();
        
        JsonObject response = new JsonObject();
        
        String headerNavegador = context.request().getHeader("User-Agent");
        String headerIP = context.request().getHeader("Host");
        int userID = UtilsJWT.getUserIdFrom(context.request().headers().get("Authorization"));
                
        
        response.put("Navegador", headerNavegador);
        response.put("IP", headerIP);
        response.put("User ID Token", userID);
        
        context
                .response()
                .putHeader("Content-Type", "application/json")
                .end(Json.encode(response));
    }
        
    
}
