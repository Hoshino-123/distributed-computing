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

    private static final Map<String, String> configMap = new HashMap<>();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties prop = new Properties();
        InputStream rawStream = AopConfigLoader.class.getClassLoader()
                .getResourceAsStream("aop-config.txt");

        if (rawStream == null) {
            System.err.println("未找到配置文件");
            return;
        }

        try {
            InputStreamReader reader = new InputStreamReader(rawStream, StandardCharsets.UTF_8);
            prop.load(reader);

            //  将配置文件中的键值对存入 configMap
            for (String key : prop.stringPropertyNames()) {
                configMap.put(key, prop.getProperty(key));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void execute(Object target, String methodName, String type) {
        String className = target.getClass().getName();

        // 1. 拼接 Key，先找特定的
        String specificKey = className + "#" + methodName + "." + type;
        String instruction = configMap.get(specificKey);

        // 2. 如果特定的没找到，找通配符
        if (instruction == null) {
            String wildcardKey = "*#" + methodName + "." + type;
            instruction = configMap.get(wildcardKey);
        }

        // 3. 如果找到了配置，就解析并执行
        if (instruction != null) {
            if (instruction.contains(";")) {
                String[] instructions = instruction.split(";");
                for (String singleInstruction : instructions) {
                    parseAndExecute(singleInstruction.trim());
                }
            } else {

                parseAndExecute(instruction);
            }
        }
    }

    private static void parseAndExecute(String instruction) {
        int commentIndex = instruction.indexOf("#");
        if (commentIndex != -1) {
            instruction = instruction.substring(0, commentIndex).trim();
        }



        if (instruction.startsWith("print:")) {
            String text = instruction.substring(6);
            System.out.println(text);

        } else if (instruction.startsWith("call:")) {
            String methodPath = instruction.substring(5);
            try {

                int lastDotIndex = methodPath.lastIndexOf(".");
                String className = methodPath.substring(0, lastDotIndex);
                String methodName = methodPath.substring(lastDotIndex + 1);


                Class<?> clazz = Class.forName(className);
                Method method = clazz.getMethod(methodName);
                method.invoke(null);

            } catch (Exception e) {
                System.err.println("执行配置方法失败: " + instruction);
                e.printStackTrace();
            }
        }
    }
}
