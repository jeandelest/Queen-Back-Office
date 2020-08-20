package fr.insee.queen.api.basicAuth;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.hasItem;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import fr.insee.queen.api.constants.Constants;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import liquibase.Liquibase;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties= {"fr.insee.queen.application.mode = Basic"})
@ActiveProfiles({ "test" })
@ContextConfiguration(initializers = { TestBasicAuth.Initializer.class})
@Testcontainers
class TestBasicAuth {
	
	public Liquibase liquibase;
	
	@LocalServerPort
	int port;

	@BeforeEach
	public void setUp() {
		RestAssured.port = port;
	}

	@SuppressWarnings("rawtypes")
	@Container
	@ClassRule
	public static PostgreSQLContainer postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer("postgres")
			.withDatabaseName("queen").withUsername("queen").withPassword("queen");

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues
					.of("spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
							"spring.datasource.username=" + postgreSQLContainer.getUsername(),
							"spring.datasource.password=" + postgreSQLContainer.getPassword())
					.applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	/**
	 * Test that the GET endpoint "api/operations"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindOperation() throws InterruptedException {
		given().auth().preemptive().basic("INTW1", "a").when().get("api/operations").then()
			.statusCode(200).and()
			.assertThat().body("id", hasItem("simpsons2020x00"));
		
	}

	/**
	 * Test that the GET endpoint "api/operations/{id}/questionnaire"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindQuestionnaireByOperation() {
		given().auth().preemptive().basic("INTW1", "a").get("api/operation/simpsons2020x00/questionnaire").then()
		.statusCode(200).and()
		.assertThat().body("isEmpty()", Matchers.is(false));
	}
	
	/**
	 * Test that the GET endpoint "api/operations/{id}/questionnaire"
	 * return 404 with wrong questionnaire Id
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindQuestionnaireByUnexistOperation() {
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/operation/toto/questionnaire").then().statusCode(404);
	}

	/**
	 * Test that the GET endpoint "api/operations/{id}/reporting-units"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindReportUnitsByOperation() {
		String expectedBody = "["
				+ "{"
					+ "\"id\":\"11\", "
					+ "\"campaign\":\"simpsons2020x00\", "
					+ "\"campaignLabel\":\"Survey on the Simpsons tv show 2020\", "
					+ "\"collectionStartDate\":\"1577836800000\", "
					+ "\"collectionEndDate\":\"1622035845000\""
				+ "}, "
				+ "{"
					+ "\"id\":\"12\", "
					+ "\"campaign\":\"simpsons2020x00\", "
					+ "\"campaignLabel\":\"Survey on the Simpsons tv show 2020\", "
					+ "\"collectionStartDate\":\"1577836800000\", "
					+ "\"collectionEndDate\":\"1622035845000\""
				+ "}, "
				+ "{"
					+ "\"id\":\"20\", "
					+ "\"campaign\":\"vqs2021x00\", "
					+ "\"campaignLabel\":\"Everyday life and health survey 2021\", "
					+ "\"collectionStartDate\":\"1577836800000\", "
					+ "\"collectionEndDate\":\"1622035845000\""
				+ "}]";
		ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(8081);
		MockServerClient mockServerClient = new MockServerClient("127.0.0.1", 8081);
		mockServerClient.when(request()
	        .withPath(Constants.API_PEARLJAM_SURVEY_UNITS))
	    .respond(response()
    		.withStatusCode(200)
    		.withHeaders(
	            new Header("Content-Type", "application/json; charset=utf-8"),
	            new Header("Cache-Control", "public, max-age=86400"))
	        .withBody(expectedBody));
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/operation/simpsons2020x00/reporting-units").then()
		.statusCode(200).and()
		.assertThat().body("id", hasItem("11"));
	}
	
	/**
	 * Test that the GET endpoint "api/operations/{id}/questionnaire"
	 * return 404 with wrong questionnaire Id
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindReportUnitsByUnexistOperation() {
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/operation/toto/reporting-units").then().statusCode(404);
	}

	/**
	 * Test that the GET endpoint "api/nomenclature/{id}"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindNomenclatureById() {
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/nomenclature/cities2019").then()
		.statusCode(200).and()
		.assertThat().body("isEmpty()", Matchers.is(false));
	}
	
	/**
	 * Test that the GET endpoint "api/nomenclature/{id}"
	 * return 404 with wrong nomenclature Id
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindUnexistNomenclatureById() {
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/nomenclature/toto").then().statusCode(404);
	}
	
	/**
	 * Test that the GET endpoint "api/reporting-unit/{id}/comment"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindCommentByReportingUnit() {
		Response response = given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/22/comment");
		response.then().statusCode(200);
		Assert.assertEquals(response.getBody().asString(), new JSONObject().toJSONString());
	}
	
	/**
	 * Test that the PUT endpoint "api/reporting-unit/{id}/comment"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testPutCommentByReportingUnit() {
		Map<String,String> putComment = new HashMap<>();
		putComment.put("comment", "value");
		JSONObject comment = new JSONObject(putComment);

		with()
			.contentType(ContentType.JSON)
			.body(comment.toJSONString())
			.given().auth().preemptive().basic("INTW1", "a")
		.put("api/reporting-unit/21/comment")
			.then()
			.statusCode(200);
		Response response = given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/21/comment");
		response.then().statusCode(200);
		Assert.assertEquals(response.getBody().asString(), comment.toJSONString());
	}
	
	/**
	 * Test that the GET endpoint "api/reporting-unit/{id}/comment"
	 * return 404 with wrong reporting-unit Id
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindCommentByUnexistReportingUnit() {
		given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/toto/comment").then().statusCode(404);
		given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/0/comment").then().statusCode(404);
	}

	/**
	 * Test that the GET endpoint "api/reporting-unit/{id}/data"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindDataByReportingUnit() {
		Response response = given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/22/data");
		response.then().statusCode(200);
		Assert.assertEquals(response.getBody().asString(), new JSONObject().toJSONString());
	}
	
	/**
	 * Test that the PUT endpoint "api/reporting-unit/{id}/data"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testPutDataByReportingUnit() {
		Map<String,String> putData = new HashMap<>();
		putData.put("data", "value");
		JSONObject data = new JSONObject(putData);

		with()
			.contentType(ContentType.JSON)
			.body(data.toJSONString())
			.given().auth().preemptive().basic("INTW1", "a")
		.put("api/reporting-unit/21/data")
			.then()
			.statusCode(200);
		Response response = given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/21/data");
		response.then().statusCode(200);
		Assert.assertEquals(response.getBody().asString(), data.toJSONString());
	}
	
	/**
	 * Test that the GET endpoint "api/reporting-unit/{id}/data"
	 * return 404 with wrong reporting-unit Id
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindDataByUnexistReportingUnit() {
		given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/toto/data").then().statusCode(404);
		given().auth().preemptive().basic("INTW1", "a").get("api/reporting-unit/0/data").then().statusCode(404);
	}

	/**
	 * Test that the GET endpoint "api/operation/{id}/required-nomenclature"
	 * return 200
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindRequiredNomenclatureByOperation() {
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/operation/vqs2021x00/required-nomenclatures").then()
		.statusCode(200).and()
		.assertThat().body("$", hasItem("cities2019"));
	}
	
	/**
	 * Test that the GET endpoint "api/operation/{id}/required-nomenclature"
	 * return 404 with wrong operation Id
	 * @throws InterruptedException
	 * @throws JSONException 
	 */
	@Test
	public void testFindRequiredNomenclatureByUnexistOperation() {
		given().auth().preemptive().basic("INTW1", "a")
		.get("api/operation/toto/required-nomenclatures").then().statusCode(404);
	}

}
