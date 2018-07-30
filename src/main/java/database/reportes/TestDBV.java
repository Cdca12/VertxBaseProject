/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.reportes;

import database.commons.DBVerticle;

/**
 *
 * @author kriblet
 */
public class TestDBV extends DBVerticle{

    public static final String ADDRESS = "db.prueba";
    
    @Override
    public String getTableName() {
        return "prueba";
    }

    @Override
    public String getVerticleAddress() {
        return ADDRESS;
    }
    
}
