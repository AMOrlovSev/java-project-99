package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;

import java.util.List;
import java.util.Optional;

public interface LabelService {
    List<Label> getAll();
    Optional<Label> findById(Long id);
    Label create(LabelCreateDTO labelData);
    Label update(Long id, LabelUpdateDTO labelData);
    void delete(Long id);
}
