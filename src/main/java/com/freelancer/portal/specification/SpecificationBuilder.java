package com.freelancer.portal.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic specification builder for creating dynamic queries
 * with Spring Data JPA's Specification API.
 * 
 * @param <T> The entity type for the specification
 */
public class SpecificationBuilder<T> {
    
    private final List<Specification<T>> specifications = new ArrayList<>();
    
    /**
     * Create a specification based on search criteria from request parameters
     * 
     * @param searchParams Search parameters as key-value pairs
     * @return A combined specification with all search criteria
     */
    public Specification<T> buildSpecification(Map<String, String> searchParams) {
        searchParams.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                parseSearchParam(key, value);
            }
        });
        
        return specifications.isEmpty() ? null : 
            specifications.stream().reduce(Specification::and).orElse(null);
    }

    /**
     * Parse a search parameter and add the corresponding specification
     * 
     * @param key The parameter key
     * @param value The parameter value
     */
    private void parseSearchParam(String key, String value) {
        if (key.contains("_")) {
            String[] parts = key.split("_", 2);
            String field = parts[0];
            String operation = parts[1].toLowerCase();
            
            switch (operation) {
                case "eq":
                    specifications.add(equal(field, value));
                    break;
                case "neq":
                    specifications.add(notEqual(field, value));
                    break;
                case "gt":
                    specifications.add(greaterThan(field, value));
                    break;
                case "lt":
                    specifications.add(lessThan(field, value));
                    break;
                case "like":
                    specifications.add(like(field, value));
                    break;
                case "in":
                    specifications.add(in(field, value.split(",")));
                    break;
                case "between":
                    String[] range = value.split(",");
                    if (range.length == 2) {
                        specifications.add(between(field, range[0], range[1]));
                    }
                    break;
                case "isnull":
                    specifications.add(isNull(field));
                    break;
                case "notnull":
                    specifications.add(isNotNull(field));
                    break;
                case "join":
                    String[] joinParts = value.split(":");
                    if (joinParts.length == 3) {
                        specifications.add(joinProperty(field, joinParts[0], joinParts[1], joinParts[2]));
                    }
                    break;
            }
        } else {
            // Default to equals if no operation is specified
            specifications.add(equal(key, value));
        }
    }

    /**
     * Create a specification for equality comparison
     */
    public Specification<T> equal(String attribute, String value) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            Class<?> type = path.getJavaType();
            
            if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                return cb.equal(path, Boolean.valueOf(value));
            } else if (type.isEnum()) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Class<Enum> enumType = (Class<Enum>) type;
                return cb.equal(path, Enum.valueOf(enumType, value));
            } else if (Number.class.isAssignableFrom(type)) {
                return cb.equal(path, parseNumber(type, value));
            } else {
                return cb.equal(path, value);
            }
        };
    }

    /**
     * Create a specification for inequality comparison
     */
    public Specification<T> notEqual(String attribute, String value) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            return cb.notEqual(path, value);
        };
    }

    /**
     * Create a specification for greater than comparison
     */
    public Specification<T> greaterThan(String attribute, String value) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            Class<?> type = path.getJavaType();
            
            if (LocalDate.class.equals(type)) {
                LocalDate date = LocalDate.parse(value);
                @SuppressWarnings("unchecked")
                Path<LocalDate> datePath = (Path<LocalDate>) path;
                return cb.greaterThan(datePath, date);
            } else if (LocalDateTime.class.equals(type)) {
                LocalDateTime dateTime = LocalDateTime.parse(value);
                @SuppressWarnings("unchecked")
                Path<LocalDateTime> dateTimePath = (Path<LocalDateTime>) path;
                return cb.greaterThan(dateTimePath, dateTime);
            } else if (Number.class.isAssignableFrom(type)) {
                Number number = parseNumber(type, value);
                @SuppressWarnings("unchecked")
                Path<Number> numberPath = (Path<Number>) path;
                return cb.gt(numberPath, number);
            } else {
                @SuppressWarnings("unchecked")
                Path<String> stringPath = (Path<String>) path;
                return cb.greaterThan(stringPath, value);
            }
        };
    }

    /**
     * Create a specification for less than comparison
     */
    public Specification<T> lessThan(String attribute, String value) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            Class<?> type = path.getJavaType();
            
            if (LocalDate.class.equals(type)) {
                LocalDate date = LocalDate.parse(value);
                @SuppressWarnings("unchecked")
                Path<LocalDate> datePath = (Path<LocalDate>) path;
                return cb.lessThan(datePath, date);
            } else if (LocalDateTime.class.equals(type)) {
                LocalDateTime dateTime = LocalDateTime.parse(value);
                @SuppressWarnings("unchecked")
                Path<LocalDateTime> dateTimePath = (Path<LocalDateTime>) path;
                return cb.lessThan(dateTimePath, dateTime);
            } else if (Number.class.isAssignableFrom(type)) {
                Number number = parseNumber(type, value);
                @SuppressWarnings("unchecked")
                Path<Number> numberPath = (Path<Number>) path;
                return cb.lt(numberPath, number);
            } else {
                @SuppressWarnings("unchecked")
                Path<String> stringPath = (Path<String>) path;
                return cb.lessThan(stringPath, value);
            }
        };
    }

    /**
     * Create a specification for LIKE comparison (partial match)
     */
    public Specification<T> like(String attribute, String value) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            @SuppressWarnings("unchecked")
            Path<String> stringPath = (Path<String>) path;
            return cb.like(cb.lower(stringPath), "%" + value.toLowerCase() + "%");
        };
    }

    /**
     * Create a specification for IN comparison (value in list)
     */
    public Specification<T> in(String attribute, String[] values) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            CriteriaBuilder.In<Object> inClause = cb.in(path);
            for (String value : values) {
                inClause.value(value);
            }
            return inClause;
        };
    }

    /**
     * Create a specification for BETWEEN comparison (value in range)
     */
    public Specification<T> between(String attribute, String value1, String value2) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            Class<?> type = path.getJavaType();
            
            if (LocalDate.class.equals(type)) {
                LocalDate date1 = LocalDate.parse(value1);
                LocalDate date2 = LocalDate.parse(value2);
                @SuppressWarnings("unchecked")
                Path<LocalDate> datePath = (Path<LocalDate>) path;
                return cb.between(datePath, date1, date2);
            } else if (LocalDateTime.class.equals(type)) {
                LocalDateTime dateTime1 = LocalDateTime.parse(value1);
                LocalDateTime dateTime2 = LocalDateTime.parse(value2);
                @SuppressWarnings("unchecked")
                Path<LocalDateTime> dateTimePath = (Path<LocalDateTime>) path;
                return cb.between(dateTimePath, dateTime1, dateTime2);
            } else if (Integer.class.equals(type) || int.class.equals(type)) {
                Integer num1 = Integer.parseInt(value1);
                Integer num2 = Integer.parseInt(value2);
                @SuppressWarnings("unchecked")
                Path<Integer> numPath = (Path<Integer>) path;
                return cb.between(numPath, num1, num2);
            } else if (Long.class.equals(type) || long.class.equals(type)) {
                Long num1 = Long.parseLong(value1);
                Long num2 = Long.parseLong(value2);
                @SuppressWarnings("unchecked")
                Path<Long> numPath = (Path<Long>) path;
                return cb.between(numPath, num1, num2);
            } else if (Double.class.equals(type) || double.class.equals(type)) {
                Double num1 = Double.parseDouble(value1);
                Double num2 = Double.parseDouble(value2);
                @SuppressWarnings("unchecked")
                Path<Double> numPath = (Path<Double>) path;
                return cb.between(numPath, num1, num2);
            } else if (Float.class.equals(type) || float.class.equals(type)) {
                Float num1 = Float.parseFloat(value1);
                Float num2 = Float.parseFloat(value2);
                @SuppressWarnings("unchecked")
                Path<Float> numPath = (Path<Float>) path;
                return cb.between(numPath, num1, num2);
            } else {
                @SuppressWarnings("unchecked")
                Path<String> stringPath = (Path<String>) path;
                return cb.between(stringPath, value1, value2);
            }
        };
    }

    /**
     * Create a specification for IS NULL comparison
     */
    public Specification<T> isNull(String attribute) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            return cb.isNull(path);
        };
    }

    /**
     * Create a specification for IS NOT NULL comparison
     */
    public Specification<T> isNotNull(String attribute) {
        return (root, query, cb) -> {
            Path<?> path = getPath(root, attribute);
            return cb.isNotNull(path);
        };
    }

    /**
     * Create a specification for joined entity property comparison
     */
    public Specification<T> joinProperty(String joinTable, String joinProperty, String operator, String value) {
        return (root, query, cb) -> {
            Join<T, ?> join = root.join(joinTable);
            
            switch (operator.toLowerCase()) {
                case "eq":
                    return cb.equal(join.get(joinProperty), value);
                case "like":
                    return cb.like(cb.lower(join.get(joinProperty).as(String.class)), 
                            "%" + value.toLowerCase() + "%");
                default:
                    return cb.equal(join.get(joinProperty), value);
            }
        };
    }

    /**
     * Get a path for an attribute, handling nested properties
     */
    private Path<?> getPath(Root<T> root, String attribute) {
        Path<?> path = root;
        if (attribute.contains(".")) {
            String[] parts = attribute.split("\\.");
            for (String part : parts) {
                path = path.get(part);
            }
            return path;
        } else {
            return root.get(attribute);
        }
    }

    /**
     * Parse a string value to appropriate number type
     */
    private Number parseNumber(Class<?> type, String value) {
        if (Long.class.equals(type) || long.class.equals(type)) {
            return Long.valueOf(value);
        } else if (Integer.class.equals(type) || int.class.equals(type)) {
            return Integer.valueOf(value);
        } else if (Double.class.equals(type) || double.class.equals(type)) {
            return Double.valueOf(value);
        } else if (Float.class.equals(type) || float.class.equals(type)) {
            return Float.valueOf(value);
        }
        return null;
    }
}