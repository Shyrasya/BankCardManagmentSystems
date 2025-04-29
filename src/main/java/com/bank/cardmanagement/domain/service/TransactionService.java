package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.dto.response.TransactionResponse;
import com.bank.cardmanagement.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    private final CardRepository cardRepository;

    private final CardValidationService cardValidationService;

    public TransactionService(TransactionRepository transactionRepository, CardRepository cardRepository, CardValidationService cardValidationService) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.cardValidationService = cardValidationService;
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

    public Page<TransactionResponse> getAllMyTransactions(TransactionType transactionType, Long cardId, Pageable pageable) {
        Long userId = cardValidationService.getCurrentUserId();
        if (cardId != null){
            boolean isMyCard = cardRepository.existsByIdAndUserId(cardId, userId);
            if (!isMyCard){
                throw new AccessDeniedException("Вы не имеете доступа к данной карте!");
            }
        }
        if (transactionType != null && cardId != null){
            return transactionRepository.findByTypeAndCardId(transactionType, cardId, pageable)
                    .map(this::convertToTransactionResponse);
        }
        else if (transactionType != null){
            return transactionRepository.findByTypeAndUserId(transactionType, userId, pageable)
                    .map(this::convertToTransactionResponse);
        } else if (cardId != null){
            return transactionRepository.findByCardId(cardId, pageable)
                    .map(this::convertToTransactionResponse);
        }
        return transactionRepository.findByUserId(userId, pageable)
                .map(this::convertToTransactionResponse);
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction){
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().toString(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTimestamp());
    }
}
