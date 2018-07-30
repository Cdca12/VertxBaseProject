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
public class GenericInvalidException extends Exception {

    private String field;

    public GenericInvalidException(String field, String msg) {
        super(msg);
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }


}
