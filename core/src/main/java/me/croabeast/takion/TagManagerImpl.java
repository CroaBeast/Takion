package me.croabeast.takion;

import me.croabeast.takion.tag.Tag;
import me.croabeast.takion.tag.TagManager;
import me.croabeast.vnc.VNC;

final class TagManagerImpl extends TokenRegistryImpl<Tag> implements TagManager {

    private static final double OBJECT_TAG_VERSION = 21.9;

    {
        load(new CharacterTag());

        if (VNC.SERVER_VERSION >= OBJECT_TAG_VERSION) {
            load(new PlayerHeadTag());
            load(new SpriteTag());
        }
    }
}
