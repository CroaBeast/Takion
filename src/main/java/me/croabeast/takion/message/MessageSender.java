package me.croabeast.takion.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.takion.TakionLib;
import me.croabeast.takion.channel.Channel;
import me.croabeast.takion.placeholder.Placeholder;
import me.croabeast.lib.PlayerFormatter;
import me.croabeast.lib.applier.StringApplier;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.ReplaceUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.UnaryOperator;

@Accessors(chain = true)
@Setter
public class MessageSender {

    private final TakionLib lib;

    private final Set<Player> targets = new HashSet<>();
    private Player parser;

    private final List<Placeholder<?>> placeholders;

    private final Set<PlayerFormatter> functions;
    private final Set<Flag> flags = new HashSet<>();

    @Getter
    private boolean logger = true, sensitive = true;
    @Getter
    private String errorPrefix = "&c[NOT_SENT]&7 ";

    public MessageSender(TakionLib lib, Collection<? extends Player> targets, Player parser) {
        this.lib = lib;

        placeholders = new ArrayList<>();
        functions = new HashSet<>();

        setTargets(targets);
        this.parser = parser;
    }

    public MessageSender(TakionLib lib, Collection<? extends Player> targets) {
        this(lib, targets, null);
    }

    public MessageSender(TakionLib lib, Player player) {
        this(lib, player != null ? ArrayUtils.toList(player) : null, player);
    }

    public MessageSender(TakionLib lib) {
        this(lib, null, null);
    }

    public MessageSender(MessageSender sender) {
        this(sender.lib, sender.targets, sender.parser);

        this.errorPrefix = sender.getErrorPrefix();
        this.logger = sender.isLogger();
        this.sensitive = sender.isSensitive();
    }

    public MessageSender setTargets(Collection<? extends Player> targets) {
        if (targets == null || targets.isEmpty())
            return this;

        this.targets.clear();
        this.targets.addAll(targets);

        this.targets.removeIf(Objects::isNull);
        return this;
    }

    public MessageSender setTargets(Player... targets) {
        return setTargets(ArrayUtils.toList(targets));
    }

    public MessageSender addFunctions(PlayerFormatter... functions) {
        this.functions.addAll(ArrayUtils.toList(functions));
        return this;
    }

    @SafeVarargs
    public final MessageSender addFunctions(UnaryOperator<String>... operators) {
        try {
            ArrayUtils.toList(operators).forEach(u -> {
                if (u != null)
                    functions.add((p, s) -> u.apply(s));
            });
        } catch (Exception ignored) {}
        return this;
    }

    public MessageSender setFlags(Flag... flags) {
        this.flags.clear();
        this.flags.addAll(ArrayUtils.toList(flags));

        return this;
    }

    public <T> MessageSender addPlaceholder(Placeholder<T> placeholder) {
        placeholders.add(placeholder.setSensitive(isSensitive()));
        return this;
    }

    public <T> MessageSender addPlaceholder(String key, T value) {
        return addPlaceholder(new Placeholder<>(key, value).setSensitive(isSensitive()));
    }

    public MessageSender addPlaceholders(Collection<? extends Placeholder<?>> placeholders) {
        Objects.requireNonNull(placeholders).forEach(this::addPlaceholder);
        return this;
    }

    public MessageSender addPlaceholders(Placeholder<?>... placeholders) {
        return addPlaceholders(ArrayUtils.toList(placeholders));
    }

    @SafeVarargs
    public final <T> MessageSender addPlaceholders(String[] keys, T... values) {
        if (ReplaceUtils.isApplicable(keys, values)) {
            for (int i = 0; i < keys.length; i++)
                try {
                    addPlaceholder(keys[i], values[i]);
                } catch (Exception ignored) {}
            return this;
        }

        throw new NullPointerException(
                "Keys/Values are not applicable for replacements.");
    }

    public MessageSender copy() {
        return new MessageSender(this);
    }

    public boolean send(List<String> strings) {
        if (strings == null) return false;

        final List<Message> messages = new ArrayList<>();
        for (String s : strings)
            if (s != null) messages.add(new Message(s));

        if (messages.isEmpty()) return false;

        if (targets.isEmpty()) {
            boolean atLeastOneIsSent = false;

            for (Message message : messages) {
                if (message.flag == Flag.WEBHOOK && message.isAllowed())
                    atLeastOneIsSent = message.send(null);

                message.log(atLeastOneIsSent);
            }
            return atLeastOneIsSent;
        }

        boolean atLeastOneIsSent = false;

        for (Message message : messages) {
            if (message.isBlank() && message.isAllowed()) {
                message.log(true);

                targets.forEach(message::send);
                continue;
            }

            if (lib.getBlankSpacesAction().act(targets, message.message)
                    && !message.isAllowed()) continue;

            boolean wasSentBefore = false;
            for (Player target : targets) {
                boolean isSent = message.send(target);

                if (isSent && !wasSentBefore) wasSentBefore = true;
                message.log(isSent);
            }

            atLeastOneIsSent = wasSentBefore;
        }
        return atLeastOneIsSent;
    }

    public boolean send(String... strings) {
        return send(ArrayUtils.toList(strings));
    }

    private class Message {

        private final Channel channel;
        private final Flag flag;
        private final String message;

        private Message(String message) {
            this.message = message;

            channel = lib.getChannelManager().identify(message);
            flag = channel.getFlag();
        }

        boolean isBlank() {
            return StringUtils.isBlank(message);
        }

        String formatMessage() {
            StringApplier applier = StringApplier.simplified(message)
                    .apply(s -> lib.replacePrefixKey(s, false));

            functions.forEach(f -> applier.apply(s -> f.apply(parser, s)));
            placeholders.forEach(p ->
                    applier.apply(s -> p.replace(parser, s)));

            return applier.toString();
        }

        boolean isAllowed() {
            return flags.isEmpty() || flags.contains(flag);
        }

        void log(boolean sent) {
            if (!isLogger()) return;

            String s = lib.getPlaceholderManager().replace(parser, formatMessage());
            String error = getErrorPrefix();

            lib.getLogger().log((sent || StringUtils.isBlank(error) ? "" : error) + s);
        }

        boolean send(Player target) {
            return isAllowed() && channel.send(target, parser, formatMessage());
        }
    }

    public enum Flag {
        CHAT,
        ACTION_BAR,
        TITLE,
        BOSSBAR,
        JSON,
        WEBHOOK
    }
}
