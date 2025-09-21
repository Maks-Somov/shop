package ru.innotech.orderorchestrator.core.model;

public enum SagaState {NEW, WAIT_PAYMENT, WAIT_STOCK, ROLLBACKING, COMPLETED, ROLLED_BACK, FAILED}