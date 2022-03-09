package com.company;

import com.company.annotations.JsonElement;
import com.company.annotations.JsonSerialisable;

public class Example {

    @JsonSerialisable
    static class Person {
        @JsonElement
        String name = "nathan";
        @JsonElement(key = "years alive")
        int age = 18;
        @JsonElement
        int[] nums = {1, 2, 3};
    }

    public static void main(String[] args) {
        Person p = new Person();
        ObjectToJsonConverter converter = new ObjectToJsonConverter();
        String json = converter.convertToJson(p);
        System.out.printf("%s\n", json);
    }
}

