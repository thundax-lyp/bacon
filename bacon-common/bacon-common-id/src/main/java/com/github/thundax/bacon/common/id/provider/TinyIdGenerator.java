package com.github.thundax.bacon.common.id.provider;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;

public class TinyIdGenerator implements IdGenerator {

    @Override
    public long nextId(String bizTag) {
        try {
            Long id = TinyId.nextId(bizTag);
            if (id == null || id <= 0L) {
                throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_RESPONSE_INVALID,
                        "tinyid return invalid id, bizTag=" + bizTag);
            }
            return id;
        } catch (IdGeneratorException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_UNAVAILABLE,
                    "tinyid generate failed, bizTag=" + bizTag, ex);
        }
    }
}
