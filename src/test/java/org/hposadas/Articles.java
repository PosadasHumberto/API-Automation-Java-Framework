package org.hposadas;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static io.restassured.RestAssured.*;

public class Articles extends Base{

    @Test(groups = "GetGroup")
    public void getArticlesList() {
        Response response = given()
                .log().all()
                .get("/articles");

        List<String> allArticles = response.path("data.title");
        Assert.assertFalse(allArticles.isEmpty(), "No artices returned");
    }

    @Test(groups = "PostGroup")
    public void createAndDeleteArticle() throws URISyntaxException {
        File articleFile = new File(getClass().getResource("/article.json").toURI());

        Response createResponse = given()
                .body(articleFile)
                .when()
                .log().all()
                .post("/articles");

        String responseID = createResponse.jsonPath().getString("post.article_id");

        Assert.assertTrue(responseID.length() > 0);

        Response deleteResponse = given()
                .body("{\n" +
                        "\t\"article_id\": " + responseID + "\n" +
                        "}")
                .log().all()
                .delete("/articles");

        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        Assert.assertEquals(deleteResponse.jsonPath().getString("message"), "Article successfully deleted");

    }

    @Test(groups = "PostGroup")
    public void deleteNonExistingArticle_FailMessage() {
        String nonExistingArticleID = "123456";

        Response response = given()
                .body("{\n" +
                        "\t\"article_id\": " + nonExistingArticleID + "\n" +
                        "}")
                .log().all()
                .delete("/articles");

        Assert.assertEquals(response.getStatusCode(), 500);
        Assert.assertEquals(response.jsonPath().getString("error"), "Unable to find article id: " + nonExistingArticleID);
    }

}
