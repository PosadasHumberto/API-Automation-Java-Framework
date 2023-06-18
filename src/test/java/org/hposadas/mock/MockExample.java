package org.hposadas.mock;

import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.jsv.JsonSchemaValidatorSettings.settings;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class MockExample{
    private static WireMockServer wireMockServer;

    private static final String EVENTS_PATH = "/events?id=390";     //endpoint imaginario
    private static final String APPLICATION_JSON = "application/json";
    private static final String GAME_ODDS = getEventJson(); //método que recupera el contenido de un json que deberiams tener en el resources del proyecto que contiene datos y atributos falsos.

    @BeforeClass    //método que se ejecutará antes de que se ejecute cualquier método de prueba en la clse MockExample
    public static void before() throws Exception {
        System.out.println("Setting up!");
        final int port = Util.getAvailablePort();   //Agarra cualquier puerto que esté disponible. el modificador final indica que la variable no puede ser modificada una vez que se le asigna un valor.
        wireMockServer = new WireMockServer(port);  // se configura el servidor para que escuche en ese puerto. El puerto sera siempre diferente
        wireMockServer.start();                     //Le decimos al doble(mock) que arranque
        RestAssured.port = port;                    //RestAssured va a usar tambien el puerto libre que obtuvimos anteriormente, esto nos asegura que WireMock y RestAssured van a usar el mismo puerto.
        configureFor("localhost", port);
        stubFor(get(urlEqualTo(EVENTS_PATH)).willReturn(        //se define la respuesta que se obtendra al hacer una peticion GET en la constante EVENTS_PATH
                aResponse().withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(GAME_ODDS)));                 //el body de la respuesta es el json con atributos y datos falsos que tenemos definido en resources.
    }

    @Test
    public void givenUrl_whenCheckingFloatValuePasses_thenCorrect() {
        get("/events?id=390").then().assertThat()
                .body("odd.ck", equalTo(12.2f));
        /*una prueba unitaria que verifica si el valor de tipo float en el cuerpo
        de la respuesta de una URL coincide con un valor específico.*/
    }

    @Test
    public void givenUrl_whenSuccessOnGetsResponseAndJsonHasRequiredKV_thenCorrect() {

        get("/events?id=390").then().statusCode(200).assertThat()
                .body("id", equalTo("390"));
        /* es una prueba unitaria que verifica si la respuesta de una URL tiene un
        código de estado 200 (éxito) y si el cuerpo de la respuesta contiene la clave
         "id" con el valor "390".*/
    }

    @Test
    public void givenUrl_whenJsonResponseHasArrayWithGivenValuesUnderKey_thenCorrect() {
        get("/events?id=390").then().assertThat()
                .body("odds.price", hasItems("1.30", "5.25", "2.70", "1.20"));
        /*es una prueba unitaria que verifica si la respuesta de una URL contiene un array
        con los valores especificados bajo la clave "odds.price".*/
    }

    @Test
    public void givenUrl_whenJsonResponseConformsToSchema_thenCorrect() {

        get("/events?id=390").then().assertThat()
                .body(matchesJsonSchemaInClasspath("evento.json"));
        /*es una prueba unitaria que valida si la respuesta de una URL cumple con un
        esquema JSON específico utilizando JSON Schema Validator de RestAssured.*/
    }

    @Test
    public void givenUrl_whenValidatesResponseWithInstanceSettings_thenCorrect() {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory
                .newBuilder()
                .setValidationConfiguration(
                        ValidationConfiguration.newBuilder()
                                .setDefaultVersion(SchemaVersion.DRAFTV4)
                                .freeze()).freeze();

        get("/events?id=390")
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("evento.json").using(
                        jsonSchemaFactory));
        /*es una prueba unitaria que valida la respuesta de una URL con una configuración de
        validación personalizada utilizando JSON Schema Validator de RestAssured.*/
    }

    @Test
    public void givenUrl_whenValidatesResponseWithStaticSettings_thenCorrect() {

        get("/events?id=390")
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("evento.json").using(
                        settings().with().checkedValidation(false)));
        /*es una prueba unitaria que valida la respuesta de una URL con una configuración
        estática utilizando JSON Schema Validator de RestAssured.*/
    }

    @AfterClass
    public static void after() throws Exception {
        System.out.println("Running: tearDown");
        wireMockServer.stop();

        /*La anotación `@AfterClass` se utiliza en pruebas unitarias para indicar que
        un método debe ejecutarse después de que se hayan ejecutado todos los métodos
        de prueba de una clase, generalmente para realizar tareas de limpieza o
        liberación de recursos.*/
    }

    private static String getEventJson() {
        return Util.inputStreamToString(RestAssuredIntegrationTest.class
                .getResourceAsStream("/evento.json"));
        /*devuelve una cadena de texto que representa el contenido de un archivo
        llamado "evento.json". el cual contiene un json con atributos y infirmacion
        falsa que representara el body de respuesta a una peticion get simulada*/
    }
}
