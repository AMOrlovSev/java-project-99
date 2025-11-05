package hexlet.code.mapper;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
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
        Label label = new Label();
        label.setName("Test Label");
        Label savedLabel = labelRepository.save(label);

        Label result = referenceMapper.toEntity(savedLabel.getId(), Label.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedLabel.getId());
        assertThat(result.getName()).isEqualTo("Test Label");
    }
}
