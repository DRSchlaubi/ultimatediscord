package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.GuildController;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CommandDiscord implements CommandExecutor {

    public static HashMap<String, String> users = new HashMap<>();

    private String generateString(){
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567980";
        StringBuilder random = new StringBuilder();
        Random rnd = new Random();
        while(random.length() < 5){
            int index = (int) (rnd.nextFloat() * CHARS.length());
            random.append(CHARS.charAt(index));
        }
        return random.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command name, String lable, String[] args) {
        if(sender instanceof Player){
            FileConfiguration cfg = Main.getConfiguration();
            Player player = (Player) sender;
            if(args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("discord.reload")) {
                        player.sendMessage("§7[§Discord§7]§a Settings reloaded");
                        try {
                            cfg.save(new File("plugins/TeamspeakVerifyer", "config.yml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (args[0].equalsIgnoreCase("verify")) {
                    if (users.containsKey(player.getName())) {
                        player.sendMessage(cfg.getString("Messages.running").replace("&", "§").replace("%code%", users.get(player.getName())));
                    } else if (MySQL.userExists(player)) {
                        player.sendMessage(cfg.getString("Messages.verified").replace("&", "§"));
                    } else {
                        users.put(player.getName(), generateString());
                        player.sendMessage(cfg.getString("Messages.verify").replace("&", "§").replace("%code%", users.get(player.getName())));
                        Bukkit.getScheduler().runTaskLater(Main.instance, new Runnable() {
                            @Override
                            public void run() {
                                if (users.containsKey(player.getName())) {
                                    users.remove(player.getName());
                                }
                            }
                        }, 60 * 1000);
                    }
                } else if (args[0].equalsIgnoreCase("unlink")) {
                    if (!MySQL.userExists(player)) {
                        player.sendMessage(cfg.getString("Messages.notverified").replace("&", "§"));
                    } else {
                        GuildController guild = new GuildController(Main.jda.getGuilds().get(0));
                        Member member = guild.getGuild().getMember(Main.jda.getUserById(MySQL.getValue(player, "discordid")));
                        guild.removeRolesFromMember(member, guild.getGuild().getRoleById(cfg.getString("Roles.defaultrole"))).queue();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                guild.removeRolesFromMember(member, guild.getGuild().getRoleById(cfg.getString("Roles.group." + Main.getPermissions().getPrimaryGroup(player)))).queue();
                            }
                        }, 1000);
                        MySQL.deleteUser(player);
                        player.sendMessage(cfg.getString("Messages.unlinked").replace("&", "§"));
                    }
                } else if(args[0].equalsIgnoreCase("update")){
                    if (!MySQL.userExists(player)) {
                        player.sendMessage(cfg.getString("Messages.notverified").replace("&", "§"));
                    } else {
                        GuildController guild = new GuildController(Main.jda.getGuilds().get(0));
                        Member member = guild.getGuild().getMemberById(MySQL.getValue(player, "discordid"));
                        Role role = guild.getGuild().getRoleById(cfg.getString("Roles.group." + Main.getPermissions().getPrimaryGroup(player)));
                        guild.addRolesToMember(member, role).queue();
                        player.sendMessage(cfg.getString("Messages.updated").replace("&", "§"));
                    }
                }
            } else {
                player.sendMessage(cfg.getString("Messages.help").replace("%nl", "\n").replace("&", "§"));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("§4§l[UltimateDiscord] You must be a player to run this command");
        }

        return false;
    }
}
