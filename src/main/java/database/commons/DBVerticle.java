/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.commons;

import static database.commons.Action.FIND_ALL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLOptions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static service.commons.Constants.ACTION;
import utils.UtilsValidation;

/**
 * Base Verticle for LCRUD entities, this class works as a facade
 *
 * @author kriblet
 */
public abstract class DBVerticle extends AbstractVerticle {

    /**
     * the sql client contains the channel of comunication with the database
     */
    protected SQLClient dbClient;
    /**
     * this logger works for messaging in console, configured in /resources/logback.xml
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DBVerticle.class);

    /**
     * method that runs when the verticles is deployed
     *
     * @param startFuture future to start with this deployment
     * @throws Exception
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", config().getString("url"))
                .put("driver_class", config().getString("driver_class"))
                .put("user", config().getString("user"))
                .put("password", config().getString("password"))
                .put("max_pool_size", config().getInteger("max_pool_size")));
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                startFuture.fail(ar.cause());
            } else {
                vertx.eventBus().consumer(getVerticleAddress(), this::onMessage);
                startFuture.complete();
            }
        });
    }

    /**
     * This method takes the action of the message and execute the method that corresponds
     *
     * @param message the message from the event bus
     */
    protected void onMessage(Message<JsonObject> message) {
        if (isValidAction(message)) {
            try {
                Action action = Action.valueOf(message.headers().get(ACTION));
                switch (action) {
                    case CREATE:
                        this.create(message);
                        break;
                    case DELETE_BY_ID:
                        this.deleteById(message);
                        break;
                    case FIND_BY_ID:
                        this.findById(message);
                        break;
                    case FIND_ALL:
                        this.findAll(message);
                        break;
                    case UPDATE:
                        this.update(message);
                        break;
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Validates if the action in the headers is valid
     *
     * @param message the message from the event bus
     * @return true if containg an action, false otherwise
     */
    protected boolean isValidAction(Message<JsonObject> message) {
        if (!message.headers().contains("action")) {
            LOGGER.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());
            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
            return false;
        }
        return true;
    }

    /**
     * This methos has the behaivor for reporting an error in a query execute
     *
     * @param message the message from the eventu bus
     * @param cause the exception giving problems
     * @param action action that was beign executed before de error
     */
    protected void reportQueryError(Message<JsonObject> message, Throwable cause, String action) {
        JsonObject catchedError = null;
        if (cause.getMessage().contains("foreign key")) { //a foreign key constraint fails
            Pattern p = Pattern.compile("`(.+?)`");
            Matcher m = p.matcher(cause.getMessage());
            List<String> incidencias = new ArrayList<>(5);
            while (m.find()) {
                incidencias.add(m.group(1));
            }
            //String warningMessage = "can´t " + action + " " + incidencias.get(1) + " because it´s property '" + incidencias.get(incidencias.size() - 3) + "' does not exist in the catalog '" + incidencias.get(incidencias.size() - 2) + "' in its '" + incidencias.get(incidencias.size() - 1) + "' property";            
            catchedError = new JsonObject().put("name", incidencias.get(incidencias.size() - 3)).put("error", "does not exist in the catalog");
        }
        if (cause.getMessage().contains("Data too long")) { //data to long for column            
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            List<String> incidencias = new ArrayList<>(5);
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", "to long data");
        }
        if (cause.getMessage().contains("Unknown column")) {//unkown column
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            List<String> incidencias = new ArrayList<>(5);
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", UtilsValidation.PARAMETER_DOES_NOT_EXIST);
        }
        if (cause.getMessage().contains("doesn't have a default")) { //not default value in not null
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", UtilsValidation.MISSING_REQUIRED_VALUE);
        }
        if (cause.getMessage().contains("Duplicate entry")) { //already exist (duplicate key for unique values)
            Pattern p = Pattern.compile("'(.+?)'");
            Matcher m = p.matcher(cause.getMessage());
            m.find();
            String value = m.group(1);
            m.find();
            String propertyName = m.group(1);
            catchedError = new JsonObject().put("name", propertyName).put("error", "value: " + value + " in " + UtilsValidation.ALREADY_EXISTS);
        }
        if (catchedError != null) {
            message.reply(catchedError, new DeliveryOptions().addHeader(ErrorCodes.DB_ERROR.toString(), catchedError.getString("error")));
        } else {
            LOGGER.error("Database query error", cause);
            message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
        }
    }

    /**
     * Execute the query "select * from"
     *
     * @param message message from the event bus
     */
    protected void findAll(Message<JsonObject> message) {
        String queryParam = message.body().getString("query");
        String fromParam = message.body().getString("from");
        String toParam = message.body().getString("to");

        String queryToExcecute;
        if (queryParam != null) {
            queryToExcecute = this.select(queryParam);
        } else {
            queryToExcecute = "select * from " + this.getTableName() + " where status != " + Status.DELETED.getValue();
        }
        //adds the limit for pagination
        if (fromParam != null && toParam != null) {
            queryToExcecute += " limit " + fromParam + "," + toParam;
        } else if (toParam != null) {
            queryToExcecute += " limit " + toParam;
        }
        dbClient.query(queryToExcecute, reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonArray(reply.result().getRows()));
            } else {
                reportQueryError(message, reply.cause(), "find");
            }
        });
    }

    /**
     * Execute the query "select * from table where id = ?"
     *
     * @param message message from the event bus
     */
    protected void findById(Message<JsonObject> message) {
        String query = "select * from " + this.getTableName() + " where id = ?";
        JsonArray params = new JsonArray().add(message.body().getString("id"));
        dbClient.queryWithParams(query, params, reply -> {
            if (reply.succeeded()) {
                if (reply.result().getNumRows() > 0) {
                    message.reply(reply.result().getRows().get(0));
                } else {
                    message.reply(null);
                }
            } else {
                reportQueryError(message, reply.cause(), "find");
            }
        });

    }

    /**
     * Execute the query "delete from table where id = ?"
     *
     * @param message message from the event bus
     */
    protected void deleteById(Message<JsonObject> message) {
        JsonArray params = new JsonArray().add(message.body().getString("id"));
        dbClient.updateWithParams("update " + this.getTableName() + " set status = 3 where id = ?", params, reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonObject().put("id", reply.result().getUpdated()));
            } else {
                reportQueryError(message, reply.cause(), "delete");
            }
        });
    }

    /**
     * Execute the query "create" generated by the properties of the object in the message
     *
     * @param message message from the event bus
     */
    protected void create(Message<JsonObject> message) {
        GenericCreate model = this.generateGenericCreate(message.body());
        dbClient.getConnection(hdl -> {
            if (hdl.succeeded()) {
                SQLConnection con = hdl.result();
                con.setOptions(new SQLOptions().setAutoGeneratedKeys(true));
                con.updateWithParams(model.getQuery(), model.getParams(), reply -> {
                    if (reply.succeeded()) {
                        message.reply(new JsonObject().put("id", reply.result().getKeys().getInteger(0)));
                    } else {
                        reportQueryError(message, reply.cause(), "create");
                    }
                    con.close();
                });
            } else {
                message.fail(ErrorCodes.DB_ERROR.ordinal(), "Imposible to get connection to the database");
            }
        });
    }

    /**
     * Execute the query "update" generated by the properties of the object in the message
     *
     * @param message message from the event bus
     */
    protected void update(Message<JsonObject> message) {
        String query = "update " + getTableName() + " set ";
        JsonArray params = new JsonArray();
        for (String fieldName : message.body().fieldNames()) {
            if (!fieldName.equals("id") && message.body().getValue(fieldName) != null) {
                query += fieldName + " = ?, ";
                params.add(message.body().getValue(fieldName));
            }
        }
        query = query.substring(0, query.length() - 2);
        query += " where id = ?";
        params.add(message.body().getInteger("id"));
        dbClient.updateWithParams(query, params, reply -> {
            if (reply.succeeded()) {
                message.reply(new JsonObject().put("updated", reply.result().getUpdated()));
            } else {
                reportQueryError(message, reply.cause(), "update");
            }
        });
    }

    /**
     * Execute the query "select {properties} from " where properties are the solicited properties in the query param
     *
     * @param queryParam string query param, that contains the selection and filter to query
     * @return query to excecute in the data base generated with the queryParam
     */
    protected String select(String queryParam) {
        //clausula de selecccion de campos
        String qSelect = "select ";
        //set de elementos en las clausulas where
        Set<String> qWheres = new LinkedHashSet<>();
        //set de elementos en las clausulas from
        Set<String> qFroms = new LinkedHashSet<>();
        qFroms.add(" from " + this.getTableName());
        //obtener los elementos solicitados
        boolean addStatusFilter = true;
        String[] selections = queryParam.split(","); //fileds separation
        for (String selection : selections) {
            if (selection.contains(".")) { //a point means that joins with a table
                String[] relation = selection.split("\\.");
                String whereSelection = whereSelection(relation[1]);
                if (whereSelection.isEmpty()) {
                    qSelect += relation[0] + "." + relation[1] + " as " + (relation[0] + "_" + relation[1]) + ",";
                    qFroms.add(relation[0]);
                    qWheres.add(this.getTableName() + "." + relation[0] + "_id = " + relation[0] + ".id");
                } else {
                    String[] fieldDecompose = relation[1].split(whereSelection);
                    qSelect += relation[0] + "." + fieldDecompose[0] + " as " + (relation[0] + "_" + fieldDecompose[0]) + ",";
                    qFroms.add(relation[0]);
                    qWheres.add(this.getTableName() + "." + relation[0] + "_id = " + relation[0] + ".id");
                    qWheres.add(relation[0] + "." + fieldDecompose[0] + " = " + fieldDecompose[1]);
                }
            } else {//if no joins then only validate a where clause
                String whereSelection = whereSelection(selection);
                if (whereSelection.isEmpty()) {
                    qSelect += this.getTableName() + "." + selection + ",";
                } else {
                    String[] whereSelections = selection.split(whereSelection);
                    if (whereSelections[0].equals("status")) { //ignore de default filter in status column
                        addStatusFilter = false;
                    }
                    if (whereSelection.equals("><")) { //if the where clause is a between operation
                        qSelect += this.getTableName() + "." + whereSelections[0] + ",";
                        String[] betweenValues = whereSelections[1].split("\\|");
                        qWheres.add(this.getTableName() + "." + whereSelections[0] + " between " + betweenValues[0] + " and " + betweenValues[1]);
                    } else {
                        qSelect += this.getTableName() + "." + whereSelections[0] + ",";
                        qWheres.add(this.getTableName() + "." + whereSelections[0] + " " + whereSelection + " " + whereSelections[1]);
                    }
                }
            }
        }
        qSelect = qSelect.substring(0, qSelect.length() - 1);
        String from = String.join(",", qFroms);
        String where = "";
        if (!qWheres.isEmpty()) {
            where += " where ";
            where += String.join(" and ", qWheres);
            if (addStatusFilter) {
                where += " and " + this.getTableName() + ".status != " + Status.DELETED.getValue();
            }
        } else {
            where += " where " + this.getTableName() + ".status != " + Status.DELETED.getValue();
        }
        System.out.println(qSelect + from + where);
        return qSelect + from + where;
    }    

    /**
     * check if there is a where condition in a selection
     *
     * @param selection the field selection
     * @return the operator to use in the where clause like '=' or '!='
     */
    private String whereSelection(String selection) {
        if (selection.contains(">=")) {
            return ">=";
        }
        if (selection.contains("<=")) {
            return "<=";
        }
        if (selection.contains("><")) { //between, its important the order of the questions
            return "><";
        }
        if (selection.contains("!=")) {
            return "!=";
        }
        if (selection.contains("=")) {
            return "=";
        }
        if (selection.contains(">")) {
            return ">";
        }
        if (selection.contains("<")) {
            return "<";
        }
        return "";
    }

    /**
     * Generates que query and params needed for a generic create operation
     *
     * @param objectBody the object message to create
     * @return model with the query and needed params to create a register in db of this ServiceDatabaseVerticle
     */
    protected final GenericCreate generateGenericCreate(JsonObject objectBody) {
        String query = "insert into " + getTableName() + "(";
        String queryValues = " values (";
        JsonArray params = new JsonArray();
        for (String fieldName : objectBody.fieldNames()) {
            query += fieldName + ",";
            queryValues += "?,";
            params.add(objectBody.getValue(fieldName));
        }
        query = query.substring(0, query.length() - 1) + ")";
        queryValues = queryValues.substring(0, queryValues.length() - 1) + ")";
        query += queryValues;
        return new GenericCreate(query, params);
    }

    /**
     * Generates a string raw query to insert in database the object in objectBody in the table given
     *
     * @param tableName name of the table to generate the insert
     * @param objectBody object with all the properties to insert
     * @return string with the query
     */
    protected final String generateGenericCreate(final String tableName, final JsonObject objectBody) {
        String query = "insert into " + tableName + "(";
        String queryValues = " values (";
        JsonArray params = new JsonArray();
        for (String fieldName : objectBody.fieldNames()) {
            query += fieldName + ",";
            //evaluate the field if is string
            try {
                queryValues += "'" + objectBody.getString(fieldName) + "',";
            } catch (Exception e) {
                queryValues += objectBody.getValue(fieldName) + ",";
            }
        }
        query = query.substring(0, query.length() - 1) + ")";
        queryValues = queryValues.substring(0, queryValues.length() - 1) + ")";
        query += queryValues;
        return query;
    }

    /**
     * Need to especifie the name of the entity table, to refer in the properties file, the actions names and queries
     *
     * @return the name of the table to manage in this verticle
     */
    public abstract String getTableName();

    /**
     * Need to especifie the addres of this verticle to be registered in the event bus
     *
     * @return name of the address in the event bus to be registered
     */
    public abstract String getVerticleAddress();

    /**
     * Roll back the actual connection in transaction with a generic invalid exception message type to the messager
     *
     * @param con connection in actual transaction
     * @param ex exception with the field and message to display
     * @param message message of the serder
     */
    protected void rollback(SQLConnection con, GenericInvalidException ex, Message<JsonObject> message) {
        con.rollback(h -> {
            con.close();
            message.reply(
                    new JsonObject()
                            .put("name", ex.getField())
                            .put("error", ex.getMessage()),
                    new DeliveryOptions()
                            .addHeader(ErrorCodes.DB_ERROR.toString(),
                                    "invalid parameter")
            );
        });
    }

    /**
     * Roll back the actual connection in transaction with a generic invalid exception message type to the messager
     *
     * @param con connection in actual transaction
     * @param t cause of the fail in an operation in the transaction
     * @param message message of the serder
     */
    protected void rollback(SQLConnection con, Throwable t, Message<JsonObject> message) {
        con.rollback(h -> {
            con.close();
            reportQueryError(message, t, "transaction");
        });
    }

    /**
     * Commist the actual transaction and replays to the sender the object provided
     *
     * @param con connection in actual transaction
     * @param message message of the sender
     * @param jsonObject object to reply
     */
    protected void commit(SQLConnection con, Message<JsonObject> message, JsonObject jsonObject) {
        con.commit(h -> {
            con.close();
            message.reply(jsonObject);
        });
    }
}
