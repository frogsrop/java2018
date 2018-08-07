package ru.ifmo.rain.ugay.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;


public class StudentDB implements StudentGroupQuery {

    private List<Group> getGroups(Collection<Student> students, Function<Collection<Student>, List<Student>> f) {
        return students.stream().collect(groupingBy(Student::getGroup)).entrySet().stream().
                map(t -> new Group(t.getKey(), f.apply(t.getValue()))).sorted(Comparator.comparing(Group::getName)).collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroups(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroups(students, this::sortStudentsById);
    }

    private String getLargestGroups(Collection<Student> students, Comparator<Group> cmp) {
        return getGroupsByName(students).stream().max(cmp).orElse(new Group("", Collections.emptyList())).getName();
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroups(students, Comparator.comparing((Group x) -> x.getStudents().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroups(students, Comparator.comparing((Group x) -> getDistinctFirstNames(x.getStudents()).size()));
    }


    private List<String> getCollect(List<Student> students, Function<Student, String> f) {
        return students.stream().map(f).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getCollect(students, Student::getFirstName);
    }


    @Override
    public List<String> getLastNames(List<Student> students) {
        return getCollect(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getCollect(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getCollect(students, x -> x.getFirstName() + " " + x.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getFirstNames(students).stream().distinct().sorted().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Comparator.naturalOrder()).map(Student::getFirstName).orElse("");
    }

    private static List<Student> getSorted(Collection<Student> students, Comparator<Student> comp) {
        return students.stream().sorted(comp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return getSorted(students, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getSorted(students, Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName).thenComparing(Student::getId));
    }

    private List<Student> getStudents(Collection<Student> students, String name, Predicate<Student> f) {
        return sortStudentsByName(students.stream().filter(f).collect(Collectors.toList()));
    }
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return getStudents(students, name, std -> std.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return getStudents(students, name, std -> std.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return getStudents(students, group, std -> std.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream().filter(std -> std.getGroup().equals(group)).collect(toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
}
