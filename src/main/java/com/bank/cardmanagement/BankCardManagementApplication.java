package com.bank.cardmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения BankCardManagement.
 * Запускает Spring Boot приложение.
 */
@SpringBootApplication
public class BankCardManagementApplication {
    /**
     * Точка входа в приложение.
     * Инициализирует контекст Spring и запускает приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(BankCardManagementApplication.class, args);
    }
}
