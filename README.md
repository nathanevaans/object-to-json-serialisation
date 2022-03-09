# object to json serialisation

- Annotate a class with ```@JsonSerialisable``` and the class fields you wish to be serialised with ```@JsonElement```
- The json key will be the name of the field annotated by default
- The json key can be set to something else by using ```@JsonElement(key = "desired key")```

```java
@JsonSerialisable
public class Person {
  @JsonElement
  String name = "nathan";
  
  @JsonElement(key = "years alive")
  int age = 100;
  
  @JsonElement
  int[] nums = {1, 2, 3};
}
```

Would become:

```json
{
  name: "nathan",
  years alive: 100,
  nums: [
    1,
    2,
    3
  ]
}
```

## HOW TO GET THE JSON

```java
ObjectToJsonConverter converter = new ObjectToJsonConverter();
String json = conveter.convertToJson(obj);
```

## SUPPORTED FIELD TYPES

- String
- char
- Character
- int
- Integer
- long
- Long
- float
- Float
- double
- Double
- bool
- Boolean
- int[]
- Integer[]
- long[]
- Long[]
- float[]
- Float[]
- double[]
- Double[]
- char[]
- Character[]
- boolean[]
- Boolean[]
- String[]
- Custom Objects 
- Custom Object Arrays

