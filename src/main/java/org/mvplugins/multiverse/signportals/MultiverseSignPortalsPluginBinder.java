package org.mvplugins.multiverse.signportals;

import org.mvplugins.multiverse.core.inject.binder.JavaPluginBinder;
import org.mvplugins.multiverse.core.module.MultiverseModuleBinder;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

public class MultiverseSignPortalsPluginBinder extends MultiverseModuleBinder<MultiverseSignPortals> {

    protected MultiverseSignPortalsPluginBinder(@NotNull MultiverseSignPortals plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<MultiverseSignPortals> bindPluginClass(
            ScopedBindingBuilder<MultiverseSignPortals> bindingBuilder) {
        return super.bindPluginClass(bindingBuilder).to(MultiverseSignPortals.class);
    }
}
