package com.bank.cardmanagment.domain.service;

import com.bank.cardmanagment.datasource.repository.TransactionRepository;
import com.bank.cardmanagment.model.Transaction;
import com.bank.cardmanagment.model.TransactionResponse;
import com.bank.cardmanagment.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<TransactionResponse> getAllTransactions(TransactionType transactionType, Long cardId, Pageable pageable){
        if (transactionType != null && cardId != null){
            return transactionRepository.findByTypeAndCardId(transactionType, cardId, pageable)
                    .map(this::convertToTransactionResponse);
        }
        else if (transactionType != null){
            return transactionRepository.findByType(transactionType, pageable)
                    .map(this::convertToTransactionResponse);
        } else if (cardId != null){
            return transactionRepository.findByCardId(cardId, pageable)
                    .map(this::convertToTransactionResponse);
        }
        return transactionRepository.findAll(pageable)
                .map(this::convertToTransactionResponse);
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction){
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().toString(),
                transaction.getDescription(),
                transaction.getTimestamp());
    }

}
