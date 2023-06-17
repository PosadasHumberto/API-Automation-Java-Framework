package org.hposadas;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

public class Base {

    @BeforeClass
    public static void setupRestAssured(){

        RestAssured.baseURI = "http://qa-library-dev.herokuapp.com";
        RestAssured.basePath = "/api";

        RequestSpecification requestSpecification = new RequestSpecBuilder()
                .addHeader("Content-Type", ContentType.JSON.toString())     //enviando JSON
                .addHeader("Accept", ContentType.JSON.toString())           //esperando recibir JSON
                .build();

        RestAssured.requestSpecification = requestSpecification;    //Establece la especificación de solicitud global para RestAssured, de modo que todas las solicitudes realizadas con RestAssured utilicen esta especificación.
    }
}
