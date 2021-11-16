package com.jonahshader.systems.ecs;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.jonahshader.systems.ecs.systems.IntegrateVelocity;

public class MainWorld {
    private World world;

    public MainWorld() {
        WorldConfiguration config = new WorldConfigurationBuilder()
                .with(
                        new IntegrateVelocity()
                ).build();

        world = new World(config);

        // populate world
    }

    /**
     * when running in simulate mode, set delta time to 0
     * and disable rendering systems
     *
     * else, enable all systems and set delta time to
     * 1/60 or something
     *
     *
     * simulation stuff:
     * collision algorithm will sort entities by x position
     * (might drop ecs at this point, don't think i can process
     * entities in ordered way)
     * performance might benefit by
     *
     */
}
