package pl.rynbou.langapi3;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class LangAPI {

    private final ConfigurationSection defaultSection;
    private final ConfigurationSection customSection;
    private final Logger log;

    private final Map<String, LangMessage> messages = new ConcurrentHashMap<>();

    public LangAPI(JavaPlugin plugin, String filename) {
        this.log = plugin.getLogger();

        plugin.saveResource(filename, false);
        File customFile = new File(plugin.getDataFolder(), filename);
        FileConfiguration customConfiguration = YamlConfiguration.loadConfiguration(customFile);
        this.customSection = customConfiguration.getConfigurationSection("messages");

        InputStream defaultStream = plugin.getClass().getResourceAsStream("/messages.yml");
        InputStreamReader defaultStreamReader = new InputStreamReader(defaultStream);
        FileConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(defaultStreamReader);
        this.defaultSection = defaultConfiguration.getConfigurationSection("messages");
    }

    private static Replacement[] convert(String[] strings) {
        int size = strings.length % 2 == 0 ? strings.length : strings.length - 1;
        Replacement[] replacements = new Replacement[size / 2];

        for (int i = 0; i < size / 2; i++) {
            replacements[i] = new Replacement(strings[i * 2], strings[i * 2 + 1]);
        }

        return replacements;
    }

    public boolean broadcast(String id, Replacement... replacements) {
        LangMessage message = messages.get(id);

        if (message == null) {
            log.warning("[LangAPI] missing message: " + id);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage("[LangAPI] missing message: " + id);
                List<String> replacementsFormatted = formatReplacements(replacements);
                if (replacementsFormatted.size() > 0) {
                    player.sendMessage("Replacements:");
                }
                replacementsFormatted.forEach(player::sendMessage);
            }

            return false;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            message.send(player, replacements);
        }
        return true;
    }

    public boolean broadcast(String id, String... strings) {
        return broadcast(id, convert(strings));
    }

    public boolean broadcast(String id) {
        return broadcast(id, "", "");
    }

    public boolean sendMessage(String id, CommandSender sender, Replacement... replacements) {
        LangMessage message = messages.get(id);

        if (message == null) {
            log.warning("[LangAPI] missing message: " + id);
            sender.sendMessage("[LangAPI] missing message: " + id);
            List<String> replacementsFormatted = formatReplacements(replacements);
            if (replacementsFormatted.size() > 0) {
                sender.sendMessage("Replacements:");
            }
            replacementsFormatted.forEach(sender::sendMessage);
            return false;
        }

        message.send(sender, replacements);
        return true;
    }

    public boolean sendMessage(String id, CommandSender sender, String... strings) {
        return sendMessage(id, sender, convert(strings));
    }

    public boolean sendMessage(String id, CommandSender sender) {
        return sendMessage(id, sender, "", "");
    }

    public boolean reload() {
        messages.clear();

        if (customSection == null || defaultSection == null) {
            log.warning("[LangAPI] Missing messages section!");
            return false;
        }

        for (String id : defaultSection.getKeys(false)) {
            messages.put(id, new LangMessage(defaultSection.getConfigurationSection(id)));
        }

        for (String id : customSection.getKeys(false)) {
            messages.put(id, new LangMessage(customSection.getConfigurationSection(id)));
        }

        return true;
    }

    private List<String> formatReplacements(Replacement... replacements) {
        return Arrays.stream(replacements)
                .filter(replacement -> !replacement.from.isEmpty())
                .filter(replacement -> !replacement.from.equals(replacement.to))
                .map(replacement -> "\"" + replacement.from + "\" = \"" + replacement.to + "\"")
                .collect(Collectors.toList());
    }

    public final static class Replacement {

        private final String from;
        private final String to;

        public Replacement(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    private final class LangMessage {

        private boolean useChat;
        private List<String> chatContent = new ArrayList<>();

        private boolean useTitle;
        private String titleContent;
        private String subtitleContent;
        private int fadeIn;
        private int stay;
        private int fadeOut;

        private boolean useActionBar;
        private String actionBarContent;

        private LangMessage(ConfigurationSection section) {

            useChat = section.getBoolean("chat-enabled");
            if (useChat) {
                if (section.isString("chat.content")) {
                    chatContent.add(color(section.getString("chat.content")));
                } else if (section.isList("chat.content")) {
                    chatContent = section.getStringList("chat.content").stream()
                            .map(this::color).collect(Collectors.toList());
                }
            }

            useTitle = section.getBoolean("title-enabled");
            if (useTitle) {
                titleContent = color(section.getString("title.content"));
                subtitleContent = color(section.getString("title.sub-content"));
                fadeIn = section.getInt("title.fade-in");
                stay = section.getInt("title.stay");
                fadeOut = section.getInt("title.fade-out");
            }

            useActionBar = section.getBoolean("actionbar-enabled");
            if (useActionBar) {
                actionBarContent = color(section.getString("actionbar.content"));
            }
        }

        private void send(Player player, Replacement... replacements) {
            if (useChat) {
                getChatContent(replacements).forEach(player::sendMessage);
            }
            if (useTitle) {
                String titleMsg = getTitleContent(replacements);
                String subtitleMsg = getSubtitleContent(replacements);
                player.sendTitle(titleMsg, subtitleMsg, fadeIn, stay, fadeOut);
            }
            if (useActionBar) {
                String actionBarMsg = getActionBarContent(replacements);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMsg));
            }
        }

        private void send(CommandSender sender, Replacement... replacements) {
            if (sender instanceof Player) {
                send((Player) sender, replacements);
                return;
            }
            if (useChat) {
                getChatContent(replacements).forEach(sender::sendMessage);
            }
        }

        private List<String> getChatContent(Replacement... replacements) {
            return chatContent.stream().map(s -> replace(s, replacements)).collect(Collectors.toList());
        }

        private String getTitleContent(Replacement... replacements) {
            return replace(titleContent, replacements);
        }

        private String getSubtitleContent(Replacement... replacements) {
            return replace(subtitleContent, replacements);
        }

        private String getActionBarContent(Replacement... replacements) {
            return replace(actionBarContent, replacements);
        }

        private String replace(String msg, Replacement... replacements) {
            String toReturn = msg;
            for (Replacement r : replacements) {
                toReturn = toReturn.replace(r.from, r.to);
            }

            return toReturn;
        }

        private String color(String msg) {
            return ChatColor.translateAlternateColorCodes('&', msg);
        }
    }
}
