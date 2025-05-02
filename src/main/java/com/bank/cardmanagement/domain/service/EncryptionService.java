package com.bank.cardmanagement.domain.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Сервис для шифрования и дешифрования данных с использованием алгоритма AES.
 * Использует секретный ключ, который считывается из конфигурации приложения.
 */
@Service
public class EncryptionService {

    /**
     * Секретный ключ для шифрования, считываемый из конфигурации приложения.
     */
    @Value("${app.encryption.secret-key}")
    private String secretKeyRaw;

    /**
     * Спецификация ключа для использования в алгоритме AES.
     */
    private SecretKeySpec secretKeySpec;

    /**
     * Инициализация сервиса: создание ключа для шифрования и дешифрования.
     * Проверка, что ключ имеет длину 16 байт.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKeyRaw.getBytes();
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("Ключ должен быть длиной 16 байт!");
        }
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Шифрует строку данных с использованием алгоритма AES.
     *
     * @param data данные, которые нужно зашифровать
     * @return зашифрованные данные в формате Base64
     * @throws RuntimeException если произошла ошибка при шифровании
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании!", e);
        }
    }

    /**
     * Дешифрует строку данных, зашифрованную с использованием алгоритма AES.
     *
     * @param encryptedData зашифрованные данные в формате Base64
     * @return дешифрованные данные в виде строки
     * @throws RuntimeException если произошла ошибка при дешифровании
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при дешифровании!", e);
        }
    }
}
