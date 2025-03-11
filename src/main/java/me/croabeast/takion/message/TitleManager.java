package me.croabeast.takion.message;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.lib.util.Exceptions;
import org.bukkit.entity.Player;

public interface TitleManager {

    int getFadeInTicks();

    int getStayTicks();

    int getFadeOutTicks();

    void setFadeInTicks(int fadeIn);

    void setStayTicks(int stay);

    void setFadeOutTicks(int fadeOut);

    default void setTicks(int fadeIn, int stay, int fadeOut) {
        try {
            setFadeInTicks(Exceptions.validate(i -> i >= 0, fadeIn));
        } catch (Exception ignored) {}

        try {
            setStayTicks(Exceptions.validate(i -> i > 0, stay));
        } catch (Exception ignored) {}

        try {
            setFadeOutTicks(Exceptions.validate(i -> i >= 0, fadeOut));
        } catch (Exception ignored) {}
    }

    default Builder builder(String title, String subtitle) {
        return new Builder(this, title, subtitle);
    }

    Builder builder(String message);

    @Accessors(chain = true)
    @Setter
    class Builder {

        private final String title;
        private final String subtitle;

        private int fadeIn;
        private int stay;
        private int fadeOut;

        private Builder(TitleManager manager, String title, String subtitle) {
            fadeIn = manager.getFadeInTicks();
            stay = manager.getStayTicks();
            fadeOut = manager.getFadeOutTicks();

            this.title = title;
            this.subtitle = subtitle;
        }

        public Builder setTicks(int fadeIn, int stay, int fadeOut) {
            try {
                this.fadeIn = Exceptions.validate(i -> i >= 0, fadeIn);
            } catch (Exception ignored) {}

            try {
                this.stay = Exceptions.validate(i -> i > 0, stay);
            } catch (Exception ignored) {}

            try {
                this.fadeOut = Exceptions.validate(i -> i >= 0, fadeOut);
            } catch (Exception ignored) {}

            return this;
        }

        public boolean send(Player player) {
            if (player == null) return false;

            if (ReflectUtils.VERSION >= 11.0) {
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                return true;
            }

            try {
                ReflectUtils.sendPacket(player, ReflectUtils.TIMES_PACKET_INSTANCE.from(fadeIn, stay, fadeOut));
                ReflectUtils.sendPacket(player, ReflectUtils.LEGACY_PACKET_INSTANCE.apply(true, title));
                ReflectUtils.sendPacket(player, ReflectUtils.LEGACY_PACKET_INSTANCE.apply(false, subtitle));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
