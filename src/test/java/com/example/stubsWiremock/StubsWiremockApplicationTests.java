package com.example.stubsWiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static io.restassured.RestAssured.given;

@SpringBootTest
class StubsWiremockApplicationTests {

    private static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(5050));

    @BeforeAll
    public static void setUpMockServer() {
        wireMockServer.start();

        WireMock.configureFor("localhost", 5050);
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/users/2"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody("{\n" +
                                "    \"data\": {\n" +
                                "        \"id\": 2,\n" +
                                "        \"email\": \"janet.weaver@reqres.in\",\n" +
                                "        \"first_name\": \"Janet\",\n" +
                                "        \"last_name\": \"Weaver\",\n" +
                                "        \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\n" +
                                "    },\n" +
                                "    \"support\": {\n" +
                                "        \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "        \"text\": \"To keep ReqRes free, contributions towards server costs are appreciated!\"\n" +
                                "    }\n" +
                                "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/users/23"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withBody("{}")));

        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/users"))
                .withRequestBody(equalToJson("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"leader\"\n" +
                        "}"))
                .willReturn(WireMock.aResponse()
                        .withStatus(201)
                        .withBody("{\n" +
                                "    \"name\": \"morpheus\",\n" +
                                "    \"job\": \"leader\",\n" +
                                "    \"id\": \"998\",\n" +
                                "    \"createdAt\": \"2021-07-25T16:56:31.090Z\"\n" +
                                "}")));

    }

    @Test
    void testGetUserWithStub() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
//                .get("https://reqres.in/api/users/2")
                .get("http://localhost:5050/api/users/2")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        System.out.println(response.getBody().prettyPrint());
        System.out.println(1);
        Assertions.assertEquals("Janet", response.jsonPath().getString("data.first_name"));
        Assertions.assertEquals("Weaver", response.jsonPath().getString("data.last_name"));
    }

    @Test
    void testGetUnknownUserWithStub() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("http://localhost:5050/api/users/23")
                .then()
                .extract().response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals("{}", response.getBody().prettyPrint());

    }

    @Test
    void testCreateUserWithStub() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"leader\"\n" +
                        "}")
                .when()
                .post("http://localhost:5050/api/users")
                .then()
                .extract().response();

        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals("morpheus", response.jsonPath().getString("name"));
        Assertions.assertEquals("leader", response.jsonPath().getString("job"));
    }

    @AfterAll
    public static void tearDownMockServer() {
        wireMockServer.stop();
    }
}
