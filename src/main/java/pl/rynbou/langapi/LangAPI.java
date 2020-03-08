package pl.rynbou.langapi;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LangAPI {

    private File file;

    private Map<String, LangMassage> messages;

    public LangAPI(JavaPlugin plugin, String fileName) {
        plugin.saveResource(fileName, false);
        this.file = new File(plugin.getDataFolder(), fileName);
        reload();
    }

    public boolean sendMessage(String id, Player player, Replacement... replacements) {
        LangMassage message = messages.get(id);

        if (message == null) {
            return false;
        }

        message.send(player, replacements);
        return true;
    }

    public boolean reload() {
        messages = new ConcurrentHashMap<>();

        ConfigurationSection section = YamlConfiguration.loadConfiguration(file).getConfigurationSection("messages");

        if (section == null) {
            return false;
        }

        for (String id : section.getKeys(false)) {
            messages.put(id, new LangMassage(section.getConfigurationSection(id)));
        }
        return true;
    }
}
