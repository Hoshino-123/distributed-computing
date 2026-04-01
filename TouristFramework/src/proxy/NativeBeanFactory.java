package proxy;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
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
        // 1. 将包名转为路径 (例如 "proxy" -> "proxy/")
        String path = packageName.replace('.', '/');

        // 2. 获取类加载器，找到该路径下的所有资源（文件夹）
        // 这一步是为了兼容不同的运行环境
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            // 处理文件协议（最常见的情况，即在本地文件夹中）
            if (resource.getProtocol().equals("file")) {
                String filePath = resource.getFile();
                // 解码 URL 编码（防止中文乱码）
                filePath = java.net.URLDecoder.decode(filePath, "UTF-8");

                // 3. 递归查找该文件夹下的所有 .class 文件
                findClasses(new File(filePath), packageName);
            }
        }
        System.out.println("🔍 原生扫描完成: 发现 " + classMap.size()/2 + " 个 " + interfaceType.getSimpleName());
    }

    // 递归查找类文件
    private void findClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是子包，递归查找 (例如 proxy.vehicle)
                findClasses(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                // 如果是 .class 文件，加载它
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                // 4. 检查是否加了 @app.Component 注解 且 实现了目标接口
                if (clazz.isAnnotationPresent(Component.class) && interfaceType.isAssignableFrom(clazz)) {
                    Component comp = clazz.getAnnotation(Component.class);
                    String name = comp.name().isEmpty() ? clazz.getSimpleName() : comp.name();
                    classMap.put(name, clazz);
                    classMap.put(clazz.getSimpleName(), clazz);
                }
            }
        }
    }

    // 获取所有名称
    public Set<String> getNames() {
        return classMap.keySet().stream()
                .filter(k -> !k.contains("."))
                .collect(Collectors.toSet());
    }

    // 获取代理实例
    public T createInstance(String name) throws Exception {
        Class<?> clazz = classMap.get(name);
        if (clazz == null) throw new IllegalArgumentException("❌ 找不到: " + name);

        // 只返回真实的实现类实例
        return (T) clazz.getDeclaredConstructor().newInstance();
    }
}