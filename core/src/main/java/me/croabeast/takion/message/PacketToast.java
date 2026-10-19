package me.croabeast.takion.message;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.advancements.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAdvancements;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.*;

// Only loaded when PacketEvents is installed. Like FlectonePulse: a client-only advancement
// is added already completed, which shows the toast, and removed right away.
@UtilityClass
class PacketToast {

    void send(Player player, String legacyTitle, String icon, String style) {
        ItemType type = ItemTypes.getByName(icon.contains(":") ? icon : "minecraft:" + icon);
        ItemStack item = ItemStack.builder().type(type == null ? ItemTypes.DIAMOND : type).build();

        AdvancementDisplay display = new AdvancementDisplay(
                AdventureSerializer.serializer().fromLegacy(legacyTitle), AdventureSerializer.serializer().fromLegacy(""),
                item, AdvancementType.valueOf(style), null, true, false, 0F, 0F);

        List<String> criteria = Collections.singletonList("trigger");
        Advancement advancement = new Advancement(null, display, criteria, Collections.singletonList(criteria), false);
        ResourceLocation id = new ResourceLocation("takion", UUID.randomUUID().toString());

        Map<String, AdvancementProgress.CriterionProgress> progress = new HashMap<>();
        progress.put("trigger", new AdvancementProgress.CriterionProgress(System.currentTimeMillis()));

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerUpdateAdvancements(
                false, Collections.singletonList(new AdvancementHolder(id, advancement)), Collections.emptySet(),
                Collections.singletonMap(id, new AdvancementProgress(progress)), true));

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerUpdateAdvancements(
                false, Collections.emptyList(), Collections.singleton(id), Collections.emptyMap(), false));
    }
}
