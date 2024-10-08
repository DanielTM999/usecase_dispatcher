package dtm.usecase.core;

public interface UseCaseDispatcher {
    String dispatcher(Class<? extends UseCaseBase> clazz);
    String dispatcher(Class<? extends UseCaseBase> clazz, Object... args);
    String dispatcher(String PID, Class<? extends UseCaseBase> clazz);
    String dispatcher(String PID, Class<? extends UseCaseBase> clazz, Object... args);
    UseCaseResult getUseCase(String caseId);
}
