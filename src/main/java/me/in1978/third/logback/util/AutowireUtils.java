//package me.in1978.third.logback.util;
//
//import ch.qos.logback.core.joran.spi.InterpretationContext;
//
//import javax.annotation.Resource;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.function.Predicate;
//import java.util.stream.Stream;
//
//public class AutowireUtils {
//
//    public static <T> T enhance(T obj, InterpretationContext ic) {
//        Class<?> clazz = obj.getClass();
//
//        allMethods(clazz)
//                .filter(m -> m.getAnnotation(Resource.class) != null)
//                .forEach(method -> {
//                    Resource anno = method.getAnnotation(Resource.class);
//                    String name = anno.name();
//                    Object value = ic.getContext().getObject(name);
//                    method.setAccessible(true);
//                    try {
//                        method.invoke(obj, value);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//
//        return obj;
//    }
//
//    private static Stream<Method> findMethods(Class<?> clazz, Predicate<Method> filter) {
//        return allMethods(clazz)
//                .filter(filter)
//                ;
//    }
//
//    private static Stream<Method> allMethods(Class<?> c) {
//        List<Class<?>> clist = new ArrayList<>();
//        for (Class<?> c2 = c; ; c2 = c2.getSuperclass()) {
//            clist.add(c2);
//
//            if (c2 == Object.class) break;
//        }
//        Collections.reverse(clist);
//
//        return clist.stream().flatMap(c2 -> Arrays.stream(c2.getDeclaredMethods()));
//    }
//
//}
