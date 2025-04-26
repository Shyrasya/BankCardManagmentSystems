package com.bank.cardmanagment.datasource.repository;

import com.bank.cardmanagment.model.Transaction;
import com.bank.cardmanagment.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByTypeAndCardId(TransactionType transactionType, Long cardId, Pageable pageable);

    Page<Transaction> findByType(TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByCardId(Long cardId, Pageable pageable);

}
