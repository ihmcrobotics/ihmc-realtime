---
title: Preallocate everything
---

The first rule is that you want to preallocate all your objects and memory. The best way to guarantee that is to make all your class fields "final".

### Avoid garbage generation at all costs
 
***Exception***: Objects send irregulary over the network.

Our Realtime hacks do not extend to the garbage collector. A garbage collection cycle will crash the robot, possibly ruining it.

Writing garbage free code is easier when doing it immediately than refactoring later on. Even when testing things out, do not generate garbage, it is not that hard.



