package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ClassRepository;
import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.SchoolClass;
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
        SchoolClass clazz = classRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToClassDTO(clazz);
    }

    public SchoolClass getClassByNameAndSchoolId(String name, Long schoolId) {
        SchoolClass clazz = classRepository.findByClassNameAndSchoolId(name, schoolId).orElse(null);
        return clazz;
    }

    public ClassDTO addClass(SchoolClass clazz) {
        SchoolClass clazz1 = classRepository.save(clazz);
        return mapToClassDTO(clazz1);
    }

    public void deleteClassById(Long id) {
        SchoolClass clazz = classRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        classRepository.delete(clazz);
    }

    private ClassDTO mapToClassDTO(SchoolClass clazz) {
        return new ClassDTO(clazz.getId() ,clazz.getClassName(), clazz.getSchool().getId());
    }
}
