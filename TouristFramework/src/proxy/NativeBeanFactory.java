package proxy;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import core.Component;

public class NativeBeanFactory<T> {

    private final Map<String, Class<?>> classMap = new HashMap<>();
    private final Class<T> interfaceType;
    private final String packageName;

    public NativeBeanFactory(String packageName, Class<T> interfaceType) {
        this.packageName = packageName;
        this.interfaceType = interfaceType;
        try {
            scan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 核心扫描逻辑
    private void scan() throws Exception {
        String path = packageName.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                String filePath = resource.getFile();
                filePath = java.net.URLDecoder.decode(filePath, "UTF-8");

                findClasses(new File(filePath), packageName);
            }
        }
    }


    private void findClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Component.class) && interfaceType.isAssignableFrom(clazz)) {
                    Component comp = clazz.getAnnotation(Component.class);
                    String name = comp.name().isEmpty() ? clazz.getSimpleName() : comp.name();
                    classMap.put(name, clazz);
                    classMap.put(clazz.getSimpleName(), clazz);
                }
            }
        }
    }


    public Set<String> getNames() {
        return classMap.keySet().stream()
                .filter(k -> !k.contains("."))
                .collect(Collectors.toSet());
    }


    public T createInstance(String name) throws Exception {
        Class<?> clazz = classMap.get(name);
        if (clazz == null) throw new IllegalArgumentException("找不到: " + name);

        return (T) clazz.getDeclaredConstructor().newInstance();
    }
}
