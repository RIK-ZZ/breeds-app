package org.home.adapters;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.home.domain.dto.RandomImageResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.concurrent.CompletionStage;

@Path("/breeds")
@RegisterRestClient(configKey = "breeds-api")
public interface DogBreedsImagesClient {

    @GET
    @Path("/image/random")
    CompletionStage<RandomImageResponse> getImageDetails();

}
