package com.github.thundax.bacon.storage.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectDeleteFacadeRequest {

    private String storedObjectNo;
}
