package fr.insee.queen.api.integration;

import fr.insee.queen.api.JsonHelper;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureEmbeddedDatabase(provider = ZONKY)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void on_get_data_return_data() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/survey-unit/11/data")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        String expectedResult = JsonHelper.getResourceFileAsString("db/dataset/data.json");
        JSONAssert.assertEquals(expectedResult, content, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void on_get_data_when_su_not_exist_return_404() throws Exception {
        mockMvc.perform(get("/api/survey-unit/plop/data")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void on_get_data_when_su_id_invalid_return_400() throws Exception {
        mockMvc.perform(get("/api/survey-unit/pl_op/data")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void on_update_data_data_is_updated() throws Exception {
        String surveyUnitId = "12";
        String dataJson = JsonHelper.getResourceFileAsString("db/dataset/data.json");
        MvcResult result = mockMvc.perform(get("/api/survey-unit/" + surveyUnitId + "/data")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JSONAssert.assertNotEquals(dataJson, content, JSONCompareMode.NON_EXTENSIBLE);

        mockMvc.perform(put("/api/survey-unit/" + surveyUnitId + "/data")
                        .content(dataJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        result = mockMvc.perform(get("/api/survey-unit/" + surveyUnitId + "/data")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        content = result.getResponse().getContentAsString();
        JSONAssert.assertEquals(dataJson, content, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void on_update_data_when_su_not_exist_return_404() throws Exception {
        mockMvc.perform(put("/api/survey-unit/not-exist/data")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void on_update_data_when_su_id_invalid_return_400() throws Exception {
        mockMvc.perform(put("/api/survey-unit/invalid_identifier/data")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void on_update_data_when_data_not_json_object_node_return_400() throws Exception {
        mockMvc.perform(put("/api/survey-unit/12/data")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
