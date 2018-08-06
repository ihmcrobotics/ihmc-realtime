---
title: Do not use the object versions of primitives (Double, Integer, Boolean, Long, Character, Byte, Float, Short)
---

Autoboxing [http://docs.oracle.com/javase/tutorial/java/data/autoboxing.html](http://docs.oracle.com/javase/tutorial/java/data/autoboxing.html) is extremely slow and generates large amounts of garbage.

An example of using autoboxing in a list would be 

```java

double a = 10.0; // double primitive
Double b = 20.0; // Double object, note capital "D"

ArrayList<Double> terribleList = new ArrayList<>;   // Never do this

terribleList.add(a);    // this is equivalent to terribleList.add(new Double(a));
terribleList.add(b);

double c = terribleList.get(1);     // This now turns Double(20.0) back in a primitive double

```

If you want a collection of primitives, use the [Trove equivalents](http://trove.starlight-systems.com/)
