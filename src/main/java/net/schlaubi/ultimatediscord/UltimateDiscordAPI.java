package net.schlaubi.ultimatediscord;

import net.dv8tion.jda.core.JDA;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.schlaubi.ultimatediscord.bungee.Main;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.entity.Player;

public class UltimateDiscordAPI {

    public static boolean isVerified(String discordid){
        return MySQL.userExists(discordid);
    }

    public static boolean isVerified(Player player){
        return MySQL.userExists(player);
    }

    public static boolean isVerified(ProxiedPlayer proxiedPlayer){
        return MySQL.userExists(proxiedPlayer);
    }

    public static String getUserName(String discordid){
        return MySQL.getValue(discordid, "uuid");
    }

    public static String getDiscordId(Player player){
        return MySQL.getValue(player, "discordid");
    }

    public static String getDiscordId(ProxiedPlayer proxiedPlayer){
        return MySQL.getValue(proxiedPlayer, "discordid");
    }

    public static JDA getBungeeCordJDA(){
        return Main.jda;
    }
    public static JDA getSpigotJDA(){
        return net.schlaubi.ultimatediscord.spigot.Main.jda;
    }
}
