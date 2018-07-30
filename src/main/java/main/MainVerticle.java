package main;

import database.commons.DBVerticle;
import database.personas.AnimalDBV;
import database.personas.AnimalesComidasDBV;
import database.personas.ComidaDBV;
import database.personas.PersonaDBV;
import database.reportes.TestDBV;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import service.commons.Constants;
import service.commons.ServiceVerticle;
import service.personas.AnimalSV;
import service.personas.AnimalesComidasSV;
import service.personas.ComidaSV;
import service.personas.HeaderSV;
import service.personas.PersonaSV;
import service.reportes.TestSV;
import utils.UtilsRouter;

/**
 * Main class to start all verticles
 *
 * @author kriblet
 */
public class MainVerticle extends AbstractVerticle {

    private static final String CONFIG_FILE_PATH = "./config.json";

    private String configFilePath;

    //constructors
    public MainVerticle() {
    }

    public MainVerticle(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public static void main(String[] args) {
        MainVerticle main;
        if (args.length > 0) {
            main = new MainVerticle(args[0]);
        } else {
            main = new MainVerticle();
        }
        Vertx v = Vertx.vertx();
        v.deployVerticle(main);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        //!important!//
        //initialize main router
        UtilsRouter.getInstance(vertx);

        JsonObject config = this.loadConfigFromFile();
        
        // Se levantan vértices
        initializeVerticle(new PersonaDBV(), new PersonaSV(), config);
        initializeVerticle(new AnimalDBV(), new AnimalSV(), config);
        initializeVerticle(new ComidaDBV(), new ComidaSV(), config);
        initializeVerticle(new AnimalesComidasDBV(), new AnimalesComidasSV(), config);
        
        // Práctica Headers
        vertx.deployVerticle(new HeaderSV());
        
    }

    public void initializeVerticle(DBVerticle dbVerticle, ServiceVerticle verticleService, JsonObject config) {
        Future<String> dbVerticleDeployment = Future.future();
        vertx.deployVerticle(dbVerticle, new DeploymentOptions().setConfig(config), dbVerticleDeployment);

        dbVerticleDeployment.compose(id -> {
            Future<String> httpVerticleDeployment = Future.future();
            vertx.deployVerticle(verticleService, new DeploymentOptions().setConfig(config), httpVerticleDeployment.completer());
            return httpVerticleDeployment;
        });
    }

    /**
     * loads into the JsonObject dbConfig the configuration from the file in db
     *
     * @param config the config file to set the properties
     */
    private JsonObject loadConfigFromFile() throws IOException {
        if (this.configFilePath != null) {
            return this.loadConfigToJsonObject(this.configFilePath);
        } else {
            return this.loadConfigToJsonObject(CONFIG_FILE_PATH); //load the default config file for db
        }
    }

    /**
     * Loads a config file in json format to deploy verticles
     *
     * @param filePath the path of the to load
     * @return json object with the properties in the file
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if an error occuer while reading
     */
    private JsonObject loadConfigToJsonObject(final String filePath) throws IOException {
        JsonObject result;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("Running app with file: " + filePath + ", and the configs are:");
            result = new JsonObject(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println("The file: " + filePath + ", was not found, running with default configs to develop enviroment:");
            result = new JsonObject()
                    .put("url", "jdbc:mysql://192.168.10.10:3306/vertx")
                    .put("driver_class", "com.mysql.jdbc.Driver")
                    .put("user", "administrador")
                    .put("password", "mysql.90Y9B8yh$123")
                    .put("max_pool_size", 100)
                    .put(Constants.CONFIG_HTTP_SERVER_PORT, 8480); //se default configs
        }
        System.out.println(result);
        return result;
    }
}
