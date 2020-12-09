package com.deser.seniorbackup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

import static com.deser.seniorbackup.util.SerializerUtil.deserialize;
import static com.deser.seniorbackup.util.SerializerUtil.serialize;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@NoArgsConstructor
@Data
@JsonTypeInfo(use = CLASS, property = "type")
public abstract class PlayerInventoryArgument {
    private String items;
    private String armors;
    @JsonIgnore
    private transient ItemStack[] contents;
    @JsonIgnore
    private transient ItemStack[] armorContents;

    public PlayerInventoryArgument(final String items, final String armors) {
        this.items = items;
        this.armors = armors;
        this.contents = deserialize(items);
        this.armorContents = deserialize(armors);
    }

    public PlayerInventoryArgument(final ItemStack[] contents, final ItemStack[] armorContents) {
        this.contents = contents;
        this.armorContents = armorContents;
        this.items = serialize(contents);
        this.armors = serialize(armorContents);
    }

    public ItemStack[] getContents() {
        if (contents == null) {
            contents = deserialize(items);
        } return contents;
    }

    public ItemStack[] getArmorContents() {
        if (armorContents == null) {
            armorContents = deserialize(armors);
        } return armorContents;
    }
}
