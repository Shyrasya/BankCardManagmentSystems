package com.bank.cardmanagment.datasource.repository;

import com.bank.cardmanagment.model.Card;
import org.springframework.data.repository.CrudRepository;

public interface CardRepository extends CrudRepository<Card, Long> {

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
}
