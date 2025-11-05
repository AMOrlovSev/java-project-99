package hexlet.code.controller;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(DatabaseCleanerExtension.class)
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetLabels() throws Exception {
        Label label1 = new Label();
        label1.setName("Label 1");
        labelRepository.save(label1);

        Label label2 = new Label();
        label2.setName("Label 2");
        labelRepository.save(label2);

        var response = mockMvc.perform(MockMvcRequestBuilders.get("/api/labels"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        List<LabelDTO> actual = objectMapper.readValue(body, new TypeReference<>() {

        });
        List<Label> expectedLabels = labelRepository.findAll();

        Set<Long> actualIds = actual.stream().map(LabelDTO::getId).collect(Collectors.toSet());
        Set<Long> expectedIds = expectedLabels.stream().map(Label::getId).collect(Collectors.toSet());
        Assertions.assertThat(actualIds).isEqualTo(expectedIds);

        Set<String> actualNames = actual.stream().map(LabelDTO::getName).collect(Collectors.toSet());
        Set<String> expectedNames = expectedLabels.stream().map(Label::getName).collect(Collectors.toSet());
        Assertions.assertThat(actualNames).isEqualTo(expectedNames);

        actual.forEach(labelDTO -> Assertions.assertThat(labelDTO.getCreatedAt()).isNotNull());
    }

    @Test
    @WithMockUser
    public void testCreateLabel() throws Exception {
        LabelCreateDTO labelData = new LabelCreateDTO();
        labelData.setName("Test Label");

        var response = mockMvc.perform(MockMvcRequestBuilders.post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Label"))
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        LabelDTO createdLabelDTO = objectMapper.readValue(body, LabelDTO.class);
        Label expectedLabel = labelRepository.findByName("Test Label").orElseThrow();

        Assertions.assertThat(createdLabelDTO.getId()).isEqualTo(expectedLabel.getId());
        Assertions.assertThat(createdLabelDTO.getName()).isEqualTo(expectedLabel.getName());
        Assertions.assertThat(createdLabelDTO.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    public void testUpdateLabel() throws Exception {
        Label label = new Label();
        label.setName("Old Name");
        label = labelRepository.save(label);

        LabelUpdateDTO updateData = new LabelUpdateDTO();
        updateData.setName(org.openapitools.jackson.nullable.JsonNullable.of("New Name"));

        var response = mockMvc.perform(MockMvcRequestBuilders.put("/api/labels/" + label.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        LabelDTO updatedLabelDTO = objectMapper.readValue(body, LabelDTO.class);
        Label expectedLabel = labelRepository.findById(label.getId()).orElseThrow();

        Assertions.assertThat(updatedLabelDTO.getId()).isEqualTo(expectedLabel.getId());
        Assertions.assertThat(updatedLabelDTO.getName()).isEqualTo(expectedLabel.getName());
        Assertions.assertThat(updatedLabelDTO.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    public void testDeleteLabel() throws Exception {
        Label label = new Label();
        label.setName("To Delete");
        label = labelRepository.save(label);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/labels/" + label.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(labelRepository.existsById(label.getId())).isFalse();
    }
}
