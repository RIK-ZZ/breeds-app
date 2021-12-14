package org.home.controller;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.home.adapters.AWSS3Adapter;
import org.home.adapters.DogBreedsImagesClient;
import org.home.domain.DogBreedInfo;
import org.home.utils.BreedURIUtils;
import org.jboss.resteasy.reactive.ResponseStatus;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.Objects;

@Path("dogs/breeds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DogsBreedsResource {

    @Inject
    @RestClient
    DogBreedsImagesClient imagesClient;

    @Inject
    AWSS3Adapter s3Adapter;

    // Generate new dog breed record
    @POST
    public Uni<Response> createNewBreedRecord() {
        return Uni.createFrom().completionStage(imagesClient.getImageDetails())
            .chain(response -> {
                
                DogBreedInfo breed = new DogBreedInfo();
                breed.setName(BreedURIUtils.getBreedName(response.getMessage()));
                breed.setDateCreated();
                try {
                    CompletableFuture<String> url = s3Adapter.uploadImage(response.getMessage(),
                                                BreedURIUtils.getImageName(response.getMessage()));
                
                    breed.setLocation(url.get());

                    return Uni.createFrom().item(breed);
                
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to get Dog Breed Info");
                }
            })
            .call(breed -> {
                return breed.persist();
            })
            .map(breed -> {
                return Response.ok(breed).build();
            })
            .onFailure().recoverWithItem(error -> {
                error.printStackTrace();
                return Response.serverError().entity(error.getMessage()).build();
            });
    }
    
    // Get breed info by ID
    @GET
    @Path("/{id}")
    public Uni<Response> getById(String id) {
        ObjectId objectId = new ObjectId(id);
        return DogBreedInfo.findById(objectId)
                    .map(breed -> {
                        if (Objects.isNull(breed)) {
                            return Response.status(Status.NOT_FOUND).entity("No breeds found for this ID").build();
                        } else {
                            return Response.ok(breed).build();
                        }
                    })
                    .onFailure().recoverWithItem(error -> {
                        error.printStackTrace();
                        return Response.serverError().entity(error).build();
                    });
    }

    // Get all breeds names
    @GET
    @Path("/names")
    @Blocking
    public Uni<Set<String>> findAllBreedNames() {
        return DogBreedInfo.findAllUniqueNames();
    }

    // Get breeds info by breed name
    @GET
    @Path("/names/{name}")
    public Uni<List<DogBreedInfo>> findByBreedName(String name) {
        return DogBreedInfo.findByName(name);
    }

    // Delete breed entry by document id
    @ResponseStatus(204)
    @DELETE
    @Path("/{id}")
    public Uni<Boolean> removeById(String id) {
        ObjectId objectId = new ObjectId(id);
        return DogBreedInfo.deleteById(objectId);
    }
    
    @GET
    @Path("/status")
    public Uni<String> getServiceStatus() {
        return Uni.createFrom().item("RUNNING");
    }
}
