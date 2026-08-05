package me.croabeast.takion;

import me.croabeast.takion.action.Action;
import me.croabeast.takion.action.ActionManager;

final class ActionManagerImpl extends TokenRegistryImpl<Action> implements ActionManager {

    {
        load(new BlankSpacesAction());
    }
}
