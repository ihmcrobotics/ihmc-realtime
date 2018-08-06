---
title: Do not change collections at runtime
---

Do not add/remove items to collections (`List`, `Map`, `Set`) during the control loop. Growing an `ArrayList` is extremely expensive.

