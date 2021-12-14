package org.home.adapters;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.home.utils.BreedURIUtils;

import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ApplicationScoped
public class AWSS3Adapter {

    @Inject
    S3AsyncClient s3;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    public CompletableFuture<String> uploadImage(String url, String objectKey){
        
        return BreedURIUtils.downloadImage(url)
                        .thenApply(bytes -> 
                                s3.putObject(PutObjectRequest.builder()
                                         .bucket(bucketName)
                                         .contentLength(Long.valueOf(bytes.length))
                                         .key(objectKey)
                                         .build(), AsyncRequestBody.fromBytes(bytes))
                        )
                        .thenComposeAsync(result -> getPreSignedURL(objectKey))
                        .exceptionally(ex -> {
                                throw new RuntimeException("Unable to upload image", ex);
        
                        }).toCompletableFuture();
    }

    private CompletionStage<String> getPreSignedURL(String objectKey) {
        
        CompletionStage<String> stage = CompletableFuture.supplyAsync(() -> {
        
            S3Presigner presigner = S3Presigner.create();
        
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
                                                                           .key(objectKey)
                                                                           .build();

            GetObjectPresignRequest getObjectPresignRequest =  GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getObjectRequest)
                             .build();

            // Generate the presigned request
            PresignedGetObjectRequest presignedGetObjectRequest =
                            presigner.presignGetObject(getObjectPresignRequest);

            // Log the presigned URL
            System.out.println("Presigned URL: " + presignedGetObjectRequest.url());

            return presignedGetObjectRequest.url().toString();
        });

        return stage;
    }
}
