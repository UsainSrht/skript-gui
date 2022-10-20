package io.github.apickledwalrus.skriptgui.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;

@Name("Virtual Inventory")
@Description("An expression to create inventories that can be used with GUIs.")
@Examples("create a gui with virtual chest inventory with 3 rows named \"My GUI\"")
@Since("1.0.0")
public class ExprVirtualInventory extends SimpleExpression<Inventory>{

	static {
		Skript.registerExpression(ExprVirtualInventory.class, Inventory.class, ExpressionType.SIMPLE,
				"virtual (1¦(crafting [table]|workbench)|2¦chest|3¦anvil|4¦hopper|5¦dropper|6¦dispenser|%-inventorytype%) [with size %-number%] [(named|with (name|title)) %-string%]",
				"virtual (1¦(crafting [table]|workbench)|2¦chest|3¦anvil|4¦hopper|5¦dropper|6¦dispenser|%-inventorytype%) [with %-number% row[s]] [(named|with (name|title)) %-string%]",
				"virtual (1¦(crafting [table]|workbench)|2¦chest|3¦anvil|4¦hopper|5¦dropper|6¦dispenser|%-inventorytype%) [(named|with (name|title)) %-string%] with size %-number%",
				"virtual (1¦(crafting [table]|workbench)|2¦chest|3¦anvil|4¦hopper|5¦dropper|6¦dispenser|%-inventorytype%) [(named|with (name|title)) %-string%] with %-number% row[s]",
				"virtual %-inventorytype% [named component %-adventurecomponent%] [with size %-number%]",
				"virtual %-inventorytype% [named component %-adventurecomponent%] [with %-number% rows]",
				"virtual %-inventorytype% [with size %-number%] [named component %-adventurecomponent%]",
				"virtual %-inventorytype% [with %-number% rows] [named component %-adventurecomponent%]"
		);
	}

	@Nullable
	private Expression<InventoryType> inventoryType;
	@Nullable
	private Expression<Number> rows;
	@Nullable
	private Expression<String> name;
	@Nullable
	private Expression<Component> componentName;

	// The name of this inventory.
	@Nullable
	private Component invName;

	public LegacyComponentSerializer lcs = LegacyComponentSerializer.builder().build();

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
		inventoryType = (Expression<InventoryType>) exprs[0];
		int namePosition;
		int rowsPosition;
		switch (matchedPattern) {
			case 3,4,5,6 -> {
				namePosition = 1;
				rowsPosition = 2;
			}
			default -> {
				namePosition = 2;
				rowsPosition = 1;
			}
		}
		if (matchedPattern > 3) componentName = (Expression<Component>) exprs[namePosition];
		else name = (Expression<String>) exprs[namePosition];
		rows = (Expression<Number>) exprs[rowsPosition];
		return true;
	}

	@Override
	protected Inventory[] get(Event e) {
		InventoryType type = inventoryType != null ? inventoryType.getSingle(e) : InventoryType.CHEST;
		if (type == null) {
			return new Inventory[0];
		} else if (type == InventoryType.CRAFTING) { // Make it a valid inventory. It's not the same, but it's likely what the user wants.
			type = InventoryType.WORKBENCH;
		}

		String name = this.name != null ? this.name.getSingle(e) : null;
		if (name == null) {
			if (componentName == null) invName = type.defaultTitle();
			else invName = componentName.getSingle(e);
		}
		else {
			Component parsedName;
			try {
				parsedName = MiniMessage.miniMessage().deserialize(name);
			}
			catch (ParsingException ex) {
				parsedName = lcs.deserialize(name);
			}
			invName = parsedName;
		}

		Inventory inventory;
		if (type == InventoryType.CHEST) {
			int size = -1;
			if (rows != null) {
				Number rows = this.rows.getSingle(e);
				if (rows != null) {
					size = rows.intValue();
					if (size <= 6) {
						size *= 9;
					}
				}
			}
			if (size < 9 || size > 54 || size % 9 != 0) { // Invalid inventory size
				size = type.getDefaultSize();
			}
			inventory = Bukkit.getServer().createInventory(null, size, invName);
		} else {
			inventory = Bukkit.getServer().createInventory(null, type, invName);
		}

		return new Inventory[]{inventory};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "virtual " + (inventoryType != null ? inventoryType.toString(e, debug) : InventoryType.CHEST.name().toLowerCase())
			+ (name != null ? " with name" + name.toString(e, debug) : "")
			+ (rows != null ? " with " + rows.toString(e, debug) + " rows" : "");
	}

	/**
	 * @return The name of this inventory. If {@link #invName} is null
	 * when this method is called, an empty string will be returned.
	 */
	public Component getName() {
		return invName != null ? invName : Component.empty();
	}

}
