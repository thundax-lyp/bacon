package com.github.thundax.bacon.inventory.domain.exception;

import com.github.thundax.bacon.common.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InventoryErrorCode implements ErrorCode {
    INVALID_INVENTORY_KEY("INV-400001", "Invalid inventory key", HttpStatus.BAD_REQUEST),
    INVALID_ON_HAND_QUANTITY("INV-400002", "Invalid on hand quantity", HttpStatus.BAD_REQUEST),
    INVALID_REQUIRED_QUANTITY("INV-400008", "Invalid required quantity", HttpStatus.BAD_REQUEST),
    INVALID_DELTA_QUANTITY("INV-400007", "Invalid delta quantity", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("INV-409001", "Insufficient stock", HttpStatus.CONFLICT),
    INSUFFICIENT_AVAILABLE_STOCK("INV-409008", "Insufficient available stock", HttpStatus.CONFLICT),
    RESERVED_QUANTITY_NOT_ENOUGH("INV-409002", "Reserved quantity not enough", HttpStatus.CONFLICT),
    ON_HAND_QUANTITY_NOT_ENOUGH("INV-409003", "On hand quantity not enough", HttpStatus.CONFLICT),
    INVENTORY_DISABLED("INV-409004", "Inventory is disabled", HttpStatus.CONFLICT),
    INVALID_QUANTITY("INV-400003", "Invalid quantity", HttpStatus.BAD_REQUEST),
    INVALID_INVENTORY_STATUS("INV-400004", "Invalid inventory status", HttpStatus.BAD_REQUEST),
    INVALID_ID_GENERATOR_CONFIG("INV-400006", "Invalid id generator config", HttpStatus.BAD_REQUEST),
    INVALID_RELEASE_REASON("INV-400005", "Invalid release reason", HttpStatus.BAD_REQUEST),
    INVALID_RESERVATION_STATUS("INV-409005", "Invalid reservation status", HttpStatus.CONFLICT),
    INVENTORY_ALREADY_EXISTS("INV-409006", "Inventory already exists", HttpStatus.CONFLICT),
    INVENTORY_NOT_FOUND("INV-404001", "Inventory not found", HttpStatus.NOT_FOUND),
    RESERVATION_NOT_FOUND("INV-404002", "Reservation not found", HttpStatus.NOT_FOUND),
    INVENTORY_CONCURRENT_MODIFIED("INV-409007", "Inventory concurrent modified", HttpStatus.CONFLICT),
    UNKNOWN_RESERVATION_STATUS("INV-500001", "Unknown reservation status", HttpStatus.INTERNAL_SERVER_ERROR),
    INVENTORY_REMOTE_BAD_REQUEST("INV-400901", "Inventory remote bad request", HttpStatus.BAD_REQUEST),
    INVENTORY_REMOTE_UNAUTHORIZED("INV-401901", "Inventory remote unauthorized", HttpStatus.UNAUTHORIZED),
    INVENTORY_REMOTE_FORBIDDEN("INV-403901", "Inventory remote forbidden", HttpStatus.FORBIDDEN),
    INVENTORY_REMOTE_NOT_FOUND("INV-404901", "Inventory remote not found", HttpStatus.NOT_FOUND),
    INVENTORY_REMOTE_CONFLICT("INV-409901", "Inventory remote conflict", HttpStatus.CONFLICT),
    INVENTORY_REMOTE_CIRCUIT_OPEN("INV-503901", "Inventory remote circuit open", HttpStatus.SERVICE_UNAVAILABLE),
    INVENTORY_REMOTE_BULKHEAD_FULL("INV-503902", "Inventory remote bulkhead full", HttpStatus.SERVICE_UNAVAILABLE),
    INVENTORY_REMOTE_UNAVAILABLE("INV-503903", "Inventory remote unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INVENTORY_REMOTE_ERROR("INV-500901", "Inventory remote internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    InventoryErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
