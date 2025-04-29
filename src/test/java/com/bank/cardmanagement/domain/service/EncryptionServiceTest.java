package com.bank.cardmanagement.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class EncryptionServiceTest {
    @InjectMocks
    private EncryptionService encryptionService;

    private void setUp() {
        ReflectionTestUtils.setField(encryptionService, "secretKeyRaw", "1234567890123456");
        encryptionService.init();
    }

    @Test
    void testEncryptAndDecrypt() {
        setUp();
        String original = "SensitiveData123";

        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        Assertions.assertNotEquals(original, encrypted);
        Assertions.assertEquals(original, decrypted);
    }

    @Test
    void testEncrypt_throwsExceptionIfInvalidData() {
        ReflectionTestUtils.setField(encryptionService, "secretKeyRaw", "1234567890123456");
        String encryptedData = "%%%invalidbase64%%%";

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                encryptionService.encrypt(encryptedData));

        Assertions.assertEquals("Ошибка при шифровании!", exception.getMessage());
    }

    @Test
    void testDecrypt_throwsExceptionIfInvalidData() {
        setUp();
        String encryptedData = "%%%invalidbase64%%%";

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                encryptionService.decrypt(encryptedData));

        Assertions.assertEquals("Ошибка при дешифровании!", exception.getMessage());
    }

    @Test
    void testInit_throwsExceptionIfNotValidKey() {
        ReflectionTestUtils.setField(encryptionService, "secretKeyRaw", "1234567890123");

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                encryptionService.init());

        Assertions.assertEquals("Ключ должен быть длиной 16 байт!", exception.getMessage());
    }
}
