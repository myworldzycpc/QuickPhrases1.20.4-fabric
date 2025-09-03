package io.github.myworldzycpc.quick_phrases;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PhraseNodesSerializer {

    // 将 PhraseNodes 保存到文件
    public static void save(File file, PhraseNodes nodes) throws IOException {
        Yaml yaml = createYaml();
        Map<String, Object> data = convertToMap(nodes);

        boolean wasSuccessful = file.getParentFile().mkdirs(); // 确保目录存在
        if (!wasSuccessful) {
            System.err.println("Failed to create directory: " + file.getParentFile());
        }
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(data, writer);
        }
    }

    // 从文件加载 PhraseNodes
    public static PhraseNodes load(File file) throws IOException {
        if (!file.exists()) return new PhraseNodes();

        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> data = yaml.load(reader);
            if (data == null) return new PhraseNodes();
            return convertFromMap(data);
        }
    }

    // 递归转换 PhraseNodes -> Map (保留嵌套结构)
    private static Map<String, Object> convertToMap(PhraseNodes nodes) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : nodes.nodes.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof PhraseNodes) {
                map.put(entry.getKey(), convertToMap((PhraseNodes) value));
            } else if (value instanceof Leaf) {
                map.put(entry.getKey(), convertToMap((Leaf) value));
            } else {
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    private static String convertToMap(Leaf leaf) {
        if (leaf.getType() == Leaf.Type.CHAT_MESSAGE) {
            return leaf.getContent();
        } else {
            return leaf.getType().name();
        }
    }

    // 递归转换 Map -> PhraseNodes
    private static PhraseNodes convertFromMap(Map<String, Object> map) {
        HashMap<String, Object> nodes = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) value;
                nodes.put(entry.getKey().toUpperCase(), convertFromMap(subMap));
            } else {
                nodes.put(entry.getKey().toUpperCase(), new Leaf(value.toString()));
            }
        }
        return new PhraseNodes(nodes);
    }

    // 配置 YAML 输出格式
    private static Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2); // 缩进2空格
        options.setPrettyFlow(true); // 美化输出
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // 块状格式

        return new Yaml(options);
    }
}