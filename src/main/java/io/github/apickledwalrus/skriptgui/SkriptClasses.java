package io.github.apickledwalrus.skriptgui;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EnumUtils;
import io.github.apickledwalrus.skriptgui.gui.GUI;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.eclipse.jdt.annotation.Nullable;

public class SkriptClasses {

	public SkriptClasses() {

		Classes.registerClass(new ClassInfo<>(GUI.class, "guiinventory")
			.user("gui inventor(y|ies)?")
			.name("GUI")
			.description("Represents a skript-gui GUI")
			.examples("See the GUI creation section.")
			.since("1.0")
			.parser(new Parser<GUI>() {
				@Override
				public boolean canParse(ParseContext ctx) {
					return false;
				}

				@Override
				public String toString(GUI gui, int flags) {
					return gui.getInventory().getType().getDefaultTitle().toLowerCase()
							+ " gui named " + gui.getName() 
							+ " with " + gui.getInventory().getSize() / 9 + " rows"
							+ " and shape " + gui.getRawShape();
				}

				@Override
				public String toVariableNameString(GUI gui) {
					return toString(gui, 0);
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(Component.class, "adventurecomponent")
				.user("[adventure][(-| )]component[s]")
				.name("COMPONENT")
				.description("Represents an Adventure Component.")
				.examples("set {_s} to adventure-component")
				.since("1.3")
				.parser(new Parser<Component>() {
					@Override
					public boolean canParse(ParseContext ctx) {
						return false;
					}

					@Override
					public String toString(Component component, int flags) {
						return component.toString();
					}

					@Override
					public String toVariableNameString(Component component) {
						return toString(component, 0);
					}
				}));

		if (Classes.getExactClassInfo(SlotType.class) == null) {
			EnumUtils<SlotType> slotTypes = new EnumUtils<>(SlotType.class, "slot types");
			Classes.registerClass(new ClassInfo<>(SlotType.class, "slottype")
				.user("slot types?")
				.name("Slot Types")
				.description("Represents the slot type in an Inventory Click Event.")
				.examples(slotTypes.getAllNames())
				.since("1.0.0")
				.parser(new Parser<SlotType>() {
					@Override
					@Nullable
					public SlotType parse(String expr, ParseContext context) {
						return slotTypes.parse(expr);
					}

					@Override
					public boolean canParse(ParseContext ctx) {
						return true;
					}

					@Override
					public String toString(SlotType type, int flags) {
						return slotTypes.toString(type, flags);
					}

					@Override
					public String toVariableNameString(SlotType type) {
						return "slottype:" + type.name();
					}
				})
				.serializer(new EnumSerializer<>(SlotType.class)
			));
		}

	}

}
