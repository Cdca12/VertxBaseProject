/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.personas;

import database.personas.AnimalDBV;
import service.commons.ServiceVerticle;

/**
 *
 * @author sergioc
 */
public class AnimalSV extends ServiceVerticle {

    @Override
    protected String getDBAddress() {
        return AnimalDBV.ADDRESS;
    }

    @Override
    protected String getEndpointAddress() {
        return "/animales";
    }
    
}
