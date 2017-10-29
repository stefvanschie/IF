package com.gmail.stefvanschiedev.inventoryframework.test;

import com.gmail.stefvanschiedev.inventoryframework.GUI;
import com.gmail.stefvanschiedev.inventoryframework.GUIItem;
import com.gmail.stefvanschiedev.inventoryframework.GUILocation;
import com.gmail.stefvanschiedev.inventoryframework.pane.AnimatedPane;
import com.gmail.stefvanschiedev.inventoryframework.pane.OutlinePane;
import com.gmail.stefvanschiedev.inventoryframework.pane.StaticPane;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class GUITest extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;

        if (command.getName().equals("inv")) {
            GUI gui = new GUI(this, 6, ChatColor.LIGHT_PURPLE + "Colors!");

            /*AnimatedPane animatedPane = new AnimatedPane(new GUILocation(0, 0), 9, 6, 4);

            //frame one
            StaticPane frameOne = new StaticPane(animatedPane.getLocation(), animatedPane.getLength(),
                    animatedPane.getWidth());
            frameOne.fill(new GUIItem(new ItemStack(Material.RED_SHULKER_BOX), event ->
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Red")));
            animatedPane.setFrame(0, frameOne);

            //frame two
            StaticPane frameTwo = new StaticPane(animatedPane.getLocation(), animatedPane.getLength(),
                    animatedPane.getWidth());
            frameTwo.fill(new GUIItem(new ItemStack(Material.BLUE_SHULKER_BOX), event ->
                    event.getWhoClicked().sendMessage(ChatColor.BLUE + "Blue")));
            animatedPane.setFrame(1, frameTwo);

            //frame three
            StaticPane frameThree = new StaticPane(animatedPane.getLocation(), animatedPane.getLength(),
                    animatedPane.getWidth());
            frameThree.fill(new GUIItem(new ItemStack(Material.GREEN_SHULKER_BOX), event ->
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Green")));
            animatedPane.setFrame(2, frameThree);

            //frame four
            StaticPane frameFour = new StaticPane(animatedPane.getLocation(), animatedPane.getLength(),
                    animatedPane.getWidth());
            frameFour.fill(new GUIItem(new ItemStack(Material.YELLOW_SHULKER_BOX), event ->
                    event.getWhoClicked().sendMessage(ChatColor.YELLOW + "Yellow")));
            animatedPane.setFrame(3, frameFour);

            gui.addPane(animatedPane);
            gui.show((HumanEntity) sender);

            animatedPane.start(this, gui, 20L);*/

            OutlinePane pane = new OutlinePane(new GUILocation(0, 0), 9, 6);

            for (int i = 0; i < 13; i++) {
                final int j = i;

                pane.addItem(new GUIItem(new ItemStack(Material.BARRIER), event -> event.getWhoClicked().sendMessage(ChatColor.RED + "Clicked " + j)));
            }

            gui.addPane(pane);
            gui.show((HumanEntity) sender);
        }

        return true;
    }
}