package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only the refusals can be tested here: a real Medieval Factions needs a running server. Its
 * classes are compile-only, so they are missing from the test classpath, just as they are from a
 * server running a Medieval Factions build with other packages.
 */
class MedievalFactionsWarStateTest {

    private static Plugin pluginThatIsEnabled(boolean enabled) {
        return (Plugin) Proxy.newProxyInstance(
                Plugin.class.getClassLoader(), new Class<?>[]{Plugin.class}, (proxy, method, args) -> {
                    if (method.getName().equals("isEnabled")) {
                        return enabled;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    void noPluginGivesNoWarState() {
        assertTrue(MedievalFactionsWarState.of(null).isEmpty());
    }

    @Test
    void aDisabledPluginGivesNoWarState() {
        assertTrue(MedievalFactionsWarState.of(pluginThatIsEnabled(false)).isEmpty());
    }

    @Test
    void aPluginWhoseClassesDoNotMatchGivesNoWarState() {
        assertTrue(MedievalFactionsWarState.of(pluginThatIsEnabled(true)).isEmpty());
    }
}
