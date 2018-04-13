/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.blueprint.plugin;

import org.apache.aries.blueprint.plugin.handlers.Handlers;
import org.apache.xbean.finder.ClassFinder;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FilteredClassFinder {

    @SuppressWarnings("unchecked")
    static Set<Class<?>> findClasses(PackageScopeClassFinder finder, Collection<String> packageNames) {
        return findClasses(finder, packageNames, Handlers.BEAN_MARKING_ANNOTATION_CLASSES.toArray(new Class[Handlers.BEAN_MARKING_ANNOTATION_CLASSES.size()]));
    }

    private static Set<Class<?>> findClasses(PackageScopeClassFinder finder, Collection<String> packageNames, Class<? extends Annotation>[] annotations) {
        return new HashSet<>(finder.findAnnotatedClassesFromBasePackages(annotations, packageNames));
    }

}

class PackageScopeClassFinder extends ClassFinder {
    PackageScopeClassFinder(ClassLoader classLoader, Collection<URL> urls) {
        super(classLoader, urls);
    }

    PackageScopeClassFinder(ClassLoader classLoader) throws Exception {
        super(classLoader);
    }

    List<Class<?>> findAnnotatedClassesFromBasePackages(Class<? extends Annotation>[] annotations, Collection<String> packageNames) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        Set<Info> toCheck = new HashSet<>();
        for (Class<? extends Annotation> annotation : annotations) {
            toCheck.addAll(getAnnotationInfos(annotation.getName()));
        }
        for (Info info : toCheck) {
            if (info instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) info;
                if (classFromBasePackage(classInfo, packageNames)) {
                    try {
                        Class clazz = classInfo.get();
                        if (classHasAtLeastOneAnnotation(clazz, annotations)) {
                            classes.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Class not found", e);
                    }
                }
            }
        }
        return classes;
    }

    private boolean classHasAtLeastOneAnnotation(Class clazz, Class<? extends Annotation>[] annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (clazz.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean classFromBasePackage(ClassInfo classInfo, Collection<String> packageNames) {
        for (String packageName : packageNames) {
            if (classInfo.getPackageName().startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }
}

