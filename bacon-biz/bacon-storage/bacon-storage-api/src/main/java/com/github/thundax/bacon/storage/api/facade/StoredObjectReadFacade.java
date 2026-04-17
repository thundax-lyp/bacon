package com.github.thundax.bacon.storage.api.facade;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;

public interface StoredObjectReadFacade {

    StoredObjectDTO getObjectByNo(String storedObjectNo);
}
