package pl.rynbou.langapi;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class LangMassage {

    private boolean useChat;
    private String chatContent;

    private boolean useTitle;
    private String titleContent;
    private String subtitleContent;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    private boolean useActionBar;
    private String actionBarContent;

    public LangMassage(ConfigurationSection section) {

        useChat = section.getBoolean("useChat");
        if (useChat) {
            chatContent = color(section.getString("chat.content"));
        }

        useTitle = section.getBoolean("useTitle");
        if (useTitle) {
            titleContent = color(section.getString("title.content"));
            subtitleContent = color(section.getString("title.sub-content"));
            fadeIn = section.getInt("title.fade-in");
            stay = section.getInt("title.stay");
            fadeOut = section.getInt("title.fade-out");
        }

        useActionBar = section.getBoolean("useActionBar");
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
        String chatMsg = replace(chatContent, replacements);
        String titleMsg = replace(titleContent, replacements);
        String subtitleMsg = replace(subtitleContent, replacements);
        String actionBarMsg = replace(actionBarContent, replacements);

        if (useChat) {
            player.sendMessage(chatMsg);
        }
        if (useTitle) {
            player.sendTitle(titleMsg, subtitleMsg, fadeIn, stay, fadeOut);
        }
        if (useActionBar) {
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
