package pl.rynbou.langapi;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LangMassage {

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

    public LangMassage(ConfigurationSection section) {

        useChat = section.getBoolean("chat-enabled");
        if (useChat) {
            if (section.isString("chat.content")) {
                chatContent.add(section.getString("chat.content"));
            } else if (section.isList("chat.content")) {
                chatContent.addAll(section.getStringList("chat.content"));
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

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void broadcast(Replacement... replacements) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, replacements);
        }
    }

    public void send(Player player, Replacement... replacements) {
        if (useChat) {
            for (String chatMsg : chatContent) {
                player.sendMessage(replace(chatMsg, replacements));
            }
        }
        if (useTitle) {
            String titleMsg = replace(titleContent, replacements);
            String subtitleMsg = replace(subtitleContent, replacements);
            player.sendTitle(titleMsg, subtitleMsg, fadeIn, stay, fadeOut);
        }
        if (useActionBar) {
            String actionBarMsg = replace(actionBarContent, replacements);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMsg));
        }
    }

    private String replace(String msg, Replacement... replacements) {
        String toReturn = msg;
        for (Replacement r : replacements) {
            toReturn = toReturn.replace(r.getFrom(), r.getTo());
        }

        return toReturn;
    }
}
