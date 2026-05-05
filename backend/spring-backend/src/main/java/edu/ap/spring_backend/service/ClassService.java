package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ClassRepository;
import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Class;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassService {
    private ClassRepository classRepository;
    public ClassService(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<ClassDTO> getClasses() {
        return classRepository.findAll().stream().map(this::mapToClassDTO).toList();
    }

    public List<ClassDTO> getClassesBySchoolId(Long schoolId) {
        return classRepository.findBySchoolId(schoolId).stream().map(this::mapToClassDTO).toList();
    }

    public ClassDTO getClassById(Long id) throws EntityNotFoundException {
        Class clazz = classRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToClassDTO(clazz);
    }

    public Class getClassByNameAndSchoolId(String name, Long schoolId) {
        Class clazz = classRepository.findByClassNameAndSchoolId(name, schoolId).orElse(null);
        return clazz;
    }

    public ClassDTO addClass(Class clazz) {
        Class clazz1 = classRepository.save(clazz);
        return mapToClassDTO(clazz1);
    }

    public void deleteClassById(Long id) {
        Class clazz = classRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        classRepository.delete(clazz);
    }

    private ClassDTO mapToClassDTO(Class clazz) {
        return new ClassDTO(clazz.getClassName(), clazz.getSchool().getId());
    }
}
