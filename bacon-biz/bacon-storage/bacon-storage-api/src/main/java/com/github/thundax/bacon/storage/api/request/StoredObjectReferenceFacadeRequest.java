package com.github.thundax.bacon.storage.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectReferenceFacadeRequest {

    private String storedObjectNo;
    private String ownerType;
    private String ownerId;
}
