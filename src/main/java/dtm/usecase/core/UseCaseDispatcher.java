package dtm.usecase.core;

public interface UseCaseDispatcher {
    void dispatcher(Class<? extends UseCaseBase> clazz);
    void dispatcher(Class<? extends UseCaseBase> clazz, Object... args);
    UseCaseResult getUseCase(String caseId);
}
