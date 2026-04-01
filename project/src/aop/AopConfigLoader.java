package aop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AopConfigLoader {

    // 缓存所有配置 <"类名#方法名", "指令内容">
    // 例如："app.Car#drive.before" -> "call:app.CarHelper.startEngine"
    private static final Map<String, String> configMap = new HashMap<>();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties prop = new Properties();
        InputStream rawStream = AopConfigLoader.class.getClassLoader()
                .getResourceAsStream("aop-config.txt");

        if (rawStream == null) {
            System.err.println("⚠️ 未找到配置文件");
            return;
        }

        try {
            InputStreamReader reader = new InputStreamReader(rawStream, StandardCharsets.UTF_8);
            prop.load(reader);

            // 把配置读进 Map
            for (String key : prop.stringPropertyNames()) {
                configMap.put(key, prop.getProperty(key));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 核心方法：执行配置
    // 这个方法不再返回 String，而是直接执行动作 (void)
    public static void execute(Object target, String methodName, String type) {
        String className = target.getClass().getName();

        // 1. 拼接 Key，先找特定的 (如 app.Car#drive.before)
        String specificKey = className + "#" + methodName + "." + type;
        String instruction = configMap.get(specificKey);

        // 2. 如果特定的没找到，找通配符 (*#drive.before)
        if (instruction == null) {
            String wildcardKey = "*#" + methodName + "." + type;
            instruction = configMap.get(wildcardKey);
        }

        // 3. 如果找到了配置，就解析并执行
        if (instruction != null) {
            parseAndExecute(instruction);
        }
    }

    // 解析指令并执行
    private static void parseAndExecute(String instruction) {
        int commentIndex = instruction.indexOf("#");
        if (commentIndex != -1) {
            instruction = instruction.substring(0, commentIndex).trim(); // trim() 去掉末尾可能的空格
        }



        if (instruction.startsWith("print:")) {
            // 处理打印
            String text = instruction.substring(6); // 去掉 "print:" 前缀
            System.out.println(text);

        } else if (instruction.startsWith("call:")) {
            // 处理调用方法
            String methodPath = instruction.substring(5); // 去掉 "call:" 前缀
            try {
                // 分割字符串，获取类名和方法名
                // 例如：app.Utils.logFinish -> ["app.Utils", "logFinish"]
                int lastDotIndex = methodPath.lastIndexOf(".");
                String className = methodPath.substring(0, lastDotIndex);
                String methodName = methodPath.substring(lastDotIndex + 1);

                // 1. 加载类
                Class<?> clazz = Class.forName(className);
                // 2. 获取方法 (静态方法)
                Method method = clazz.getMethod(methodName);
                // 3. 调用方法 (因为是静态的，所以第一个参数传 null)
                method.invoke(null);

            } catch (Exception e) {
                System.err.println("❌ 执行配置方法失败: " + instruction);
                e.printStackTrace();
            }
        }
        // 如果指令不是 print 也不是 call，就忽略
    }
}
