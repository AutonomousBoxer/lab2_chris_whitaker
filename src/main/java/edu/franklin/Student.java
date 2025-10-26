/**
 * Purpose: Panache entity for Student.
 * Author: Chris Whitaker
 */
package edu.franklin;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;


@Entity
public class Student extends PanacheEntity {
    @NotBlank(message = "name cannot be empty")
    public String name;

    @NotBlank(message = "phone cannot be empty")
    public String phone;

    @Min(value = 1, message = "grade must be >= 1")
    @Max(value = 12, message = "grade must be <= 12")
    public int grade;

    public String license;

    public static Student persistStudent(@Valid Student student) {
        student.persist();
        return student;
    }

    public static Student findStudentById(Long id) {
        return Student.findById(id);
    }

    public static Student findStudentByName(String name) {
        return Student.find("lower(name) = ?1", name.toLowerCase()).firstResult();
    }

    public static List<Student> findAllStudents() {
        return Student.listAll();
    }

    public static Student findRandomStudent() {
        return Student.find("order by random()").firstResult();
    }

    public static void deleteStudent(Long id) {
        Student.deleteById(id);
    }

    public static Student updateStudent(@Valid Student student) {
        if (student == null || student.id == null) {
            return null;
        }

        Student existing = Student.findById(student.id);
        if (existing == null) {
            return null;
        }

        existing.name = student.name;
        existing.phone = student.phone;
        existing.grade = student.grade;
        existing.license = student.license;
        return existing;
    }
}
