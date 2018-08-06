---
title: Introduction
---

Below is a collection of heuristics and rules that we have developed after spending a significant amount of time analyzing YourKit profiles of control loops while developing our custom Real Time threading implementation.

These are not the general IHMC style-guide, but rather apply to any code that may be called inside of an embedded control loop; anything that appears inside of a doControl() method or methods invoked from inside of doControl().

