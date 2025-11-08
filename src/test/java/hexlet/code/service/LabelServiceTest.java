package hexlet.code.service;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class LabelServiceTest {

    @Autowired
    private LabelService labelService;

    @Autowired
    private LabelRepository labelRepository;

    @Test
    void testFindByIdNotFound() {
        Optional<Label> result = labelService.findById(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void testCreateLabelWithDuplicateName() {
        LabelCreateDTO dto1 = new LabelCreateDTO();
        dto1.setName("Duplicate Label");
        labelService.create(dto1);

        LabelCreateDTO dto2 = new LabelCreateDTO();
        dto2.setName("Duplicate Label");

        assertThatThrownBy(() -> labelService.create(dto2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testUpdateLabelNotFound() {
        LabelUpdateDTO updateDTO = new LabelUpdateDTO();

        assertThatThrownBy(() -> labelService.update(9999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testDeleteLabelNotFound() {
        assertThatThrownBy(() -> labelService.delete(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testUpdateLabelWithDuplicateName() {
        Label label1 = new Label();
        label1.setName("Label 1");
        labelRepository.save(label1);

        Label label2 = new Label();
        label2.setName("Label 2");
        labelRepository.save(label2);

        LabelUpdateDTO updateDTO = new LabelUpdateDTO();
        updateDTO.setName(org.openapitools.jackson.nullable.JsonNullable.of("Label 1"));

        assertThatThrownBy(() -> labelService.update(label2.getId(), updateDTO))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
