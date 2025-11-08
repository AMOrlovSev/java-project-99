package hexlet.code.service.impl;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    @Override
    public List<Label> getAll() {
        return labelRepository.findAll();
    }

    @Override
    public Optional<Label> findById(Long id) {
        return labelRepository.findById(id);
    }

    @Override
    public Label create(LabelCreateDTO labelData) {
        Label label = labelMapper.map(labelData);
        return labelRepository.save(label);
    }

    @Override
    public Label update(Long id, LabelUpdateDTO labelData) {
        Label labelToUpdate = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));

        labelMapper.update(labelData, labelToUpdate);
        return labelRepository.save(labelToUpdate);
    }

    @Override
    public void delete(Long id) {
        Label labelToDelete = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));

        labelRepository.delete(labelToDelete);
    }
}
