import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.response.Response;        
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class PatientIntegrationTest {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnPatientsWithValidToken () {
        String token = getToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());
    }

    @Test  // 429 => Too many request in status code back
    public void shouldReturn429AfterLimitExceeded () throws InterruptedException {
        String token = getToken();
        int total = 10; // As we have limited 5 in yml in api-gateway so 10 here should throw an error
        int tooManyRequests = 0;

        for(int i = 1; i <= total; i++) {
            Response response = RestAssured
                    .given()
                    .header("Authorization","Bearer " + token)
                    .get("/api/patients");

            System.out.printf("Request %d ->  Status code: %d\n", i, response.statusCode()); // printf as we need formatted value . so println not allowed
            if(response.statusCode() == 429) {
                tooManyRequests++;
            }
            Thread.sleep(100); // 10 requests so 10 calls per second
        }
        assertTrue(tooManyRequests >= 1,
                "Expected at least 1 request to be rate limited(429)");
    }

    private static String getToken() {
        String loginPayload = """
          {
            "email": "testuser@test.com",
            "password": "password123"
          }
        """;

        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");
        return token;
    }
}