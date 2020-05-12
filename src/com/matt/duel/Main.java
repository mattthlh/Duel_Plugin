package com.matt.duel;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin {

    private HashMap<String, String> duelers = new HashMap<>();
    HashMap<String, String> duelings = new HashMap<>();

    HashMap<String, ItemStack[]> inventories = new HashMap<>();
    HashMap<String, ItemStack[]> armorinventories = new HashMap<>();

    ArrayList<String> frozenPlayers = new ArrayList<>();

    Location beforeDuelOp;
    Location beforeDuelPlayer;

    Player target;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getConfig();
        saveConfig();
        Bukkit.getServer().getLogger().info("Plugin enabled");
    }

    public void onDisable() {
        Bukkit.getServer().getLogger().info("Plugin disabled");

        for (String s : duelings.keySet()) {
            Player one = getServer().getPlayer(duelings.get(s));
            Player two = getServer().getPlayer(s);
            if (inventories.containsKey(one.getName())) {
                one.getInventory().setContents(inventories.get(one.getName()));
                inventories.remove(one.getName());
                one.teleport(beforeDuelPlayer);
            }
            if (inventories.containsKey(two.getName())) {
                two.getInventory().setContents(inventories.get(two.getName()));
                inventories.remove(two.getName());
                two.teleport(beforeDuelOp);
            }
        }
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = ((Player) sender).getPlayer();


            if (!sender.hasPermission(command.getName())) {
                sender.sendMessage(ChatColor.RED + "You are not permitted to use this command!");
                return false;
            }

            switch (command.getName()) {
                case "duel":
                    if(getConfig().getString( "setduel1.world") != null ||
                            getConfig().getString( "setduel2.world") != null) {
                        if (args.length == 1) {
                            Player opponent = getServer().getPlayer(args[0]);
                            if (opponent != null) {
                                duelers.put(player.getName(), opponent.getName());
                                opponent.sendMessage(ChatColor.BLUE + player.getName() + " wants to duel you");
                                opponent.sendMessage(ChatColor.GREEN + "/accept" + ChatColor.BLACK + " or " + ChatColor.RED + " /deny");
                                player.sendMessage("Duel sent");
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED + "Invalid Player");
                                return false;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Please enter a player name");
                            return false;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You did not set both duel spawns");
                        return false;
                    }

                case "accept":
                    if(duelers.containsValue(player.getName())) {
                        player.sendMessage(ChatColor.GREEN + "You accepted the duel");
                        for(String s : duelers.keySet()) {
                            if(duelers.get(s).equalsIgnoreCase(player.getName())) {
                                Player opponent = getServer().getPlayer(s);
                                opponent.sendMessage(ChatColor.GREEN + player.getName() + " accepted your duel");
                                duelers.remove(s , player.getName());
                                duelings.put(s, player.getName());

                                beforeDuelOp = opponent.getLocation();
                                beforeDuelPlayer = player.getLocation();

                                player.setHealth(20);
                                opponent.setHealth(20);

                                player.setGameMode(GameMode.SURVIVAL);
                                opponent.setGameMode(GameMode.SURVIVAL);
                                World wd1 = Bukkit.getServer().getWorld(getConfig().getString("setduel1.world"));
                                double xd1 = getConfig().getDouble("setduel1.x");
                                double yd1 = getConfig().getDouble("setduel1.y");
                                double zd1 = getConfig().getDouble("setduel1.z");
                                float pitchd1 = (float) getConfig().getDouble("setduel1.pitch");
                                float yawd1 = (float) getConfig().getDouble("setduel1.yaw");

                                Location dueler1tp = new Location(wd1, xd1, yd1, zd1);
                                dueler1tp.setYaw(yawd1);
                                dueler1tp.setPitch(pitchd1);
                                opponent.teleport(dueler1tp);

                                World wd2 = Bukkit.getServer().getWorld(getConfig().getString("setduel2.world"));
                                double xd2 = getConfig().getDouble("setduel2.x");
                                double yd2 = getConfig().getDouble("setduel2.y");
                                double zd2 = getConfig().getDouble("setduel2.z");
                                float pitchd2 = (float)getConfig().getDouble("setduel2.pitch");
                                float yawd2 = (float) getConfig().getDouble("setduel2.yaw");


                                Location dueler2tp = new Location(wd2, xd2, yd2, zd2);
                                dueler2tp.setPitch(pitchd2);
                                dueler2tp.setYaw(yawd2);
                                player.teleport(dueler2tp);



                                inventories.put(player.getName(), player.getInventory().getContents());
                                inventories.put(opponent.getName(), opponent.getInventory().getContents());

                                armorinventories.put(player.getName(), player.getInventory().getArmorContents());
                                armorinventories.put(opponent.getName(), opponent.getInventory().getArmorContents());

                                for (String item : getConfig().getConfigurationSection("items").getKeys(false)) {
                                    int quantity = getConfig().getInt(item + ".quantity");
                                    ItemStack target = new ItemStack(Material.matchMaterial(item), quantity);
                                    player.getInventory().addItem(target);
                                    opponent.getInventory().addItem(target);
                                }
                                String boots = getConfig().getString("boots");
                                String leggings = getConfig().getString("leggings");
                                String chestplate = getConfig().getString("chestplate");
                                String helmet = getConfig().getString("helmet");

                                ItemStack[] duelArmour = {new ItemStack(Material.matchMaterial(boots)),
                                        new ItemStack(Material.matchMaterial(leggings)),
                                        new ItemStack(Material.matchMaterial(chestplate)),
                                        new ItemStack(Material.matchMaterial(helmet))};


                                player.getInventory().setArmorContents(duelArmour);
                                opponent.getInventory().setArmorContents(duelArmour);

                                startTimer(opponent);
                                startTimer(player);
                            }
                        }
                        return true;
                    }
                    return false;

                case "deny":
                    if(duelers.containsValue(player.getName())) {
                        player.sendMessage(ChatColor.YELLOW + "You denied the duel");
                        for(String s : duelers.keySet()) {
                            if(duelers.get(s).equalsIgnoreCase(player.getName())) {
                                getServer().getPlayer(s).sendMessage(ChatColor.RED + player.getName() + " denied your duel");
                                duelers.remove(s , player.getName());
                            }
                        }
                        return true;
                    }
                    return false;

                case "setduel1":
                    getConfig().set("setduel1.world", player.getLocation().getWorld().getName());
                    getConfig().set("setduel1.x", player.getLocation().getX());
                    getConfig().set("setduel1.y", player.getLocation().getY());
                    getConfig().set("setduel1.z", player.getLocation().getZ());
                    getConfig().set("setduel1.pitch", player.getLocation().getPitch());
                    getConfig().set("setduel1.yaw", player.getLocation().getYaw());
                    saveConfig();
                    player.sendMessage(ChatColor.GREEN + "setduel1 location has been set!");
                    return true;

                case "setduel2":
                    getConfig().set("setduel2.world", player.getLocation().getWorld().getName());
                    getConfig().set("setduel2.x", player.getLocation().getX());
                    getConfig().set("setduel2.y", player.getLocation().getY());
                    getConfig().set("setduel2.z", player.getLocation().getZ());
                    getConfig().set("setduel2.pitch", player.getLocation().getPitch());
                    getConfig().set("setduel2.yaw", player.getLocation().getYaw());
                    saveConfig();
                    player.sendMessage(ChatColor.GREEN + "setduel2 location has been set!");
                    return true;

                case "track":
                    target = getServer().getPlayer(args[0]);
                    player.sendMessage("Your target has been tracked. Chase him down!");
                    return true;

                default:
                    return false;
            }
        }
        return true;
    }

    public void startTimer(Player player) {
        String[] messages = {"§a3", "§e2", "§c1", "§aGO!!!"};
        frozenPlayers.add(player.getName());
        new BukkitRunnable() {
            int i = 0;
            public void run()
            {
                player.sendTitle(messages[i], "", 10, 40, 10);
                if(i < messages.length - 1) {
                    i++;
                } else {
                    cancel();
                    frozenPlayers.remove(player.getName());
                }
            }
        }.runTaskTimer(this, 0, 20);
    }
}
