/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.personas;

import database.personas.AnimalesComidasDBV;
import service.commons.ServiceVerticle;

/**
 *
 * @author sergioc
 */
public class AnimalesComidasSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return AnimalesComidasDBV.ADDRESS;
    }

    @Override
    protected String getEndpointAddress() {
        return "/animalesComidas";
    }
    
}
