package org.home.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Uni;

@MongoEntity(collection = "dog_breeds")
public class DogBreedInfo extends ReactivePanacheMongoEntity {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private String name;
    private String location;
    private String dateCreated;
    
    public String getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setDateCreated() {
        dateCreated = new SimpleDateFormat(TIMESTAMP_FORMAT)
                            .format(Calendar.getInstance().getTime());
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public static Uni<List<DogBreedInfo>> findByName(String name) {
        return find("name", name).list();
    }

    public static Uni<Set<String>> findAllUniqueNames() {        
        return findAll()
                    .project(BreedName.class)
                    .list()
                    .map(names -> {
                        return names.stream()
                                .map(name -> name.name)
                                .distinct()
                                .collect(Collectors.toSet());
                    });
    }
}
