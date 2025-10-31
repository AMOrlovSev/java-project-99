package hexlet.code.mapper;

import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class ReferenceMapperTest {

    @Autowired
    private ReferenceMapper referenceMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Test
    void testToEntityWithNullId() {
        Label result = referenceMapper.toEntity(null, Label.class);
        assertThat(result).isNull();
    }

    @Test
    void testToEntityWithNonExistentId() {
        Label result = referenceMapper.toEntity(9999L, Label.class);
        assertThat(result).isNull();
    }

    @Test
    void testToEntityWithExistingId() {
        // Create a label first
        Label label = new Label();
        label.setName("Test Label");
        Label savedLabel = labelRepository.save(label);

        // Test finding it with ReferenceMapper
        Label result = referenceMapper.toEntity(savedLabel.getId(), Label.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedLabel.getId());
        assertThat(result.getName()).isEqualTo("Test Label");
    }
}
