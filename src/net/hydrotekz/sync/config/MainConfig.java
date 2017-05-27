package net.hydrotekz.sync.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import net.hydrotekz.sync.net.SocketService;
import net.hydrotekz.sync.utils.CfgBox;
import net.hydrotekz.sync.utils.Printer;

public class MainConfig {

	public static Map<String, Object> configRoot;

	public static List<CfgBox> boxes = new ArrayList<CfgBox>();

	// Load config
	@SuppressWarnings("unchecked")
	public static void loadYAML(File file){
		try {
			InputStream input = new FileInputStream(file);
			Yaml yaml = new Yaml(new SafeConstructor());

			// Get root of config
			Map<String, Object> root = (Map<String, Object>) yaml.load(input);
			configRoot = root;

			// Load local settings
			Map<String, Object> settings = (Map<String, Object>) getValue(root, "Settings");
			int port = (int) getValue(settings, "Port");
			SocketService.port = port;

			// Get content of sync
			Map<String, Object> syncs = (Map<String, Object>) getValue(root, "Syncs");

			// Loop through syncs
			for (Entry<String, Object> entry : syncs.entrySet()){
				// Get entries within sync
				String name = entry.getKey();
				Map<String, Object> content = (Map<String, Object>) getValue(syncs, entry.getKey());

				// Finish and load list
				String path = (String) getValue(content, "Path");
				String tracker = (String) getValue(content, "Tracker");
				String key = (String) getValue(content, "Key");
				CfgBox cfgBox = new CfgBox(name, path, tracker, key);
				boxes.add(cfgBox);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// Get value of key in YAML config
	public static Object getValue(Map<String, Object> map, String key){
		for (Entry<String, Object> entry : map.entrySet()){
			if (entry.getKey().equalsIgnoreCase(key)){
				return entry.getValue();
			}
		}
		return null;
	}

	// Load config for launch
	public static void loadConfig() {
		try {
			File mainCfg = new File(System.getProperty("user.dir") + File.separator + "config.yml");
			loadYAML(mainCfg);

		} catch (Exception e){
			Printer.printError(e);
			Printer.printError("Failed to load config.");
			System.exit(0);
		}
	}
}