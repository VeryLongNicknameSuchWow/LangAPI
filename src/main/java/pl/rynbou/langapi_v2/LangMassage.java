package pl.rynbou.langapi_v2;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                chatContent.add(color(section.getString("chat.content")));
            } else if (section.isList("chat.content")) {
                chatContent = section.getStringList("chat.content").stream()
                        .map(LangMassage::color).collect(Collectors.toList());
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

    public void broadcast(Replacement... replacements) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, replacements);
        }
    }

    public void send(Player player, Replacement... replacements) {
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

    public boolean useChat() {
        return useChat;
    }

    public List<String> getChatContent(Replacement... replacements) {
        return chatContent.stream().map(s -> replace(s, replacements)).collect(Collectors.toList());
    }

    public boolean useTitle() {
        return useTitle;
    }

    public String getTitleContent(Replacement... replacements) {
        return replace(titleContent, replacements);
    }

    public String getSubtitleContent(Replacement... replacements) {
        return replace(subtitleContent, replacements);
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public boolean useActionBar() {
        return useActionBar;
    }

    public String getActionBarContent(Replacement... replacements) {
        return replace(actionBarContent, replacements);
    }

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String replace(String msg, Replacement... replacements) {
        String toReturn = msg;
        for (Replacement r : replacements) {
            toReturn = toReturn.replace(r.getFrom(), r.getTo());
        }

        return toReturn;
    }
}
