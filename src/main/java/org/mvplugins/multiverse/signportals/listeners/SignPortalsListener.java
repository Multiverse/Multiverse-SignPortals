package org.mvplugins.multiverse.signportals.listeners;

import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.dynamiclistener.DynamicListener;

@Contract
public sealed interface SignPortalsListener extends DynamicListener permits MVSPBlockListener, MVSPPlayerListener, MVSPPluginListener, MVSPVersionListener {
}
