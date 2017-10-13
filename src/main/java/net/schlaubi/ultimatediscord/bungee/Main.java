package net.schlaubi.ultimatediscord.bungee;

import com.google.common.io.ByteStreams;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.schlaubi.ultimatediscord.util.MySQL;

import javax.security.auth.login.LoginException;
import java.io.*;

public class Main extends Plugin{

    private static Configuration configuration;
    public static JDA jda;
    private static Main instance;



    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        MySQL.connect(getConfiguration());
        MySQL.createDatabase();
        startBot();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new CommandDiscord("discord"));
    }

    private void startBot() {
        Configuration cfg = getConfiguration();
        JDABuilder bot = new JDABuilder(AccountType.BOT);
        bot.setAutoReconnect(true);
        bot.setToken(cfg.getString("Discord.token"));
        bot.setGame(Game.of(cfg.getString("Discord.game")));
        bot.addEventListener(new MessageListener());

        try {
            jda = bot.buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            ProxyServer.getInstance().getConsole().sendMessage("§4§l[UltimateDiscord] Invalid discord token");
            e.printStackTrace();
        }

    }

    public static void loadConfig() {
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(loadResource(instance, "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            if (!resourceFile.exists()) {
                resourceFile.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }

    public static Configuration getConfiguration(){
        return configuration;
    }
}
