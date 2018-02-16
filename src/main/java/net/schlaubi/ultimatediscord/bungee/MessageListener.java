package net.schlaubi.ultimatediscord.bungee;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.schlaubi.ultimatediscord.util.MySQL;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MessageListener extends ListenerAdapter{

    private HashMap<String, String> users = CommandDiscord.users;
    private String getUser(String code) {
        for(String key : users.keySet()) {
            String value = users.get(key);
            if(value.equalsIgnoreCase(code)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Configuration cfg = Main.getConfiguration();
        if(event.isFromType(ChannelType.PRIVATE)){
            JDA jda = event.getJDA();
            String message = event.getMessage().getContentDisplay();
            String[] args = message.split(" ");
            if(message.startsWith("!roles")){
                StringBuilder sb = new StringBuilder();
                for(Role r : jda.getGuilds().get(0).getRoles()){
                    sb.append("[R: " + r.getName() + "(" + r.getId() + ")");
                }
                event.getPrivateChannel().sendMessage(sb.toString()).queue();
            } else if(message.startsWith("!verify")){
                if(users.containsValue(args[1])){
                    ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(getUser(args[1]));
                    GuildController guild = new GuildController(jda.getGuilds().get(0));
                    Member member = jda.getGuilds().get(0).getMember(event.getAuthor());
                    guild.addRolesToMember(member, guild.getGuild().getRoleById(cfg.getLong("Roles.defaultrole"))).queue();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            cfg.getSection("Roles.group").getKeys().forEach(i ->{
                                if(pp.hasPermission("group." + i)){
                                    guild.addRolesToMember(member, guild.getGuild().getRoleById(cfg.getLong("Roles.group." + i))).queue();
                                }
                            });
                        }
                    }, 1000);
                    MySQL.createUser(pp.getName(), event.getAuthor().getId());
                    event.getPrivateChannel().sendMessage(cfg.getString("Messages.success").replace("%discord%", event.getAuthor().getName())).complete();
                    users.remove(pp.getName());
                } else {
                    event.getPrivateChannel().sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
            }
        }
    }
}
