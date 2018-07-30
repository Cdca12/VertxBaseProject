/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.personas;

import database.commons.DBVerticle;

/**
 *
 * @author sergioc
 */
public class AnimalesComidasDBV extends DBVerticle {
    
    public static final String ADDRESS = "db.animalesComidas";

    @Override
    public String getTableName() {
        return "CC_animales_comidas";
    }

    @Override
    public String getVerticleAddress() {
        return ADDRESS;
    }
    
    
}
