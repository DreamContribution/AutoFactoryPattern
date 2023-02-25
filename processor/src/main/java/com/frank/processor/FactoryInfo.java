package com.frank.processor;

import java.util.HashSet;
import java.util.Set;

class FactoryInfo {
   Class<?> methodReturnType;
   String factoryName;
   Set<ClassInfo> subClassInfo = new HashSet<>();
}
