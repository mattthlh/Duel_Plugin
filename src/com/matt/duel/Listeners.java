package com.matt.duel;

import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;


public class Listeners implements Listener {

    private final Main main;

    public Listeners(Main plugin) {
        this.main = plugin;
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.MUSHROOM_STEW) {
                for (String s : main.duelings.keySet()) {
                    if(main.duelings.get(s).equalsIgnoreCase(e.getPlayer().getName()) || s.equalsIgnoreCase(e.getPlayer().getName())) {
                        double health = e.getPlayer().getHealth() + 4;
                        if (health > 20) {
                            health = 20;
                        }
                        e.getPlayer().setHealth(health);
                        e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }
            } else if(e.getPlayer().getInventory().getItemInMainHand().getType() == Material.COMPASS) {
                e.getPlayer().setCompassTarget(main.target.getLocation());
            }
        }
    }
    @EventHandler
    public void playerDeath(PlayerDeathEvent e) {
        Player loser = e.getEntity();
        Player winner;

        for (String s : main.duelings.keySet()) {
            if (main.duelings.get(s).equalsIgnoreCase(loser.getName())) {
                e.getDrops().clear();
                winner = main.getServer().getPlayer(s);

                loser.sendMessage(winner.getName() + " won the duel");
                winner.sendMessage(winner.getName() + " won the duel");
                main.duelings.remove(loser.getName());
                main.duelings.remove(winner.getName());


                winner.getInventory().clear();
                if (main.inventories.containsKey(winner.getName())) {
                    winner.getInventory().setContents(main.inventories.get(winner.getName()));
                    winner.getInventory().setArmorContents(main.armorinventories.get(winner.getName()));
                    main.inventories.remove(winner.getName());
                }
                winner.teleport(main.beforeDuelOp);
            } else if (s.equalsIgnoreCase(e.getEntity().getName())) {
                e.getDrops().clear();
                winner = main.getServer().getPlayer(main.duelings.get(s));

                loser.sendMessage(winner.getName() + " won the duel");
                winner.sendMessage(winner.getName() + " won the duel");
                main.duelings.remove(winner.getName());
                main.duelings.remove(loser.getName());


                winner.getInventory().clear();
                if (main.inventories.containsKey(winner.getName())) {
                    winner.getInventory().setContents(main.inventories.get(winner.getName()));
                    main.inventories.remove(winner.getName());
                }
                winner.teleport(main.beforeDuelPlayer);
            }
        }
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent e) {
        for (String s : main.duelings.keySet()) {
            if (main.duelings.get(s).equalsIgnoreCase(e.getPlayer().getName())) {
                if (main.inventories.containsKey(e.getPlayer().getName())) {
                    e.getPlayer().getInventory().setContents(main.inventories.get(e.getPlayer().getName()));
                    main.inventories.remove(e.getPlayer().getName());
                    e.getPlayer().teleport(main.beforeDuelPlayer);
                }
            } else if (s.equalsIgnoreCase(e.getPlayer().getName())) {
                if (main.inventories.containsKey(e.getPlayer().getName())) {
                    e.getPlayer().getInventory().setContents(main.inventories.get(e.getPlayer().getName()));
                    main.inventories.remove(e.getPlayer().getName());
                    e.getPlayer().teleport(main.beforeDuelOp);
                }
            }
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        if (main.frozenPlayers.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void playerCommandProcess(PlayerCommandPreprocessEvent e) {
        for (String s : main.duelings.keySet()) {
            if (main.duelings.get(s).equalsIgnoreCase(e.getPlayer().getName()) || s.equalsIgnoreCase(e.getPlayer().getName())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("You can't use any commands in a duel");
            }
        }
    }
}
