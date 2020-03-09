package pl.rynbou.langapi;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class LangAPI {

    private File file;
    private Logger logger;

    private Map<String, LangMassage> messages;

    public LangAPI(JavaPlugin plugin, String fileName) {
        plugin.saveResource(fileName, false);
        this.file = new File(plugin.getDataFolder(), fileName);
        this.logger = Bukkit.getLogger();
        reload();
    }

    public boolean sendMessage(String id, Player player, Replacement... replacements) {
        LangMassage message = messages.get(id);

        if (message == null) {
            logger.warning("[LangAPI] missing message: " + id);
            return false;
        }

        message.send(player, replacements);
        return true;
    }

    public boolean reload() {
        messages = new ConcurrentHashMap<>();

        ConfigurationSection section = YamlConfiguration.loadConfiguration(file).getConfigurationSection("messages");

        if (section == null) {
            logger.warning("[LangAPI] Missing messages section in file " + file.getName());
            return false;
        }

        for (String id : section.getKeys(false)) {
            messages.put(id, new LangMassage(section.getConfigurationSection(id)));
        }
        return true;
    }
}
