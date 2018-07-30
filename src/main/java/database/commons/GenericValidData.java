/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

/**
 *
 * @author kriblet
 */
public class GenericValidData {
    
    private final boolean valid;
    private final String message;

    public GenericValidData(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
    
    
}
