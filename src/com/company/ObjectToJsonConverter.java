package com.company;

import com.company.annotations.JsonElement;
import com.company.annotations.JsonSerialisable;
import com.company.util.GenericWrapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ObjectToJsonConverter {

    public String convertToJson(Object object) {
        checkIfSerializable(object);
        return formatJsonString(getJsonString(object));
    }

    private String formatJsonString(String json) {
        StringBuilder sb = new StringBuilder();
        int tabs = 0;
        for (int i = 0; i < json.length(); i++) {
            if ("{[".contains("" + json.charAt(i))) tabs++;
            if ("}]".contains("" + json.charAt(i))) tabs--;
            switch (json.charAt(i)) {
                case '{' -> sb.append("{").append("\n").append("\t".repeat(tabs));
                case '}' -> sb.append("\n").append("\t".repeat(tabs)).append("}");
                case '[' -> sb.append("[").append("\n").append("\t".repeat(tabs));
                case ']' -> sb.append("\n").append("\t".repeat(tabs)).append("]");
                case ',' -> sb.append(",\n").append("\t".repeat(tabs));
                case ':' -> sb.append(": ");
                default -> sb.append(json.charAt(i));
            }
        }
        return sb.toString();
    }

    private void checkIfSerializable(Object object) {
        if (Objects.isNull(object)) {
            throw new RuntimeException("Can't serialize a null object");
        }

        Class<?> clazz = object.getClass();
        if (!clazz.isAnnotationPresent(JsonSerialisable.class) && Map.class.equals(clazz)) {
            throw new RuntimeException("The class " + clazz.getSimpleName() + " is not annotated with JsonSerializable");
        }
    }

    private String getJsonString(Object object) {
        Class<?> clazz = object.getClass();
        Map<String, GenericWrapper<?>> jsonElementsMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(JsonElement.class)) {
                try {
                    jsonElementsMap.put(getKey(field), new GenericWrapper<>(field.get(object).getClass().cast(field.get(object))));
                } catch (Exception e) {
                    throw new RuntimeException("Could not access/cast the field: " + field.getName() + "'s value\n" + e.getMessage());
                }
            }
        }

        String jsonString = jsonElementsMap.entrySet()
                .stream()
                .map(this::entryToString)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        return "{" + jsonString + "}";
    }

    private final Map<String, Function<Map.Entry<String, GenericWrapper<?>>, String>> typeToStringMap = new HashMap<>() {{
        put("String", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("Character", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("Integer", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("Long", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("Float", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("Double", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("Boolean", entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().value + "\"");
        put("int[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((int[]) entry.getValue().value)
                .mapToObj(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("Integer[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Integer[]) entry.getValue().value)
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("long[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((long[]) entry.getValue().value)
                .mapToObj(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("Long[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Long[]) entry.getValue().value)
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("float[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream(floatArrayToFloatArray((float[]) entry.getValue().value))
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("Float[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Float[]) entry.getValue().value)
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("double[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((double[]) entry.getValue().value)
                .mapToObj(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("Double[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Double[]) entry.getValue().value)
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("char[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream(charArrayToCharacterArray((char[]) entry.getValue().value))
                .map(i -> "\"" + i + "\"")
                .collect(Collectors.joining(","))
                + "]");
        put("Character[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Character[]) entry.getValue().value)
                .map(i -> "\"" + i + "\"")
                .collect(Collectors.joining(","))
                + "]");
        put("boolean[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream(booleanArrayToBooleanArray((boolean[]) entry.getValue().value))
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("Boolean[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Boolean[]) entry.getValue().value)
                .map(i -> "" + i)
                .collect(Collectors.joining(","))
                + "]");
        put("String[]", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((String[]) entry.getValue().value)
                .map(i -> "\"" + i + "\"")
                .collect(Collectors.joining(","))
                + "]");
        put("CustomObject", entry -> {
            String returned = convertToJson(entry.getValue().value);
            if (!returned.equals("{}")) {
                // object had specified fields
                return "\"" + entry.getKey() + "\":" + returned;
            } else {
                // no fields were specified so return nothing
                // null instead of "" as its easier to filter from a stream
                return null;
            }
        });
        put("CustomObjectArray", entry -> "\"" + entry.getKey() + "\":[" + Arrays.stream((Object[]) entry.getValue().value)
                .map(e -> new ObjectToJsonConverter().convertToJson(e))
                .collect(Collectors.joining(","))
                + "]");
    }};

    private String entryToString(Map.Entry<String, GenericWrapper<?>> entry) {
        Class<?> clazz = entry.getValue().value.getClass();
        if (typeToStringMap.containsKey(clazz.getSimpleName())) {
            // standard implemented type | custom implementation has been given
            return typeToStringMap.get(clazz.getSimpleName()).apply(entry);
        } else {
            // non-standard implemented type
            if (entry.getValue().value.getClass().isArray()) {
                return typeToStringMap.get("CustomObjectArray").apply(entry);
            } else {
                return typeToStringMap.get("CustomObject").apply(entry);
            }
        }
    }

    private Float[] floatArrayToFloatArray(float[] fs) {
        Float[] Fs = new Float[fs.length];
        for (int i = 0; i < fs.length; i++) {
            Fs[i] = fs[i];
        }
        return Fs;
    }

    private Character[] charArrayToCharacterArray(char[] cs) {
        Character[] Cs = new Character[cs.length];
        for (int i = 0; i < cs.length; i++) {
            Cs[i] = cs[i];
        }
        return Cs;
    }

    private Boolean[] booleanArrayToBooleanArray(boolean[] bs) {
        Boolean[] Bs = new Boolean[bs.length];
        for (int i = 0; i < bs.length; i++) {
            Bs[i] = bs[i];
        }
        return Bs;
    }

    private String getKey(Field field) {
        String value = field.getAnnotation(JsonElement.class)
                .key();
        return value.isEmpty() ? field.getName() : value;
    }
}
