package com.jonahshader.systems.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalEntitySystem;
import com.artemis.systems.IntervalSystem;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.jonahshader.systems.ecs.Position;
import com.jonahshader.systems.ecs.Velocity;

@All({Position.class, Velocity.class})
public class IntegrateVelocity extends IteratingSystem {
    private ComponentMapper<Position> positionMapper;
    private ComponentMapper<Velocity> velocityMapper;

    private Vector2 temp = new Vector2();

    @Override
    protected void process(int entityId) {
        temp.set(velocityMapper.get(entityId).getVel());
        temp.scl(dt);
        positionMapper.get(entityId).getPos().add(velocityMapper.get(entityId).getVel().)
    }
}
