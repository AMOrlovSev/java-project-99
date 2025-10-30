package hexlet.code.controller;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    public void setUp() {
        token = jwtUtils.generateToken("hexlet@example.com");
    }

    @Test
    @WithMockUser
    public void testGetLabels() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/labels")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    public void testCreateLabel() throws Exception {
        LabelCreateDTO labelData = new LabelCreateDTO();
        labelData.setName("Test Label");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/labels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelData)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Label"));

        assertThat(labelRepository.existsByName("Test Label")).isTrue();
    }

    @Test
    @WithMockUser
    public void testUpdateLabel() throws Exception {
        Label label = new Label();
        label.setName("Old Name");
        label = labelRepository.save(label);

        LabelUpdateDTO updateData = new LabelUpdateDTO();
        updateData.setName(org.openapitools.jackson.nullable.JsonNullable.of("New Name"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/labels/" + label.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("New Name"));

        Label updatedLabel = labelRepository.findById(label.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo("New Name");
    }

    @Test
    @WithMockUser
    public void testDeleteLabel() throws Exception {
        Label label = new Label();
        label.setName("To Delete");
        label = labelRepository.save(label);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/labels/" + label.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertThat(labelRepository.existsById(label.getId())).isFalse();
    }
}
