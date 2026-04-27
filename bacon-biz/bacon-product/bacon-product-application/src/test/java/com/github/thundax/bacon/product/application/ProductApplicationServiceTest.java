package com.github.thundax.bacon.product.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.product.application.command.CreateOrderProductSnapshotCommand;
import com.github.thundax.bacon.product.application.command.CreateProductCommand;
import com.github.thundax.bacon.product.application.command.CreateProductSkuCommand;
import com.github.thundax.bacon.product.application.command.UpdateProductCommand;
import com.github.thundax.bacon.product.application.document.ProductSearchDocument;
import com.github.thundax.bacon.product.application.executor.ProductIdempotencyExecutor;
import com.github.thundax.bacon.product.application.port.ProductSearchDocumentGateway;
import com.github.thundax.bacon.product.application.result.ProductCommandResult;
import com.github.thundax.bacon.product.application.result.ProductSnapshotResult;
import com.github.thundax.bacon.product.application.service.ProductIndexSyncApplicationService;
import com.github.thundax.bacon.product.application.service.ProductManagementApplicationService;
import com.github.thundax.bacon.product.application.service.ProductSnapshotApplicationService;
import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.model.entity.ProductIdempotencyRecord;
import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSnapshot;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.model.enums.CategoryStatus;
import com.github.thundax.bacon.product.domain.model.enums.OutboxEventType;
import com.github.thundax.bacon.product.domain.model.enums.OutboxStatus;
import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import com.github.thundax.bacon.product.domain.repository.ProductArchiveRepository;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;
import com.github.thundax.bacon.product.domain.repository.ProductIdempotencyRecordRepository;
import com.github.thundax.bacon.product.domain.repository.ProductImageRepository;
import com.github.thundax.bacon.product.domain.repository.ProductOutboxRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSnapshotRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductApplicationServiceTest {

    private FakeIdGenerator idGenerator;
    private InMemorySpuRepository spuRepository;
    private InMemorySkuRepository skuRepository;
    private InMemoryCategoryRepository categoryRepository;
    private InMemorySnapshotRepository snapshotRepository;
    private InMemoryIdempotencyRepository idempotencyRepository;
    private InMemoryOutboxRepository outboxRepository;
    private InMemorySearchDocumentGateway searchDocumentGateway;
    private ProductManagementApplicationService managementService;
    private ProductSnapshotApplicationService snapshotService;
    private ProductIndexSyncApplicationService indexSyncService;

    @BeforeEach
    void setUp() {
        idGenerator = new FakeIdGenerator();
        spuRepository = new InMemorySpuRepository();
        skuRepository = new InMemorySkuRepository();
        categoryRepository = new InMemoryCategoryRepository();
        snapshotRepository = new InMemorySnapshotRepository();
        idempotencyRepository = new InMemoryIdempotencyRepository();
        outboxRepository = new InMemoryOutboxRepository();
        searchDocumentGateway = new InMemorySearchDocumentGateway();
        ProductIdempotencyExecutor idempotencyExecutor =
                new ProductIdempotencyExecutor(idempotencyRepository, idGenerator);
        managementService = new ProductManagementApplicationService(
                spuRepository, skuRepository, categoryRepository, outboxRepository, idempotencyExecutor, idGenerator);
        snapshotService = new ProductSnapshotApplicationService(
                snapshotRepository,
                spuRepository,
                skuRepository,
                categoryRepository,
                idempotencyExecutor,
                idGenerator);
        indexSyncService = new ProductIndexSyncApplicationService(
                outboxRepository, spuRepository, skuRepository, categoryRepository, searchDocumentGateway);
        categoryRepository.save(ProductCategory.reconstruct(20L, 10L, null, "CAT-1", "digital", 0, CategoryStatus.ENABLED));
    }

    @Test
    void createProductShouldBeIdempotent() {
        CreateProductCommand command = createProductCommand("idem-1", "phone");

        ProductCommandResult first = managementService.createProduct(command);
        ProductCommandResult second = managementService.createProduct(command);

        assertEquals(first.spuId(), second.spuId());
        assertEquals(1, spuRepository.storage.size());
        assertEquals(1, outboxRepository.storage.size());
    }

    @Test
    void createProductShouldRejectSameIdempotencyKeyWithDifferentRequest() {
        managementService.createProduct(createProductCommand("idem-1", "phone"));

        assertThrows(ProductDomainException.class,
                () -> managementService.createProduct(createProductCommand("idem-1", "tablet")));
    }

    @Test
    void updateProductShouldRejectVersionConflict() {
        ProductCommandResult created = managementService.createProduct(createProductCommand("idem-1", "phone"));

        UpdateProductCommand command =
                new UpdateProductCommand(10L, created.spuId(), "phone-new", 20L, "desc", "obj-main", 99L, "idem-2");

        assertThrows(ProductDomainException.class, () -> managementService.updateProduct(command));
    }

    @Test
    void snapshotShouldBeIdempotentByOrderItem() {
        ProductCommandResult created = managementService.createProduct(createProductCommand("idem-1", "phone"));
        ProductSpu spu = spuRepository.findById(created.spuId()).orElseThrow();
        spu.onSale(skuRepository.listBySpuId(created.spuId()), 1L);
        spuRepository.update(spu);
        Long skuId = skuRepository.listBySpuId(created.spuId()).get(0).getSkuId();
        CreateOrderProductSnapshotCommand command =
                new CreateOrderProductSnapshotCommand(10L, "ORD-1", "ITEM-1", skuId, 2);

        ProductSnapshotResult first = snapshotService.createOrderProductSnapshot(command);
        ProductSnapshotResult second = snapshotService.createOrderProductSnapshot(command);

        assertEquals(first.snapshotId(), second.snapshotId());
        assertEquals(1, snapshotRepository.storage.size());
    }

    @Test
    void indexSyncShouldSkipOlderEvent() {
        ProductCommandResult created = managementService.createProduct(createProductCommand("idem-1", "phone"));
        ProductOutbox outbox = ProductOutbox.reconstruct(
                900L,
                10L,
                created.spuId(),
                "PRODUCT",
                OutboxEventType.PRODUCT_UPDATED,
                1L,
                "{}",
                OutboxStatus.PENDING,
                0,
                null,
                null,
                null);
        outboxRepository.save(outbox);
        searchDocumentGateway.currentVersion = 2L;

        indexSyncService.syncEvent(900L);

        assertNull(searchDocumentGateway.savedDocument);
        assertEquals(OutboxStatus.SUCCEEDED, outboxRepository.findById(900L).orElseThrow().getOutboxStatus());
    }

    @Test
    void indexSyncShouldWriteNewerDocument() {
        ProductCommandResult created = managementService.createProduct(createProductCommand("idem-1", "phone"));
        ProductOutbox outbox = ProductOutbox.reconstruct(
                900L,
                10L,
                created.spuId(),
                "PRODUCT",
                OutboxEventType.PRODUCT_UPDATED,
                1L,
                "{}",
                OutboxStatus.PENDING,
                0,
                null,
                null,
                null);
        outboxRepository.save(outbox);

        indexSyncService.syncEvent(900L);

        assertEquals(created.spuId(), searchDocumentGateway.savedDocument.spuId());
        assertEquals(OutboxStatus.SUCCEEDED, outboxRepository.findById(900L).orElseThrow().getOutboxStatus());
    }

    private CreateProductCommand createProductCommand(String idempotencyKey, String spuName) {
        return new CreateProductCommand(
                10L,
                "SPU-" + spuName,
                spuName,
                20L,
                "desc",
                "obj-main",
                List.of(new CreateProductSkuCommand(
                        "SKU-" + spuName, spuName + " black", "{\"color\":\"black\"}", BigDecimal.valueOf(99))),
                idempotencyKey);
    }

    private static class FakeIdGenerator implements IdGenerator {

        private long next = 100;

        @Override
        public long nextId(String bizTag) {
            return next++;
        }
    }

    private static class InMemorySpuRepository implements ProductSpuRepository {

        private final Map<Long, ProductSpu> storage = new HashMap<>();

        @Override
        public ProductSpu save(ProductSpu spu) {
            storage.put(spu.getSpuId(), spu);
            return spu;
        }

        @Override
        public ProductSpu update(ProductSpu spu) {
            storage.put(spu.getSpuId(), spu);
            return spu;
        }

        @Override
        public Optional<ProductSpu> findById(Long spuId) {
            return Optional.ofNullable(storage.get(spuId));
        }

        @Override
        public Optional<ProductSpu> findByCode(String spuCode) {
            return storage.values().stream().filter(spu -> spu.getSpuCode().equals(spuCode)).findFirst();
        }
    }

    private static class InMemorySkuRepository implements ProductSkuRepository {

        private final Map<Long, ProductSku> storage = new HashMap<>();

        @Override
        public ProductSku save(ProductSku sku) {
            storage.put(sku.getSkuId(), sku);
            return sku;
        }

        @Override
        public ProductSku update(ProductSku sku) {
            storage.put(sku.getSkuId(), sku);
            return sku;
        }

        @Override
        public Optional<ProductSku> findById(Long skuId) {
            return Optional.ofNullable(storage.get(skuId));
        }

        @Override
        public Optional<ProductSku> findByCode(String skuCode) {
            return storage.values().stream().filter(sku -> sku.getSkuCode().equals(skuCode)).findFirst();
        }

        @Override
        public List<ProductSku> listBySpuId(Long spuId) {
            return storage.values().stream().filter(sku -> sku.getSpuId().equals(spuId)).toList();
        }
    }

    private static class InMemoryCategoryRepository implements ProductCategoryRepository {

        private final Map<Long, ProductCategory> storage = new HashMap<>();

        @Override
        public ProductCategory save(ProductCategory category) {
            storage.put(category.getCategoryId(), category);
            return category;
        }

        @Override
        public ProductCategory update(ProductCategory category) {
            storage.put(category.getCategoryId(), category);
            return category;
        }

        @Override
        public Optional<ProductCategory> findById(Long categoryId) {
            return Optional.ofNullable(storage.get(categoryId));
        }

        @Override
        public Optional<ProductCategory> findByCode(String categoryCode) {
            return storage.values().stream()
                    .filter(category -> category.getCategoryCode().equals(categoryCode))
                    .findFirst();
        }

        @Override
        public List<ProductCategory> listByParentId(Long parentId) {
            return storage.values().stream()
                    .filter(category -> java.util.Objects.equals(category.getParentId(), parentId))
                    .toList();
        }
    }

    private static class InMemorySnapshotRepository implements ProductSnapshotRepository {

        private final Map<String, ProductSnapshot> storage = new HashMap<>();

        @Override
        public ProductSnapshot save(ProductSnapshot snapshot) {
            storage.put(snapshot.orderNo() + ":" + snapshot.orderItemNo(), snapshot);
            return snapshot;
        }

        @Override
        public Optional<ProductSnapshot> findByOrderItem(String orderNo, String orderItemNo) {
            return Optional.ofNullable(storage.get(orderNo + ":" + orderItemNo));
        }
    }

    private static class InMemoryIdempotencyRepository implements ProductIdempotencyRecordRepository {

        private final Map<String, ProductIdempotencyRecord> storage = new HashMap<>();

        @Override
        public ProductIdempotencyRecord insert(ProductIdempotencyRecord record) {
            storage.put(record.getOperationType() + ":" + record.getIdempotencyKey(), record);
            return record;
        }

        @Override
        public ProductIdempotencyRecord update(ProductIdempotencyRecord record) {
            storage.put(record.getOperationType() + ":" + record.getIdempotencyKey(), record);
            return record;
        }

        @Override
        public Optional<ProductIdempotencyRecord> findByOperationTypeAndKey(String operationType, String idempotencyKey) {
            return Optional.ofNullable(storage.get(operationType + ":" + idempotencyKey));
        }
    }

    private static class InMemoryOutboxRepository implements ProductOutboxRepository {

        private final Map<Long, ProductOutbox> storage = new HashMap<>();

        @Override
        public ProductOutbox save(ProductOutbox outbox) {
            storage.put(outbox.getEventId(), outbox);
            return outbox;
        }

        @Override
        public ProductOutbox update(ProductOutbox outbox) {
            storage.put(outbox.getEventId(), outbox);
            return outbox;
        }

        @Override
        public Optional<ProductOutbox> findById(Long eventId) {
            return Optional.ofNullable(storage.get(eventId));
        }
    }

    private static class InMemorySearchDocumentGateway implements ProductSearchDocumentGateway {

        private Long currentVersion;
        private ProductSearchDocument savedDocument;

        @Override
        public Optional<Long> findCurrentVersion(Long tenantId, Long spuId) {
            return Optional.ofNullable(currentVersion);
        }

        @Override
        public void saveIfNewer(ProductSearchDocument document) {
            savedDocument = document;
            currentVersion = document.productVersion();
        }
    }
}
