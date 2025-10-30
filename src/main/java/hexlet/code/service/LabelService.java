package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceAlreadyExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    public List<Label> getAll() {
        return labelRepository.findAll();
    }

    public Optional<Label> findById(Long id) {
        return labelRepository.findById(id);
    }

    public Label create(LabelCreateDTO labelData) {
        if (labelRepository.existsByName(labelData.getName())) {
            throw new ResourceAlreadyExistsException("Label with name " + labelData.getName() + " already exists");
        }

        Label label = labelMapper.map(labelData);
        return labelRepository.save(label);
    }

    public Label update(Long id, LabelUpdateDTO labelData) {
        Label labelToUpdate = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));

        if (labelData.getName() != null && labelData.getName().isPresent()) {
            String newName = labelData.getName().get();
            if (!newName.equals(labelToUpdate.getName()) && labelRepository.existsByName(newName)) {
                throw new ResourceAlreadyExistsException("Label with name " + newName + " already exists");
            }
        }

        labelMapper.update(labelData, labelToUpdate);
        return labelRepository.save(labelToUpdate);
    }

    public void delete(Long id) {
        Label labelToDelete = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));

        if (labelRepository.existsByIdAndTasksIsNotEmpty(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete label with associated tasks");
        }

        labelRepository.deleteById(id);
    }
}
