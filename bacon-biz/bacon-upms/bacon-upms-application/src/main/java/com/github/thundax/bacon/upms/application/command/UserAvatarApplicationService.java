package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectCommandFacade;
import com.github.thundax.bacon.storage.api.request.StoredObjectReferenceFacadeRequest;
import com.github.thundax.bacon.storage.api.request.UploadObjectFacadeRequest;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.upms.application.assembler.UserAssembler;
import com.github.thundax.bacon.upms.application.command.UserAvatarUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAvatarApplicationService {

    private static final String USER_AVATAR_OWNER_TYPE = "UPMS_USER_AVATAR";
    private static final String USER_AVATAR_CATEGORY = "avatar";
    private static final long MAX_AVATAR_SIZE = 2L * 1024L * 1024L;
    private static final int MIN_AVATAR_PIXEL = 128;
    private static final int MAX_AVATAR_PIXEL = 1024;
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final StoredObjectCommandFacade storedObjectCommandFacade;

    public UserAvatarApplicationService(
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository,
            StoredObjectCommandFacade storedObjectCommandFacade) {
        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.storedObjectCommandFacade = storedObjectCommandFacade;
    }

    @Transactional
    public UserDTO updateAvatar(UserAvatarUpdateCommand command) {
        User currentUser = requireUser(command.userId());
        AvatarImage avatarImage =
                readAndValidateAvatar(command.originalFilename(), command.contentType(), command.size(), command.inputStream());
        StoredObjectFacadeResponse storedObject = uploadAvatarObject(avatarImage);
        AvatarStoredObjectNo avatarStoredObjectNo = AvatarStoredObjectNo.of(storedObject.getStoredObjectNo());
        storedObjectCommandFacade.markObjectReferenced(
                new StoredObjectReferenceFacadeRequest(
                        avatarStoredObjectNo.value(), USER_AVATAR_OWNER_TYPE, String.valueOf(command.userId().value())));
        AvatarStoredObjectNo previousAvatarStoredObjectNo = currentUser.getAvatarStoredObjectNo();
        try {
            currentUser.useAvatar(avatarStoredObjectNo);
            User savedUser = userRepository.update(currentUser);
            if (previousAvatarStoredObjectNo != null && !previousAvatarStoredObjectNo.equals(avatarStoredObjectNo)) {
                storedObjectCommandFacade.clearObjectReference(
                        new StoredObjectReferenceFacadeRequest(
                                previousAvatarStoredObjectNo.value(),
                                USER_AVATAR_OWNER_TYPE,
                                String.valueOf(command.userId().value())));
            }
            return UserAssembler.toDto(
                    savedUser,
                    requireIdentityValue(savedUser.getId(), UserIdentityType.ACCOUNT),
                    resolveIdentityValue(savedUser.getId(), UserIdentityType.PHONE),
                    storedObject.getAccessEndpoint());
        } catch (RuntimeException ex) {
            storedObjectCommandFacade.clearObjectReference(
                    new StoredObjectReferenceFacadeRequest(
                            avatarStoredObjectNo.value(), USER_AVATAR_OWNER_TYPE, String.valueOf(command.userId().value())));
            throw ex;
        }
    }

    private User requireUser(UserId userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private String requireIdentityValue(UserId userId, UserIdentityType identityType) {
        return userIdentityRepository
                .findIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElseThrow(() -> new NotFoundException(
                        "User identity not found: " + userId + "/" + identityType.value()));
    }

    private String resolveIdentityValue(UserId userId, UserIdentityType identityType) {
        return userIdentityRepository
                .findIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElse(null);
    }

    private AvatarImage readAndValidateAvatar(
            String originalFilename, String contentType, Long size, InputStream inputStream) {
        validateRequired(originalFilename, "originalFilename");
        if (inputStream == null) {
            throw new BadRequestException("avatar file must not be null");
        }
        if (size == null || size <= 0L) {
            throw new BadRequestException("avatar size must be greater than 0");
        }
        if (size > MAX_AVATAR_SIZE) {
            throw new BadRequestException("avatar size exceeds 2MB");
        }
        String normalizedContentType =
                contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_AVATAR_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestException("avatar contentType must be image/jpeg or image/png");
        }
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                throw new BadRequestException("avatar file must not be empty");
            }
            if (bytes.length > MAX_AVATAR_SIZE) {
                throw new BadRequestException("avatar size exceeds 2MB");
            }
            String actualContentType = detectAvatarContentType(bytes);
            if (!normalizedContentType.equals(actualContentType)) {
                throw new BadRequestException("avatar contentType does not match image data");
            }
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bufferedImage == null) {
                throw new BadRequestException("avatar file is not a valid image");
            }
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if (width != height) {
                throw new BadRequestException("avatar image must be square");
            }
            if (width < MIN_AVATAR_PIXEL || width > MAX_AVATAR_PIXEL) {
                throw new BadRequestException("avatar image width must be between 128 and 1024");
            }
            if (height < MIN_AVATAR_PIXEL || height > MAX_AVATAR_PIXEL) {
                throw new BadRequestException("avatar image height must be between 128 and 1024");
            }
            return new AvatarImage(originalFilename.trim(), actualContentType, (long) bytes.length, bytes);
        } catch (IOException ex) {
            throw new BadRequestException("avatar file cannot be read", ex);
        }
    }

    private StoredObjectFacadeResponse uploadAvatarObject(AvatarImage avatarImage) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(avatarImage.bytes())) {
            return storedObjectCommandFacade.uploadObject(new UploadObjectFacadeRequest(
                    USER_AVATAR_OWNER_TYPE,
                    USER_AVATAR_CATEGORY,
                    avatarImage.originalFilename(),
                    avatarImage.contentType(),
                    avatarImage.size(),
                    inputStream));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to close avatar stream", ex);
        }
    }

    private String detectAvatarContentType(byte[] bytes) throws IOException {
        ImageIO.setUseCache(false);
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (imageInputStream == null) {
                throw new BadRequestException("avatar file is not a valid image");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                throw new BadRequestException("avatar file is not a supported image");
            }
            ImageReader reader = readers.next();
            try {
                String formatName = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpeg".equals(formatName) || "jpg".equals(formatName)) {
                    return "image/jpeg";
                }
                if ("png".equals(formatName)) {
                    return "image/png";
                }
                throw new BadRequestException("avatar image format must be jpeg or png");
            } finally {
                reader.dispose();
            }
        }
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }

    private record AvatarImage(String originalFilename, String contentType, Long size, byte[] bytes) {}
}
