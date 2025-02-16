package me.croabeast.takion.channel;

import org.jetbrains.annotations.NotNull;

public interface ChannelManager {

    @NotNull
    String getStartDelimiter();

    void setStartDelimiter(@NotNull String delimiter);

    @NotNull
    String getEndDelimiter();

    void setEndDelimiter(@NotNull String delimiter);

    void setDefaults();

    @NotNull
    Channel identify(@NotNull String string);
}
