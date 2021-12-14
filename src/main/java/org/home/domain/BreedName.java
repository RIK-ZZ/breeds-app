package org.home.domain;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@ProjectionFor(DogBreedInfo.class)
public class BreedName {
        public String name;
}
