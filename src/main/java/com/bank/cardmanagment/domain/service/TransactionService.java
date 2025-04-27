package com.bank.cardmanagment.domain.service;

import com.bank.cardmanagment.datasource.repository.CardRepository;
import com.bank.cardmanagment.datasource.repository.TransactionRepository;
import com.bank.cardmanagment.model.Transaction;
import com.bank.cardmanagment.model.TransactionResponse;
import com.bank.cardmanagment.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    private final CardRepository cardRepository;

    public TransactionService(TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
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
        Long userId = getCurrentUserId();
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
                transaction.getDescription(),
                transaction.getTimestamp());
    }

    private Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else {
            throw new IllegalStateException("Principal для UserId в JwtAuthentication не является типом Long!");
        }
    }

}
