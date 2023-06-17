package org.hposadas;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class Books extends Base {

    @Test(groups = "GetGroup")       //validando que existan libros
    public void getBooksList() {
        Response response = given()
                .log().all()
                .get("/books");

        List<String> allBooks = response.path("data.title");
        Assert.assertTrue(allBooks.size() > 1, "No books returned");

    }

    @Test(groups = "GetGroup")       //Validando que el esquema es valido
    public void booksSchemaIsValid() {
        given()
                .log().all()
                .get("/books")
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("booksSchema.json"));    //este esquema debe ser dado por los desarrolladores de la aplicacion para que podamos hacer los tests.
    }

    @Test(groups = "PostGroup")       //validando la creacion y supresion de un Book
    public void createAndDeleteBook() throws URISyntaxException {
        File bookFile = new File(getClass().getResource("/book.json").toURI());

        Response createResponse = given()
                .body(bookFile)
                .when()
                .post("/books");

        String responseID = createResponse.jsonPath().getString("post.book_id");

        Response deleteResponse = given()
                .body("{\n" +
                        "\t\"book_id\": " + responseID + "\n" +
                        "}")
                .when()
                .delete("/books");

        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        Assert.assertEquals(deleteResponse.jsonPath().getString("message"), "Book successfully deleted");

    }

    @Test(groups = "PostGroup")
    public void deleteNonExistingBook_FailMessage() {
        String nonExistentBookID = "456123";

        Response deleteResponse =
                given()
                        .body("{\n" +
                                "\t\"book_id\": " + nonExistentBookID + "\n" +
                                "}")
                        .when()
                        .delete("/books");

        Assert.assertEquals(deleteResponse.getStatusCode(), 500);
        Assert.assertEquals(deleteResponse.jsonPath().getString("error"), "Unable to find book id: " + nonExistentBookID);

    }

}
