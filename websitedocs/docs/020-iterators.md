---
title: Do not use iterators
---

**Exception**: Iterating over arrays (`object[] array = new object[x]`)

ArrayList iterators generate garbage. Instead of

```java
ArrayList<Bar> foos;
for(Bar foo : foos)
{
    // foo
}
```

Use

```java
ArrayList<Bar> foos;
for(int i = 0; i < foos.size(); i++)
{
    Bar foo = foos.get(i);
}
```

Make sure the underlying object is an ArrayList.

To clarify here: There is a difference between the general contract imposed by the `List<T>` type and the `ArrayList<T>` type.  `List<T>` is just an `Interface`, a behavior contract with very little implementation contract except for certain asymptotic performance guarantees inherited from `Collection`, and iteration of this nature will be slightly slower because the "default" `List` type is the `LinkedList`, which has non-constant time direct access characteristics.  An `ArrayList<T>` is guaranteed to be backed by an `array T[]`, which guarantees constant lookup performance. If you need to create a collection of things and then iterate over it at a later time in real-time critical code, make sure that it is an `ArrayList<T>` and not some indeterminate `List<T>`, and iterate over it using a classic for loop so that you don't generate garbage (the Enhanced For Loop spawns a new weakly referenced object implementing the `Iterator` interface, and incurs two method dispatch calls per iteration (`iterator.hasNext()` and `iterator.next()`, hence the performance/GC issues).

### Do not iterate over Maps and Sets

Iterating over maps and sets is expensive and generates garbage.

*Exception*: Iterating over an EnumMap using the following code
 
Constructor:
```java
enum foo;
EnumMap<foo> bar = new EnumMap<foo>(foo.class);
foo[] enumValues = foo.values();
```

Main loop:

```java
for(foo fooElement : enumValues)
{
    bar.get(fooElement);
}
```

