package me.croabeast.takion;

import me.croabeast.takion.tag.Tag;
import me.croabeast.takion.tag.TagManager;
import me.croabeast.vnc.VNC;

final class TagManagerImpl extends TokenRegistryImpl<Tag> implements TagManager {

    {
        load(new CharacterTag());

        if (VNC.SERVER != null && VNC.SERVER.isAtLeast("1.21.9")) {
            load(new PlayerHeadTag());
            load(new SpriteTag());
        }
    }
}
