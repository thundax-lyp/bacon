package com.github.thundax.bacon.storage.api.facade;

import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;

public interface StoredObjectReadFacade {

    StoredObjectFacadeResponse getObjectByNo(StoredObjectGetFacadeRequest request);
}
